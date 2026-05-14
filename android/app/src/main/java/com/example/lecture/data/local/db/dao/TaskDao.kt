package com.example.lecture.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.lecture.data.local.db.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Query(
        """
        SELECT * FROM tasks
        WHERE userId = :userId
        ORDER BY createdAt DESC
        """
    )
    fun getTasksByUserId(userId: Long): Flow<List<TaskEntity>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE id = :taskId
        LIMIT 1
        """
    )
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Query(
        """
        UPDATE tasks
        SET status = :status,
            updatedAt = :updatedAt
        WHERE id = :taskId
        """
    )
    suspend fun updateTaskStatus(
        taskId: Long,
        status: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query(
        """
        UPDATE tasks
        SET errorMessage = :errorMessage,
            status = :status,
            updatedAt = :updatedAt
        WHERE id = :taskId
        """
    )
    suspend fun updateTaskError(
        taskId: Long,
        errorMessage: String,
        status: String = "FAILED",
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query(
        """
        UPDATE tasks
        SET summaryPreview = :summaryPreview,
            updatedAt = :updatedAt
        WHERE id = :taskId
        """
    )
    suspend fun updateSummaryPreview(
        taskId: Long,
        summaryPreview: String,
        updatedAt: Long = System.currentTimeMillis()
    )

    @Query(
        """
        DELETE FROM tasks
        WHERE id = :taskId
        """
    )
    suspend fun deleteTaskById(taskId: Long)
}