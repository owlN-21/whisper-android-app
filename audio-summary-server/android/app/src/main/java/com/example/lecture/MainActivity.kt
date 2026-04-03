package com.example.lecture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.lecture.data.local.UserSessionStorage
import com.example.lecture.data.remote.NetworkModule
import com.example.lecture.data.repository.TaskRepository
import com.example.lecture.ui.screen.UploadAudioScreen
import com.example.lecture.ui.theme.LectureTheme
import com.example.lecture.ui.viewmodel.UploadAudioViewModel
import com.example.lecture.ui.viewmodel.UploadAudioViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userSessionStorage = UserSessionStorage(applicationContext)
        val taskRepository = TaskRepository(
            context = applicationContext,
            taskApi = NetworkModule.taskApi
        )

        val uploadAudioViewModel = ViewModelProvider(
            this,
            UploadAudioViewModelFactory(taskRepository, userSessionStorage)
        )[UploadAudioViewModel::class.java]

        setContent {
            LectureTheme {
                UploadAudioScreen(uploadAudioViewModel = uploadAudioViewModel)
            }
        }
    }
}