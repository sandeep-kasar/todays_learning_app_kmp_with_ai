package com.todays.learning.domain.repositories

import kotlinx.coroutines.flow.Flow

interface AiTaskRepository {

    suspend fun summarizeLearning(
        subject: String,
        apiKey: String,
    ): Result<Flow<String>>

    suspend fun summarizeLearningUsingGemini(
        subject: String,
        apiKey: String,
    ): Result<Flow<String>>

    suspend fun synthesizeToBytes(
        summary: String,
        apiKey: String,
    ): Result<Flow<ByteArray>>


}