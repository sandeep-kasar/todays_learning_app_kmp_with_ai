package com.todays.learning.utils

import com.todays.learning.domain.models.Subject
import com.todays.learning.domain.models.TimeTable

data class MainUiState(
    val selectedTheme: Int? = 0
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val todaysTimetable: List<TimeTable>? = emptyList(),
)

data class SettingsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTheme: Int = 0,
    val selectedImageQuality: Int = 0
)

data class DetailsUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val subjectDetails: Subject? = null,
)

data class GptSummaryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val summary: String? = null,
)
