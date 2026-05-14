package com.example.lecture.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "transcripts",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TranscriptEntity(
    @PrimaryKey
    val taskId: Long,

    val text: String,

    val createdAt: Long = System.currentTimeMillis()
)