package com.todays.learning.ui.screens.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.todays.learning.service.GenerativeAiService
import com.todays.learning.ui.components.ChatBubbleItem
import com.todays.learning.ui.components.MessageInput
import com.todays.learning.ui.components.appbars.AppBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = remember { ChatViewModel(GenerativeAiService.instance) },
    mainPaddingValues: PaddingValues,
) {
    val chatUiState = chatViewModel.uiState
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    Scaffold(
        modifier = Modifier.fillMaxSize().padding(mainPaddingValues),
        topBar = { AppBar("Ask Me") }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Messages List takes remaining space
            ChatList(
                chatMessages = chatUiState.messages,
                listState = listState,
                modifier = Modifier.weight(1f)
            )

            // Message Input stays at bottom
            MessageInput(
                enabled = chatUiState.canSendMessage,
                onSendMessage = { inputText, image ->
                    chatViewModel.sendMessage(inputText, image)
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                    }
                }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose { chatViewModel.onCleared() }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatList(
    chatMessages: List<ChatMessage>,
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    val messages by remember { derivedStateOf { chatMessages.reversed() } }
    LazyColumn(
        modifier = modifier,
        state = listState,
        reverseLayout = true
    ) {
        items(items = messages, key = { it.id }) { message ->
            ChatBubbleItem(message)
        }
    }
}

