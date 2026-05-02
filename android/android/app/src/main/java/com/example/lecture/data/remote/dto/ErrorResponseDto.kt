package com.example.lecture.data.remote.dto

data class ErrorResponseDto(
    val code: String,
    val message: String,
    val timestamp: String
)