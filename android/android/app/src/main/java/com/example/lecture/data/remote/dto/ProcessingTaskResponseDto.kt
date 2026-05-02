package com.example.lecture.data.remote.dto

data class ProcessingTaskResponseDto(
    val id: Long,
    val userId: Long,
    val originalFilename: String,
    val status: String,
    val errorMessage: String?,
    val createdAt: String,
    val updatedAt: String
)