package com.todays.learning.utils

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.Foundation.NSData
import platform.Foundation.NSFileManager
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.create
import platform.Foundation.writeToFile
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalForeignApi::class)
actual suspend fun playAudioBytes(bytes: ByteArray, filename: String) =
    suspendCancellableCoroutine { cont ->
        try {
            // write bytes to temp file via NSData
            val tmpDir = NSTemporaryDirectory() ?: "/tmp"
            val filePath = "$tmpDir/$filename"

            // Convert ByteArray -> NSData safely using pinned memory
            val nsData = bytes.toNSData()
            val wrote = nsData.writeToFile(filePath, true)
            if (!wrote) {
                cont.resumeWithException(Exception("Failed to write TTS file to $filePath"))
                return@suspendCancellableCoroutine
            }

            val fileUrl = NSURL.fileURLWithPath(filePath)
            val item = AVPlayerItem(fileUrl)
            val player = AVPlayer(playerItem = item)

            // Observe end of playback
            val observer = NSNotificationCenter.defaultCenter.addObserverForName(
                name = platform.AVFoundation.AVPlayerItemDidPlayToEndTimeNotification,
                `object` = item,
                queue = null
            ) { _notification: NSNotification? ->
                // Playback finished
                if (cont.isActive) {
                    try {
                        // cleanup file
                        try { NSFileManager.defaultManager.removeItemAtPath(filePath, null) } catch (_: Throwable) {}
                    } finally {
                        cont.resume(Unit)
                    }
                }
            }

            // Error observation: AVPlayerItem.status could be checked, but simpler to observe failedToPlayToEndTime notification
            val failedObserver = NSNotificationCenter.defaultCenter.addObserverForName(
                name = platform.AVFoundation.AVPlayerItemFailedToPlayToEndTimeNotification,
                `object` = item,
                queue = null
            ) { notif: NSNotification? ->
                if (cont.isActive) {
                    val errDesc = notif?.userInfo?.get("NSError")?.toString() ?: "Playback failed"
                    try { NSFileManager.defaultManager.removeItemAtPath(filePath, null) } catch (_: Throwable) {}
                    cont.resumeWithException(Exception(errDesc))
                }
            }

            // Start playback
            //player.rate = 1.0f

            // Handle cancellation: stop player, remove observers, cleanup file
            cont.invokeOnCancellation {
                try {
                    //player.pause()
                } catch (_: Throwable) {}
                try {
                    NSNotificationCenter.defaultCenter.removeObserver(observer)
                    NSNotificationCenter.defaultCenter.removeObserver(failedObserver)
                } catch (_: Throwable) {}
                try { NSFileManager.defaultManager.removeItemAtPath(filePath, null) } catch (_: Throwable) {}
            }
        } catch (t: Throwable) {
            if (cont.isActive) cont.resumeWithException(t)
        }
    }

/**
 * Convert ByteArray to NSData using pinned memory (safe).
 */
@OptIn(ExperimentalForeignApi::class)
fun ByteArray.toNSData(): NSData {
    // usePinned gives us a stable pointer to the ByteArray memory for the duration of the block
    return this.usePinned { pinned ->
        val ptr: CPointer<ByteVar> = pinned.addressOf(0)
        NSData.create(bytes = ptr, length = this.size.toULong())
    }
}
