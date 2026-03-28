package com.example.lecture

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.lecture.data.local.UserSessionStorage
import com.example.lecture.data.remote.NetworkModule
import com.example.lecture.data.repository.UserRepository
import com.example.lecture.ui.screen.LoginScreen
import com.example.lecture.ui.theme.LectureTheme
import com.example.lecture.ui.viewmodel.LoginViewModel
import com.example.lecture.ui.viewmodel.LoginViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userSessionStorage = UserSessionStorage(applicationContext)
        val userRepository = UserRepository(
            userApi = NetworkModule.userApi,
            userSessionStorage = userSessionStorage
        )

        val loginViewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(userRepository)
        )[LoginViewModel::class.java]

        setContent {
            LectureTheme {
                LoginScreen(loginViewModel = loginViewModel)
            }
        }
    }
}