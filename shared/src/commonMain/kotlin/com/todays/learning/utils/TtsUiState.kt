package com.todays.learning.utils

/**
 * UI state for TTS
 */
data class TtsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastAudioBase64: String? = null // base64 audio returned from API (MP3)
)
