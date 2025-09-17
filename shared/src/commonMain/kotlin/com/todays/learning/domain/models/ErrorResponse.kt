package com.todays.learning.domain.models

data class ErrorResponse(
    val success: Boolean,
    val statusCode: Int,
    val statusMessage: String
) : Exception()
