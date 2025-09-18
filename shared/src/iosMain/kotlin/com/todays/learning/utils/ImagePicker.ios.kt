package com.todays.learning.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.mohamedrejeb.calf.io.readByteArray
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import kotlinx.cinterop.BetaInteropApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import org.jetbrains.skia.Image

@OptIn(BetaInteropApi::class)
@Composable
actual fun ImagePicker(onResult: (ByteArray?) -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberFilePickerLauncher(
        type = FilePickerFileType.Image,
        selectionMode = FilePickerSelectionMode.Single,
        onResult = { files ->
            coroutineScope.launch(Dispatchers.IO) {
                onResult(files.firstOrNull()?.readByteArray())
            }
        },
    )

    LaunchedEffect(Unit) {
        launcher.launch()
    }
}

actual fun ByteArray.toComposeImageBitmap(): ImageBitmap = Image.makeFromEncoded(this).toComposeImageBitmap()
