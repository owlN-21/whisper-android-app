package com.example.lecture

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.lecture.data.local.db.entity.SummaryEntity
import com.example.lecture.data.local.db.entity.TaskEntity
import com.example.lecture.data.local.db.entity.TranscriptEntity
import com.example.lecture.ui.navigation.AppNavGraph
import com.example.lecture.ui.theme.LectureTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        testRoomDatabase()

        setContent {
            LectureTheme {
                AppNavGraph()
            }
        }
    }
    private fun testRoomDatabase() {
        val database = (application as App).database

        lifecycleScope.launch {
            val taskDao = database.taskDao()
            val summaryDao = database.summaryDao()
            val transcriptDao = database.transcriptDao()

            val taskId = taskDao.insertTask(
                TaskEntity(
                    userId = 1L,
                    originalFileName = "test_audio.mp3",
                    localFileUri = "content://test/audio.mp3",
                    status = "LOCAL_CREATED"
                )
            )

            Log.d("RoomTest", "Created task id = $taskId")

            val tasks = taskDao.getTasksByUserId(1L).first()
            Log.d("RoomTest", "Tasks count = ${tasks.size}")
            Log.d("RoomTest", "First task = ${tasks.firstOrNull()}")

            taskDao.updateTaskStatus(
                taskId = taskId,
                status = "COMPLETED"
            )

            val updatedTask = taskDao.getTaskById(taskId)
            Log.d("RoomTest", "Updated task = $updatedTask")

            summaryDao.saveSummary(
                SummaryEntity(
                    taskId = taskId,
                    content = "Это тестовый конспект лекции."
                )
            )

            taskDao.updateSummaryPreview(
                taskId = taskId,
                summaryPreview = "Это тестовый конспект лекции."
            )

            val summary = summaryDao.getSummaryByTaskId(taskId)
            Log.d("RoomTest", "Summary = $summary")

            transcriptDao.saveTranscript(
                TranscriptEntity(
                    taskId = taskId,
                    text = "Это тестовая расшифровка аудио."
                )
            )

            val transcript = transcriptDao.getTranscriptByTaskId(taskId)
            Log.d("RoomTest", "Transcript = $transcript")

            taskDao.deleteTaskById(taskId)

            val tasksAfterDelete = taskDao.getTasksByUserId(1L).first()
            Log.d("RoomTest", "Tasks count after delete = ${tasksAfterDelete.size}")
        }
    }
}