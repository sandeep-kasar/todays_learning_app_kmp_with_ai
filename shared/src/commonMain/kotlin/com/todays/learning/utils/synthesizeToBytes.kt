package com.todays.learning.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


@Serializable
private data class TtsRequest(
    val input: Map<String, String>,
    val voice: Map<String, String?>,
    val audioConfig: Map<String, String>
)

@Serializable
private data class TtsResponse(
    @SerialName("audioContent") val audioContent: String? = null
)

/**
 * Helper to call Google Cloud Text-to-Speech REST API.
 */
@OptIn(ExperimentalEncodingApi::class)
suspend fun HttpClient.synthesizeToBytes(
    text: String,
    languageCode: String = "en-US",
    voiceName: String? = null,
    audioEncoding: String = "MP3",
    apiKey: String
): ByteArray {
    val requestObj = TtsRequest(
        input = mapOf("text" to text),
        voice = mapOf("languageCode" to languageCode, "name" to voiceName),
        audioConfig = mapOf("audioEncoding" to audioEncoding)
    )

    val url = "https://texttospeech.googleapis.com/v1/text:synthesize?key=$apiKey"

    val response: HttpResponse = this.post(url) {
        contentType(ContentType.Application.Json)
        setBody(requestObj)
    }

    if (!response.status.isSuccess()) {
        val bodyText = response.bodyAsText()
        throw Exception("TTS request failed: ${response.status} - $bodyText")
    }

    val parsed: TtsResponse = response.body()
    val base64 = parsed.audioContent
        ?: throw Exception("No audioContent in TTS response")

    return Base64.decode(base64)
}
