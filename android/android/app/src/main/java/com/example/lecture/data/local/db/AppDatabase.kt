package com.example.lecture.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.lecture.data.local.db.dao.SummaryDao
import com.example.lecture.data.local.db.dao.TaskDao
import com.example.lecture.data.local.db.dao.TranscriptDao
import com.example.lecture.data.local.db.entity.SummaryEntity
import com.example.lecture.data.local.db.entity.TaskEntity
import com.example.lecture.data.local.db.entity.TranscriptEntity

@Database(
    entities = [
        TaskEntity::class,
        SummaryEntity::class,
        TranscriptEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    abstract fun summaryDao(): SummaryDao

    abstract fun transcriptDao(): TranscriptDao
}