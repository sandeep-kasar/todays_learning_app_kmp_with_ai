package com.todays.learning.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todays.learning.data.mappers.toDomain
import com.todays.learning.data.network.models.timetable.TimetableResultDto
import com.todays.learning.domain.models.TimeTable
import com.todays.learning.domain.repositories.TimetableRepository
import com.todays.learning.utils.HomeUiState
import com.todays.learning.utils.fallbackTimetableJson
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class HomeViewModel(private val timetableRepository: TimetableRepository) : ViewModel() {

    private val _homeUiState = MutableStateFlow(HomeUiState(isLoading = true))
    val homeUiState = _homeUiState.asStateFlow()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _homeUiState.update { it.copy(isLoading = false, error = exception.message) }
    }

    init {
        fetchTodaysTimetable()
    }

    private fun fetchTodaysTimetable() = viewModelScope.launch(coroutineExceptionHandler) {
        timetableRepository.fetchTodaysTimetable("4", "A").onSuccess { data ->
            data.collectLatest { timeTable ->
                _homeUiState.update {
                    it.copy(
                        todaysTimetable = timeTable,
                        isLoading = false
                    )
                }
            }
        }.onFailure { error ->
            // Try to parse fallback JSON when API fails
            try {
                val fallback = Json.decodeFromString<TimetableResultDto>(fallbackTimetableJson)
                val fallbackResult =
                    fallback.result?.map { it.toDomain() } ?: emptyList<TimeTable>()
                _homeUiState.update {
                    it.copy(
                        todaysTimetable = fallbackResult,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _homeUiState.update {
                    it.copy(
                        error = "Failed to fetch timetable: ${error.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

}