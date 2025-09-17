package com.todays.learning.ui.screens.me

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todays.learning.domain.repositories.TimetableRepository
import com.todays.learning.utils.DetailsUiState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MeViewModel(
    private val timetableRepository: TimetableRepository
) : ViewModel() {

    private val _selectedTabState = MutableStateFlow(0)
    val selectedTabState = _selectedTabState.asStateFlow()

    private val _subjectDetailsState = MutableStateFlow(DetailsUiState())
    val subjectDetailsState = _subjectDetailsState.asStateFlow()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _subjectDetailsState.update { it.copy(isLoading = false, error = exception.message) }
    }

    fun getSubjectDetails(subject: String) = viewModelScope.launch(coroutineExceptionHandler) {
        timetableRepository.fetchSubjectDetails(subject = subject).onSuccess { data ->
            data.collectLatest { subjectDetails ->
                _subjectDetailsState.update {
                    it.copy(subjectDetails = subjectDetails, isLoading = false)
                }
            }
        }.onFailure { error ->
            _subjectDetailsState.update { it.copy(error = error.message, isLoading = false) }
        }
    }

    fun updateClick(index: Int) {
        _selectedTabState.update {
            index
        }
    }
}
