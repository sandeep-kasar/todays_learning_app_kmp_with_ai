package com.todays.learning.data.network.models.subject

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubjectDto(

    @SerialName("subject")
    val subject: String? = null,

    @SerialName("learning")
    val learning: String? = null,

    @SerialName("homework")
    val homework: String? = null,

    @SerialName("keyPoints")
    val keyPoints: String? = null
)
