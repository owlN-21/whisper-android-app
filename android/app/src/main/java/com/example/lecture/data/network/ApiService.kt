package com.example.lecture.data.network

import com.example.lecture.data.network.dto.CreateTaskRequestDto
import com.example.lecture.data.network.dto.CreateUserRequestDto
import com.example.lecture.data.network.dto.HealthResponseDto
import com.example.lecture.data.network.dto.SummaryDto
import com.example.lecture.data.network.dto.TaskDto
import com.example.lecture.data.network.dto.TranscriptDto
import com.example.lecture.data.network.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("/health")
    suspend fun getHealth(): Response<HealthResponseDto>

    @GET("/api/v1/users/by-email")
    suspend fun getUserByEmail(
        @Query("email") email: String
    ): Response<UserDto>

    @POST("/api/v1/users")
    suspend fun createUser(
        @Body request: CreateUserRequestDto
    ): Response<UserDto>

    @GET("/api/v1/users/{userId}/tasks")
    suspend fun getUserTasks(
        @Path("userId") userId: Long
    ): Response<List<TaskDto>>

    @POST("/api/v1/users/{userId}/tasks")
    suspend fun createTask(
        @Path("userId") userId: Long,
        @Body request: CreateTaskRequestDto
    ): Response<TaskDto>

    @GET("/api/v1/tasks/{taskId}")
    suspend fun getTask(
        @Path("taskId") taskId: Long
    ): Response<TaskDto>

    @GET("/api/v1/tasks/{taskId}/transcript")
    suspend fun getTranscript(
        @Path("taskId") taskId: Long
    ): Response<TranscriptDto>

    @GET("/api/v1/tasks/{taskId}/result")
    suspend fun getSummary(
        @Path("taskId") taskId: Long
    ): Response<SummaryDto>

    @DELETE("/api/v1/tasks/{taskId}")
    suspend fun deleteTask(
        @Path("taskId") taskId: Long
    ): Response<Unit>
}