package com.todays.learning.data.network.models.timetable


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimetableResultDto(

    @SerialName("status")
    val status: String? = null,

    @SerialName("result")
    val result: List<TimeTableDto>? = null,
)
