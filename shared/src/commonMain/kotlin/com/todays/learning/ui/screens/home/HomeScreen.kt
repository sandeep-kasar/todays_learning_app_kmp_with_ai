package com.todays.learning.ui.screens.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import com.todays.learning.ui.components.appbars.AppBar
import com.todays.learning.ui.composables.LazyTimeline
import com.todays.learning.utils.WindowSize
import com.kmpalette.loader.rememberNetworkLoader
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    navigator: NavHostController,
    windowSize: WindowSize = WindowSize.COMPACT,
    viewModel: HomeViewModel = koinViewModel<HomeViewModel>(),
    mainPaddingValues: PaddingValues
) {
    val scrollState = rememberScrollState()

    val homeUiState = viewModel.homeUiState.collectAsState().value

    val networkLoader = rememberNetworkLoader(httpClient = koinInject())

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(mainPaddingValues),
        topBar = { AppBar("Todays Learning") }
    ) { paddingValues ->

        // region Home section
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (homeUiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (!homeUiState.error.isNullOrEmpty()) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "Error:\n${homeUiState.error}",
                    textAlign = TextAlign.Center
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                ) {
                    LazyTimeline(
                        navigator = navigator,
                        timeTableList = homeUiState.todaysTimetable ?: arrayListOf()
                    )
                }
            }
            // endregion
        }
    }
}
