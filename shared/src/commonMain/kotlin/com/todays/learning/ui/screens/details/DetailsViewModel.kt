package com.todays.learning.ui.screens.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todays.learning.data.mappers.toDomain
import com.todays.learning.data.network.models.subject.SubjectResultDto
import com.todays.learning.domain.repositories.AiTaskRepository
import com.todays.learning.domain.repositories.TimetableRepository
import com.todays.learning.domain.utils.Constants.GEMINI_API_KEY
import com.todays.learning.utils.DetailsUiState
import com.todays.learning.utils.GptSummaryUiState
import com.todays.learning.utils.TtsUiState
import com.todays.learning.utils.fallbackSubjectJson
import com.todays.learning.utils.playAudioBytes
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock.System
import kotlinx.serialization.json.Json

/**
 * ViewModel for the Details screen.
 * Handles subject details and AI-powered learning summaries.
 */
class DetailsViewModel(
    private val timetableRepository: TimetableRepository,
    private val aiTaskRepository: AiTaskRepository,
) : ViewModel() {

    private val _selectedTabState = MutableStateFlow(0)
    val selectedTabState = _selectedTabState.asStateFlow()

    private val _subjectDetailsState = MutableStateFlow(DetailsUiState())
    val subjectDetailsState = _subjectDetailsState.asStateFlow()

    private val _gptSummaryState = MutableStateFlow(GptSummaryUiState())
    val gptSummaryState = _gptSummaryState.asStateFlow()

    private val _uiStateTts = MutableStateFlow(TtsUiState())
    val uiStateTts = _uiStateTts.asStateFlow()


    // Track active summary job to cancel ongoing summaries
    private var summaryJob: Job? = null

    // Exception handler for coroutines
    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _subjectDetailsState.update { it.copy(isLoading = false, error = exception.message) }
        _gptSummaryState.update { it.copy(isLoading = false, error = exception.message) }
        _uiStateTts.value = _uiStateTts.value.copy(isLoading = false, error = exception.message)
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
                }
                .onFailure { error ->
                    try {
                        // Decode fallback JSON into your DTO
                        val fallback = Json.decodeFromString<SubjectResultDto>(fallbackSubjectJson)

                        _subjectDetailsState.update {
                            it.copy(
                                subjectDetails = fallback.result?.toDomain(),
                                isLoading = false,
                            )
                        }
                    } catch (e: Exception) {
                        _subjectDetailsState.update {
                            it.copy(
                                error = "Failed to load subject details: ${error.message}",
                                isLoading = false
                            )
                        }
                    }
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
        summarizeLearningInternal(text, apiKey, aiTaskRepository::summarizeLearning)
    }

    fun summarizeLearningUsingGemini(text: String, apiKey: String) {
        summarizeLearningInternal(text, apiKey, aiTaskRepository::summarizeLearningUsingGemini)
    }

    /**
     * Synthesize `subject` into audio and play it on the platform.
     * Uses the aiTaskRepository.synthesizeToBytes which returns Result<Flow<ByteArray>>.
     */
    fun speak(summary: String) {
        if (summary.isBlank()) {
            _uiStateTts.update { it.copy(error = "Text is empty", isLoading = false) }
            return
        }

        _uiStateTts.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                val apiKey = GEMINI_API_KEY
                val result = aiTaskRepository.synthesizeToBytes(summary = summary, apiKey = apiKey)

                result.onSuccess { byteFlow ->
                        collectAndPlay(byteFlow)
                    }
                    .onFailure { err ->
                        _uiStateTts.update { it.copy(isLoading = false, error = err.message) }
                    }
            } catch (e: Exception) {
                _uiStateTts.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private suspend fun collectAndPlay(flow: Flow<ByteArray>) {
        try {
            flow.collectLatest { bytes ->
                _uiStateTts.update { it.copy(isLoading = true, error = null) }

                // call platform playback (suspend)
                playAudioBytes(bytes, "tts_${System.now()}.mp3")

                // after playback completed successfully, update state
                _uiStateTts.update { it.copy(isLoading = false, error = null) }
            }
        } catch (e: Exception) {
            _uiStateTts.update { it.copy(isLoading = false, error = e.message) }
        }
    }

}
