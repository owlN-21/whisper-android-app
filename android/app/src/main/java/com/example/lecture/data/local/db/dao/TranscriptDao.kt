package com.example.lecture.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lecture.data.local.db.entity.TranscriptEntity

@Dao
interface TranscriptDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTranscript(transcript: TranscriptEntity)

    @Query(
        """
        SELECT * FROM transcripts
        WHERE taskId = :taskId
        LIMIT 1
        """
    )
    suspend fun getTranscriptByTaskId(taskId: Long): TranscriptEntity?
}