
package com.todays.learning.ui.screens.chat

import com.todays.learning.service.GenerativeAiService
import com.todays.learning.utils.toComposeImageBitmap
import dev.shreyaspatil.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class ChatViewModel(aiService: GenerativeAiService) {
    private val coroutineScope = MainScope()

    private val chat = aiService.startChat(
        history = listOf(
            content(role = "user") { text("Hello AI.") },
            content(role = "model") { text("Great to meet you. What would you like to know?") },
        ),
    )

    private val _uiState = MutableChatUiState()
    val uiState: ChatUiState = _uiState

    fun sendMessage(prompt: String, imageBytes: ByteArray?) {
        val completeText = StringBuilder()

        val base = if (imageBytes != null) {
            val content = content {
                image(imageBytes)
                text(prompt)
            }
            chat.sendMessageStream(content)
        } else {
            chat.sendMessageStream(prompt)
        }

        val modelMessage = ModelChatMessage.LoadingModelMessage(
            base.map { it.text ?: "" }
                .onEach { completeText.append(it) }
                .onStart { _uiState.canSendMessage = false }
                .onCompletion {
                    _uiState.setLastModelMessageAsLoaded(completeText.toString())
                    _uiState.canSendMessage = true
                }
                .catch { _uiState.setLastMessageAsError(it.toString()) },
        )

        coroutineScope.launch(Dispatchers.Default) {
            _uiState.addMessage(UserChatMessage(prompt, imageBytes?.toComposeImageBitmap()))
            _uiState.addMessage(modelMessage)
        }
    }

    fun onCleared() {
        println("ChatViewModel: onCleared")
        coroutineScope.cancel()
    }
}
