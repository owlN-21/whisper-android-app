package com.example.lecture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.lecture.data.local.UserSessionStorage
import com.example.lecture.data.remote.NetworkModule
import com.example.lecture.data.repository.TaskRepository
import com.example.lecture.data.repository.UserRepository
import com.example.lecture.feature.upload.UploadAudioScreen
import com.example.lecture.ui.theme.LectureTheme
import com.example.lecture.feature.auth.LoginViewModel
import com.example.lecture.feature.auth.LoginViewModelFactory
import com.example.lecture.feature.upload.UploadAudioViewModel
import com.example.lecture.feature.upload.UploadAudioViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userSessionStorage = UserSessionStorage(applicationContext)

        val userRepository = UserRepository(
            userApi = NetworkModule.userApi,
            userSessionStorage = userSessionStorage
        )
        
        val taskRepository = TaskRepository(
            context = applicationContext,
            taskApi = NetworkModule.taskApi
        )

        val uploadAudioViewModel = ViewModelProvider(
            this,
            UploadAudioViewModelFactory(taskRepository, userRepository)
        )[UploadAudioViewModel::class.java]

        val loginViewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(userRepository)
        )[LoginViewModel::class.java]

        setContent {
            LectureTheme {
//                LoginScreen(loginViewModel = loginViewModel)
                UploadAudioScreen(uploadAudioViewModel = uploadAudioViewModel)
            }
        }
    }
}