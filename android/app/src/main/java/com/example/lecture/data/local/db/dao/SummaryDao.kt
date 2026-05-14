package com.example.lecture.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lecture.data.local.db.entity.SummaryEntity

@Dao
interface SummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSummary(summary: SummaryEntity)

    @Query(
        """
        SELECT * FROM summaries
        WHERE taskId = :taskId
        LIMIT 1
        """
    )
    suspend fun getSummaryByTaskId(taskId: Long): SummaryEntity?
}