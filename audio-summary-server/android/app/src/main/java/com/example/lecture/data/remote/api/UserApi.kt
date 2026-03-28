package com.example.lecture.data.remote.api

import com.example.lecture.data.remote.dto.CreateUserRequest
import com.example.lecture.data.remote.dto.UserResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface UserApi {

    @GET("api/v1/users/by-email")
    suspend fun getUserByEmail(
        @Query("email") email: String
    ): Response<UserResponseDto>

    @POST("api/v1/users")
    suspend fun createUser(
        @Body request: CreateUserRequest
    ): Response<UserResponseDto>
}