package com.todays.learning.utils

import android.content.Context
import android.media.MediaPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Provide a way to set app context from Android entrypoint (Application.onCreate)
private var appContext: Context? = null

fun initTtsPlayerAndroid(context: Context) {
    appContext = context.applicationContext
}

actual suspend fun playAudioBytes(bytes: ByteArray, filename: String) {
    val ctx = appContext
        ?: throw IllegalStateException("TTS player not initialized. Call initTtsPlayerAndroid(context) first.")
    // write to temp file on IO
    val file = withContext(Dispatchers.IO) {
        File.createTempFile(filename, null, ctx.cacheDir).apply {
            deleteOnExit()
            FileOutputStream(this).use { it.write(bytes) }
        }
    }

    // play audio and suspend until completion or error
    return suspendCancellableCoroutine { cont ->
        try {
            val player = MediaPlayer()
            player.setDataSource(file.absolutePath)
            player.setOnPreparedListener {
                it.start()
            }
            player.setOnCompletionListener {
                try {
                    it.release()
                } catch (_: Throwable) {
                }
                if (cont.isActive) cont.resume(Unit)
            }
            player.setOnErrorListener { mp, what, extra ->
                try {
                    mp.release()
                } catch (_: Throwable) {
                }
                if (cont.isActive) cont.resumeWithException(Exception("Playback error: what=$what extra=$extra"))
                true
            }
            player.prepareAsync()

            cont.invokeOnCancellation {
                try {
                    player.stop(); player.release()
                } catch (_: Throwable) {
                }
            }
        } catch (t: Throwable) {
            if (cont.isActive) cont.resumeWithException(t)
        }
    }
}
