package com.todays.learning.data.datasources

import com.todays.learning.domain.models.ChatMessage
import com.todays.learning.domain.models.ChatRequest
import com.todays.learning.domain.models.ChatResponse
import com.todays.learning.domain.models.ContentBlock
import com.todays.learning.domain.models.GeminiGenerateContentsRequest
import com.todays.learning.domain.models.Part
import com.todays.learning.domain.repositories.AiTaskRepository
import com.todays.learning.utils.synthesizeToBytes
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

    class AiTaskRepositoryImpl(
    private val httpClient: HttpClient
) : AiTaskRepository {

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
            }.body()

            val summary = response.choices.firstOrNull()?.message?.content ?: "No summary found"
            Result.success(flowOf(summary))

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
                header("X-goog-api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(requestDto)
            }

            val bodyText = response.bodyAsText()
            val json = Json { ignoreUnknownKeys = true; isLenient = true }

            val root: JsonElement = try {
                json.parseToJsonElement(bodyText)
            } catch (e: Exception) {
                null
            } ?: return Result.success(flowOf("No summary found (invalid JSON response)"))

            val extracted: String? = run {
                try {
                    val outputs = root.jsonObject["outputs"]?.jsonArray
                    if (outputs != null && outputs.isNotEmpty()) {
                        val firstOutput = outputs[0]
                        val contentArr = firstOutput.jsonObject["content"]?.jsonArray
                        if (contentArr != null && contentArr.isNotEmpty()) {
                            for (elem in contentArr) {
                                val obj = elem.jsonObject
                                obj["text"]?.let { if (it is JsonPrimitive && it.isString) return@run it.content }
                                obj["parts"]?.jsonArray?.firstOrNull()?.jsonObject?.get("text")
                                    ?.let {
                                        if (it is JsonPrimitive && it.isString) return@run it.content
                                    }
                            }
                        }
                    }
                } catch (_: Exception) {
                }

                try {
                    val candidates = root.jsonObject["candidates"]?.jsonArray
                    if (candidates != null && candidates.isNotEmpty()) {
                        val first = candidates[0].jsonObject
                        first["output"]?.let { if (it is JsonPrimitive && it.isString) return@run it.content }
                    }
                } catch (_: Exception) {
                }

                try {
                    root.jsonObject["text"]?.let {
                        if (it is JsonPrimitive && it.isString) return@run it.content
                    }
                } catch (_: Exception) {
                }

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

    override suspend fun synthesizeToBytes(
        subject: String,
        apiKey: String,
    ): Result<Flow<ByteArray>> {
        return try {
            val bytes = httpClient.synthesizeToBytes(
                text = subject,
                languageCode = "en-US",
                voiceName = null,
                audioEncoding = "MP3",
                apiKey = apiKey
            )
            Result.success(flowOf(bytes))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}