package com.example.lecture.data.network.dto

data class TranscriptDto(
    val taskId: Long,
    val status: String,
    val transcript: String?
)