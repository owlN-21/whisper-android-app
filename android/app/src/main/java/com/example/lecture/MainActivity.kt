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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            val result = safeApiCall {
                NetworkModule.apiService.getHealth()
            }

            when (result) {
                is NetworkResult.Success -> {
                    Log.d("HealthCheck", "Backend is available: ${result.data}")
                }

                is NetworkResult.Error -> {
                    Log.e("HealthCheck", "Backend error: ${result.message}")
                }

                NetworkResult.Loading -> Unit
            }
        }

        setContent {
            LectureTheme {
                AppNavGraph()
            }
        }
    }
}