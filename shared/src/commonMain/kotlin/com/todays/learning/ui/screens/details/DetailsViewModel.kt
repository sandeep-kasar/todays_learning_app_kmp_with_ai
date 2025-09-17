package com.todays.learning.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todays.learning.domain.repositories.TimetableRepository
import com.todays.learning.utils.DetailsUiState
import com.todays.learning.utils.GptSummaryUiState
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Details screen.
 * Handles subject details and AI-powered learning summaries.
 */
class DetailsViewModel(
    private val timetableRepository: TimetableRepository
) : ViewModel() {

    private val _selectedTabState = MutableStateFlow(0)
    val selectedTabState = _selectedTabState.asStateFlow()

    private val _subjectDetailsState = MutableStateFlow(DetailsUiState())
    val subjectDetailsState = _subjectDetailsState.asStateFlow()

    private val _gptSummaryState = MutableStateFlow(GptSummaryUiState())
    val gptSummaryState = _gptSummaryState.asStateFlow()

    // Track active summary job to cancel ongoing summaries
    private var summaryJob: Job? = null

    // Exception handler for coroutines
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _subjectDetailsState.update { it.copy(isLoading = false, error = exception.message) }
        _gptSummaryState.update { it.copy(isLoading = false, error = exception.message) }
    }

    fun getSubjectDetails(subject: String) {
        _subjectDetailsState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch(coroutineExceptionHandler) {
            timetableRepository.fetchSubjectDetails(subject = subject)
                .onSuccess { data ->
                    data.collectLatest { subjectDetails ->
                        _subjectDetailsState.update {
                            it.copy(subjectDetails = subjectDetails, isLoading = false)
                        }
                    }
                }.onFailure { error ->
                    _subjectDetailsState.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }

    fun updateClick(index: Int) {
        // Direct value assignment is more efficient than update for simple cases
        _selectedTabState.value = index
    }

    /**
     * Internal helper to handle learning summaries with different AI models.
     */
    private fun summarizeLearningInternal(
        text: String,
        apiKey: String,
        fetchSummary: suspend (String, String) -> Result<Flow<String>>
    ) {
        // Set loading state immediately
        _gptSummaryState.update { it.copy(isLoading = true, error = null, summary = null) }

        // Cancel any ongoing summary job
        summaryJob?.cancel()
        summaryJob = viewModelScope.launch(coroutineExceptionHandler) {
            fetchSummary(text, apiKey)
                .onSuccess { flow ->
                    flow.collectLatest { summary ->
                        _gptSummaryState.update {
                            it.copy(summary = summary, isLoading = false)
                        }
                    }
                }.onFailure { error ->
                    _gptSummaryState.update { it.copy(error = error.message, isLoading = false) }
                }
        }
    }

    fun summarizeLearningUsingGpt(text: String, apiKey: String) {
        summarizeLearningInternal(text, apiKey, timetableRepository::summarizeLearning)
    }

    fun summarizeLearningUsingGemini(text: String, apiKey: String) {
        summarizeLearningInternal(text, apiKey, timetableRepository::summarizeLearningUsingGemini)
    }
}
