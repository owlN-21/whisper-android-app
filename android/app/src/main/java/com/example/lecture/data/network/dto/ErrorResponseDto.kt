package com.example.lecture.data.network.dto

data class ErrorResponseDto(
    val message: String?,
    val code: String? = null
)