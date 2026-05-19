package com.example.lecture.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "summaries",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SummaryEntity(
    @PrimaryKey
    val taskId: Long,

    val content: String,

    val createdAt: Long = System.currentTimeMillis()
)