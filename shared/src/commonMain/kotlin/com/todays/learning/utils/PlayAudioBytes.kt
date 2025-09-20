package com.todays.learning.utils

/**
 * Platform-specific playback of raw audio bytes (MP3).
 *
 * Implementations should block (suspend) until playback completes or throw on error.
 *
 * @param bytes audio bytes (MP3 recommended)
 * @param filename desired temporary filename (platform-specific)
 */
expect suspend fun playAudioBytes(bytes: ByteArray, filename: String = "tts_output.mp3")
