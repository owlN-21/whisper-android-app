package com.example.lecture.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    val remoteTaskId: Long? = null,

    val userId: Long,

    val originalFileName: String,

    val localFileUri: String? = null,

    val status: String,

    val errorMessage: String? = null,

    val summaryPreview: String? = null,

    val createdAt: Long = System.currentTimeMillis(),

    val updatedAt: Long = System.currentTimeMillis()
)