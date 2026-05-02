package com.example.lecture.data.remote.api

import com.example.lecture.data.remote.dto.ProcessingTaskResponseDto
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface TaskApi {

    @Multipart
    @POST("api/users/{userId}/tasks")
    suspend fun uploadAudio(
        @Path("userId") userId: Long,
        @Part file: MultipartBody.Part
    ): Response<ProcessingTaskResponseDto>
}