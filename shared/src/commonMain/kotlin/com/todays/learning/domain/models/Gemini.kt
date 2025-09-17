package com.todays.learning.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Part(val text: String)

@Serializable
data class ContentBlock(val parts: List<Part>)

@Serializable
data class GeminiGenerateContentsRequest(
    val contents: List<ContentBlock>
)