package com.example.lecture.data.network.dto

data class SummaryDto(
    val taskId: Long,
    val status: String,
    val summary: String?
)