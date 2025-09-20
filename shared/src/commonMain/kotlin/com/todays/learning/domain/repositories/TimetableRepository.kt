package com.todays.learning.domain.repositories

import com.todays.learning.domain.models.Subject
import com.todays.learning.domain.models.TimeTable
import kotlinx.coroutines.flow.Flow

interface TimetableRepository {

    /** Fetch today's timetable from data source*/
    suspend fun fetchTodaysTimetable(
        standard: String,
        division: String
    ): Result<Flow<List<TimeTable>?>>

    /** Fetch subject details from data source*/
    suspend fun fetchSubjectDetails(
        subject: String,
    ): Result<Flow<Subject?>>
}
