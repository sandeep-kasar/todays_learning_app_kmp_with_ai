package com.todays.learning.ui.screens.calender

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todays.learning.domain.repositories.TimetableRepository
import com.todays.learning.utils.HomeUiState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalenderViewModel(private val timetableRepository: TimetableRepository) : ViewModel() {

    private val _homeUiState = MutableStateFlow(HomeUiState(isLoading = true))
    val homeUiState = _homeUiState.asStateFlow()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _homeUiState.update { it.copy(isLoading = false, error = exception.message) }
    }

    init {
        fetchTodaysTimetable()
    }

    private fun fetchTodaysTimetable() = viewModelScope.launch(coroutineExceptionHandler) {
        timetableRepository.fetchTodaysTimetable("1", "A").onSuccess { data ->
            data.collectLatest { timeTable ->
                _homeUiState.update {
                    it.copy(
                        todaysTimetable = timeTable,
                        isLoading = false
                    )
                }
            }
        }.onFailure { error ->
            _homeUiState.update { it.copy(error = error.message, isLoading = false) }
        }
    }
}