package com.todays.learning.data.mappers

import com.todays.learning.data.network.models.ErrorResponseDto
import com.todays.learning.data.network.models.subject.SubjectDto
import com.todays.learning.data.network.models.timetable.TimeTableDto
import com.todays.learning.domain.models.ErrorResponse
import com.todays.learning.domain.models.Subject
import com.todays.learning.domain.models.TimeTable


fun TimeTableDto.toDomain(): TimeTable {
    return TimeTable(
        time = this.time,
        subject = this.subject,
        standard = this.standard,
        division = this.division,
    )
}

fun SubjectDto.toDomain(): Subject {
    return Subject(
        subject = this.subject,
        learning = this.learning,
        homework = this.homework,
        keyPoints = this.keyPoints,
    )
}

fun ErrorResponseDto.toDomain(): ErrorResponse {
    return ErrorResponse(
        success = this.success,
        statusCode = this.statusCode,
        statusMessage = this.statusMessage
    )
}
