package com.todays.learning.service

import com.todays.learning.domain.utils.Constants.GEMINI_API_KEY
import dev.shreyaspatil.ai.client.generativeai.Chat
import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.Content

/**
 * Service for Generative AI operations that can interact with text as well as images.
 */
class GenerativeAiService private constructor(
    private val visionModel: GenerativeModel,
) {

    /**
     * Creates a chat instance which internally tracks the ongoing conversation with the model
     *
     * @param history History of conversation
     */
    fun startChat(history: List<Content>): Chat = visionModel.startChat(history)

    companion object {
        @Suppress("ktlint:standard:property-naming")

        val instance: GenerativeAiService by lazy {
            GenerativeAiService(
                visionModel = GenerativeModel(
                    modelName = "gemini-2.0-flash",
                    apiKey = GEMINI_API_KEY,
                ),
            )
        }
    }
}
