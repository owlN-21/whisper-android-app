package com.example.lecture.data.network.dto

data class TaskDto(
    val id: Long,
    val userId: Long,
    val fileName: String?,
    val status: String,
    val createdAt: String?
)