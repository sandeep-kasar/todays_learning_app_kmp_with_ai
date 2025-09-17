package com.todays.learning.data.network.models.subject

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SubjectResultDto(

    @SerialName("status")
    val status: String? = null,

    @SerialName("result")
    val result: SubjectDto? = null,
)
