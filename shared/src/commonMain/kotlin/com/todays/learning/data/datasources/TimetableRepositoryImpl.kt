package com.todays.learning.data.datasources


import com.todays.learning.data.mappers.toDomain
import com.todays.learning.data.network.models.subject.SubjectResultDto
import com.todays.learning.data.network.models.timetable.TimetableResultDto
import com.todays.learning.data.network.utils.safeApiCall
import com.todays.learning.domain.models.ChatMessage
import com.todays.learning.domain.models.ChatRequest
import com.todays.learning.domain.models.ChatResponse
import com.todays.learning.domain.models.ContentBlock
import com.todays.learning.domain.models.GeminiGenerateContentsRequest
import com.todays.learning.domain.models.Part
import com.todays.learning.domain.models.Subject
import com.todays.learning.domain.models.TimeTable
import com.todays.learning.domain.repositories.TimetableRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

class TimetableRepositoryImpl(
    private val httpClient: HttpClient
) : TimetableRepository {


    override suspend fun fetchTodaysTimetable(
        standard: String,
        division: String
    ): Result<Flow<List<TimeTable>?>> {
        return safeApiCall {
            val response = httpClient.get {
                url {
                    appendPathSegments("timetable", standard, division)
                }
            }.body<TimetableResultDto>()

            response.result?.map { it.toDomain() }
        }
    }

    override suspend fun fetchSubjectDetails(subject: String): Result<Flow<Subject?>> {
        return safeApiCall {
            val response = httpClient.get {
                url {
                    appendPathSegments("subject", "details", subject)
                }
            }.body<SubjectResultDto>()

            response.result?.toDomain()
        }
    }


    override suspend fun summarizeLearning(
        learningText: String,
        apiKey: String
    ): Result<Flow<String>> {
        val endpoint = "https://api.openai.com/v1/chat/completions"

        val request = ChatRequest(
            model = "gpt-3.5-turbo",
            messages = listOf(
                ChatMessage("user", "Summarize the following learning objective:\n$learningText")
            ),
            temperature = 0.7
        )

        return try {
            val response: ChatResponse = httpClient.post(endpoint) {
                header(HttpHeaders.Authorization, "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body() // deserialize to ChatResponse

            val summary = response.choices.firstOrNull()?.message?.content ?: "No summary found"
            Result.success(flowOf(summary)) // wrap summary string inside a Flow and Result.success

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }


    override suspend fun summarizeLearningUsingGemini(
        learningText: String,
        apiKey: String
    ): Result<Flow<String>> {
        val endpoint =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent"

        // Build request matching the "contents" -> "parts" shape
        val requestDto = GeminiGenerateContentsRequest(
            contents = listOf(
                ContentBlock(
                    parts = listOf(
                        Part(
                            text = "Summarize the following learning objective concisely:\n\n$learningText"
                        )
                    )
                )
            )
        )

        return try {
            val response: HttpResponse = httpClient.post(endpoint) {
                // Use API key header; alternatively you can append ?key=API_KEY to endpoint
                header("X-goog-api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(requestDto) // serializes via ContentNegotiation you already installed
            }

            val bodyText = response.bodyAsText()
            val json = Json { ignoreUnknownKeys = true; isLenient = true }

            // Parse into JsonElement for flexible extraction
            val root: JsonElement = try {
                json.parseToJsonElement(bodyText)
            } catch (e: Exception) {
                null
            } ?: return Result.success(flowOf("No summary found (invalid JSON response)"))

            // Try extraction strategies in order of likelihood
            val extracted: String? = run {
                // 1) outputs -> first -> content -> find first "text" or "output_text"
                try {
                    val outputs = root.jsonObject["outputs"]?.jsonArray
                    if (outputs != null && outputs.isNotEmpty()) {
                        val firstOutput = outputs[0]
                        val contentArr = firstOutput.jsonObject["content"]?.jsonArray
                        if (contentArr != null && contentArr.isNotEmpty()) {
                            // search for an element with "text" or "output" fields
                            for (elem in contentArr) {
                                val obj = elem.jsonObject
                                // direct text field
                                obj["text"]?.let { if (it is JsonPrimitive && it.isString) return@run it.content }
                                // nested output text maybe under "parts"
                                obj["parts"]?.jsonArray?.firstOrNull()?.jsonObject?.get("text")
                                    ?.let {
                                        if (it is JsonPrimitive && it.isString) return@run it.content
                                    }
                            }
                        }
                    }
                } catch (_: Exception) {
                }

                // 2) candidates -> first -> output (string)
                try {
                    val candidates = root.jsonObject["candidates"]?.jsonArray
                    if (candidates != null && candidates.isNotEmpty()) {
                        val first = candidates[0].jsonObject
                        first["output"]?.let { if (it is JsonPrimitive && it.isString) return@run it.content }
                    }
                } catch (_: Exception) {
                }

                // 3) top-level "text"
                try {
                    root.jsonObject["text"]?.let {
                        if (it is JsonPrimitive && it.isString) return@run it.content
                    }
                } catch (_: Exception) {
                }

                // 4) generic deep search for any "text" primitive >= 10 chars (naive fallback)
                try {
                    val found = mutableListOf<String>()
                    fun walk(e: JsonElement) {
                        when (e) {
                            is JsonObject -> e.values.forEach { walk(it) }
                            is JsonArray -> e.forEach { walk(it) }
                            is JsonPrimitive -> if (e.isString && e.content.length >= 10) found += e.content
                        }
                    }
                    walk(root)
                    found.firstOrNull()
                } catch (_: Exception) {
                    null
                }
            }

            val summary = extracted ?: "No summary found"
            Result.success(flowOf(summary))

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }


    override suspend fun askMe(
        prompt: String,
        apiKey: String
    ): Result<Flow<String>> {
        return try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:streamGenerateContent?key=$apiKey"

            val requestBody = buildJsonObject {
                put("contents", buildJsonArray {
                    add(buildJsonObject {
                        put("role", "user")
                        put("parts", buildJsonArray {
                            add(buildJsonObject {
                                put("text", prompt)
                            })
                        })
                    })
                })
                put("generationConfig", buildJsonObject {
                    put("temperature", 0.7)
                    put("maxOutputTokens", 2048)
                    put("topP", 0.8)
                    put("topK", 40)
                })
                put("safetySettings", buildJsonArray {
                    add(buildJsonObject {
                        put("category", "HARM_CATEGORY_HARASSMENT")
                        put("threshold", "BLOCK_NONE")
                    })
                })
            }.toString()

            val sseFlow: Flow<String> = flow {
                val response: HttpResponse = httpClient.post(url) {
                    header("x-goog-api-key", apiKey)
                    header(HttpHeaders.Accept, "text/event-stream")
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                    // If your client supports timeouts, ensure a generous read timeout for long generations
                }

                if (!response.status.isSuccess()) {
                    val txt = response.bodyAsText()
                    throw IllegalStateException("Upstream error ${response.status}: $txt")
                }

                val channel = response.bodyAsChannel()

                // We will read lines until channel closes or coroutine cancelled
                // SSE format: lines prefixed with "event: ..." and "data: <json>"
                val dataBuffer = StringBuilder()
                while (isActive) {
                    val line = channel.readUTF8Line(1000) ?: break
                    // debug: you can log raw line here to troubleshoot
                    // Log.d("SSE", line)

                    // Many SSE endpoints send blank lines between events
                    if (line.isBlank()) {
                        // End of a single SSE event. If we've accumulated data lines, process them.
                        if (dataBuffer.isNotBlank()) {
                            val dataText = dataBuffer.toString().trim()
                            dataBuffer.clear()

                            // Some SSE servers send "data: [ ... ]" (array) or "data: {...}"
                            // Also skip sentinel messages like "[DONE]" or "done"
                            if (dataText == "[DONE]" || dataText == "\"[DONE]\"" || dataText.equals(
                                    "done",
                                    true
                                )
                            ) {
                                break
                            }

                            // Try parsing JSON; ignore parse errors for non-JSON control messages
                            try {
                                val jsonElem = Json.parseToJsonElement(dataText)

                                val txt = jsonElem.jsonObject["candidates"]
                                    ?.jsonArray?.getOrNull(0)
                                    ?.jsonObject
                                    ?.get("content")
                                    ?.jsonObject
                                    ?.get("parts")
                                    ?.jsonArray?.getOrNull(0)
                                    ?.jsonObject
                                    ?.get("text")
                                    ?.jsonPrimitive
                                    ?.content

                                if (!txt.isNullOrBlank()) {
                                    emit(txt)
                                } else if (jsonElem is kotlinx.serialization.json.JsonPrimitive) {
                                    // fallback: if the payload itself is just a string
                                    val maybeStr = jsonElem.contentOrNull
                                    if (!maybeStr.isNullOrBlank()) emit(maybeStr)
                                }
                            } catch (e: Exception) {
                                // ignore parse errors
                            }

                        }
                        continue
                    }

                    // Only consider lines starting with "data:"; other lines can be safely ignored for now.
                    if (line.startsWith("data:")) {
                        // everything after "data:" can be JSON or part of a JSON stream
                        val after = line.removePrefix("data:").trimStart()
                        dataBuffer.append(after)
                        // Note: do NOT emit here — wait until blank line which marks end of event
                    } else {
                        // There may be "event:" lines or other metadata; ignore unless debugging
                    }
                } // while

            }

            Result.success(sseFlow)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }


}
