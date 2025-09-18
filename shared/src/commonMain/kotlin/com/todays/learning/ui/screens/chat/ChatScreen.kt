@file:OptIn(KoinExperimentalAPI::class)

package com.todays.learning.ui.screens.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.todays.learning.ui.components.appbars.AppBar
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    mainPaddingValues: androidx.compose.foundation.layout.PaddingValues,
    viewModel: ChatViewModel = koinViewModel(),
    apiKey: String = "AIzaSyCuVyqh1U1UPeIqdMcHW0FQK4E6eO-zRt0" // pass your local API key in debug; keep empty for prod
) {
    val scope = rememberCoroutineScope()
    var question by remember { mutableStateOf("") }

    val answerState by viewModel.answer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(mainPaddingValues),
        topBar = { AppBar("Ask Me") }
    ) { innerPadding ->

        Column(Modifier
            .fillMaxSize()
            .padding(innerPadding)) {

            TextField(
                value = question,
                onValueChange = { question = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Ask me anything...") }
            )

            Spacer(Modifier.height(8.dp))

            Button(onClick = {
                // launch askMe in ViewModel — streaming answer will update `answerState`
                scope.launch {
                    viewModel.askMe(question, apiKey)
                }
            }) {
                Text(if (isLoading) "Asking..." else "Ask Gemini")
            }

            Spacer(Modifier.height(16.dp))

            // Streaming answer text
            Text(text = "Answer:")
            Spacer(Modifier.height(8.dp))
            Text(text = answerState)

            // Show error if present
            if (!error.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(text = "Error: $error")
            }
        }

    }

}
