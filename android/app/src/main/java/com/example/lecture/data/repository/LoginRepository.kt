package com.example.lecture.data.repository

import android.util.Log
import com.example.lecture.data.network.ApiService
import com.example.lecture.data.network.NetworkResult
import com.example.lecture.data.network.dto.CreateUserRequestDto
import com.example.lecture.data.network.dto.UserDto
import com.example.lecture.data.network.safeApiCall

class LoginRepository(
    private val apiService: ApiService
) {

    suspend fun loginOrRegister(email: String): NetworkResult<UserDto> {
        val existingUserResult = safeApiCall {
            apiService.getUserByEmail(email)
        }

        return when (existingUserResult) {
            is NetworkResult.Success -> {
                Log.d(
                    "LoginRepository",
                    "User found."
                )
                existingUserResult
            }

            is NetworkResult.Error -> {
                if (existingUserResult.code == 404) {
                    Log.d("LoginRepository", "User not found. Creating new user.")
                    safeApiCall {
                        apiService.createUser(
                            CreateUserRequestDto(email = email)
                        )
                    }
                } else {
                    existingUserResult
                }
            }
        }
    }
}