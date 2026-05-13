package com.example.lecture

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.lecture.data.network.NetworkModule
import com.example.lecture.data.network.NetworkResult
import com.example.lecture.data.network.safeApiCall
import com.example.lecture.ui.navigation.AppNavGraph
import com.example.lecture.ui.theme.LectureTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setupPreferencesRepository()

//        lifecycleScope.launch {
//            val result = safeApiCall {
//                NetworkModule.apiService.getHealth()
//            }
//
//            when (result) {
//                is NetworkResult.Success -> {
//                    Log.d("HealthCheck", "Backend is available: ${result.data}")
//                }
//
//                is NetworkResult.Error -> {
//                    Log.e("HealthCheck", "Backend error: ${result.message}")
//                }
//
//                NetworkResult.Loading -> Unit
//            }
//        }

        setContent {
            LectureTheme {
                AppNavGraph()
            }
        }
    }

    private fun setupPreferencesRepository(){
        val userPreferencesRepository =
            (application as App).userPreferencesRepository

        lifecycleScope.launch {
            userPreferencesRepository.saveUser(
                userId = 1L,
                email = "test@example.com"
            )

            val userId = userPreferencesRepository.getUserId().first()
            val email = userPreferencesRepository.getEmail().first()
            val isLoggedIn = userPreferencesRepository.isLoggedIn().first()

            Log.d("UserPreferencesTest", "userId = $userId")
            Log.d("UserPreferencesTest", "email = $email")
            Log.d("UserPreferencesTest", "isLoggedIn = $isLoggedIn")

            userPreferencesRepository.clearUser()

            Log.d("UserPreferencesTest", "After clear:")
            Log.d("UserPreferencesTest", "userId = ${userPreferencesRepository.getUserId().first()}")
            Log.d("UserPreferencesTest", "email = ${userPreferencesRepository.getEmail().first()}")
            Log.d("UserPreferencesTest", "isLoggedIn = ${userPreferencesRepository.isLoggedIn().first()}")
        }
    }
}