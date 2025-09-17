package org.todays.learning

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.todays.learning.di.initKoin
import com.todays.learning.ui.screens.main.MainScreen
import org.koin.core.Koin

lateinit var koin: Koin

fun main() {
    koin = initKoin(enableNetworkLogs = true).koin

    return application {
        Thread.currentThread().contextClassLoader = this.javaClass.classLoader

        Window(
            onCloseRequest = { this.exitApplication() },
            title = "Todays Learning",
            state = rememberWindowState(
                position = WindowPosition.Aligned(Alignment.Center),
                width = 1080.dp,
                height = 800.dp,
            )
        ) {
            MainScreen()
        }
    }
}