package com.todays.learning.data.network.models.timetable

import com.todays.learning.domain.models.Subject
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimeTableDto(

    @SerialName("id")
    val id: String? = null,

    @SerialName("time")
    val time: String? = null,

    @SerialName("subject")
    val subject: Subject? = null,

    @SerialName("standard")
    val standard: Int? = null,

    @SerialName("division")
    val division: String? = null
)
