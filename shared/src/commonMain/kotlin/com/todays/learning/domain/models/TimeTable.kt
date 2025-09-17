package com.todays.learning.domain.models

data class TimeTable(
    val time: String? = null,
    val subject: Subject? = null,
    val standard: Int? = null,
    val division: String? = null
)
