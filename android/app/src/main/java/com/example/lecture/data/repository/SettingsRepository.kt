package com.example.lecture.data.repository

import com.example.lecture.data.network.ApiService
import com.example.lecture.data.network.NetworkResult
import com.example.lecture.data.network.dto.HealthResponseDto
import com.example.lecture.data.network.safeApiCall

class SettingsRepository(
    private val apiService: ApiService
) {

    suspend fun checkHealth(): NetworkResult<HealthResponseDto> {
        return safeApiCall {
            apiService.getHealth()
        }
    }
}