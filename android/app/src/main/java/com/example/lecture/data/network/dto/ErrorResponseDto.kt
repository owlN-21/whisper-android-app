package com.example.lecture.data.network.dto

data class ErrorResponseDto(
    val message: String? = null,
    val error: String? = null,
    val status: Int? = null,
    val code: String? = null
)