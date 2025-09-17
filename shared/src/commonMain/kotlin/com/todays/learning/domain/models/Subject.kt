package com.todays.learning.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Subject(
    val subject: String? = null,
    val learning: String? = null,
    val homework: String? = null,
    val keyPoints: String? = null,
)

