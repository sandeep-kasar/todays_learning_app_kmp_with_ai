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
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.appendPathSegments
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
}
