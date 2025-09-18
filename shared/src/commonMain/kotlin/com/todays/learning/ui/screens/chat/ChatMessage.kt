package com.todays.learning.ui.screens.chat

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import com.todays.learning.utils.getUUIDString
import kotlinx.coroutines.flow.Flow

/**
 * Represents as a Chat message model
 */
@Immutable
sealed interface ChatMessage {

    val id: String

    fun isUserMessage(): Boolean = this is UserChatMessage

    fun isModelMessage(): Boolean = this is ModelChatMessage

    fun isErrorMessage(): Boolean = this is ModelChatMessage.ErrorMessage
}

/**
 * User prompt message model
 */
@Immutable
data class UserChatMessage(
    val text: String,
    val image: ImageBitmap?,
    override val id: String = "user-${getUUIDString()}",
) : ChatMessage

/**
 * Model's response chat model
 */
@Immutable
sealed interface ModelChatMessage : ChatMessage {

    /**
     * Represents Loading state of a model message
     */
    @Immutable
    data class LoadingModelMessage(
        val textStream: Flow<String>,
        override val id: String = "modelloading-${getUUIDString()}",
    ) : ModelChatMessage

    /**
     * Represents loaded state of a model message
     */
    @Immutable
    data class LoadedModelMessage(
        val text: String,
        override val id: String = "model-${getUUIDString()}",
    ) : ModelChatMessage

    /**
     * Represents error state of a model message
     */
    @Immutable
    data class ErrorMessage(
        val text: String,
        override val id: String = "error-${getUUIDString()}",
    ) : ChatMessage
}
