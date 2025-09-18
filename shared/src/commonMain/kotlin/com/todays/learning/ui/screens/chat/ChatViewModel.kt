package com.todays.learning.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todays.learning.domain.repositories.TimetableRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Ask Me / Chat screen.
 * Exposes simple UI state for streaming answer, loading and error.
 */
class ChatViewModel(
    private val timetableRepository: TimetableRepository
) : ViewModel() {

    // simple UI states for chat
    private val _answer = MutableStateFlow("")
    val answer = _answer.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var askJob: Job? = null

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, exception ->
        _isLoading.value = false
        _error.value = exception.message
    }

    /**
     * askMe: streams response chunks from repository.askMe and updates UI state.
     *
     * - Cancels previous ask job if running.
     * - Clears previous answer before emitting new chunks.
     */
    fun askMe(prompt: String, apiKey: String) {
        // Basic validation
        if (prompt.isBlank()) {
            _error.value = "Prompt is empty"
            return
        }

        // reset UI
        _error.value = null
        _answer.value = ""
        _isLoading.value = true

        askJob?.cancel()
        askJob = viewModelScope.launch(coroutineExceptionHandler) {
            try {
                val result = timetableRepository.askMe(prompt, apiKey)
                result.onSuccess { flow ->
                    // collect streaming chunks and append to answer
                    flow.collectLatest { chunk ->
                        // append chunk to the existing answer
                        _answer.value = _answer.value + chunk
                    }
                    // finished successfully
                    _isLoading.value = false
                }.onFailure { throwable ->
                    _isLoading.value = false
                    _error.value = throwable.message ?: "Unknown error"
                }
            } catch (t: Throwable) {
                _isLoading.value = false
                _error.value = t.message
            }
        }
    }

    /** Cancel any ongoing ask streaming */
    fun cancelAsk() {
        askJob?.cancel()
        _isLoading.value = false
    }

    /** Clear answer & error */
    fun clear() {
        _answer.value = ""
        _error.value = null
    }
}
