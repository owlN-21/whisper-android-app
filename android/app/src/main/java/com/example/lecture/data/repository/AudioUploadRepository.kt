package com.example.lecture.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.lecture.data.network.ApiService
import com.example.lecture.data.network.NetworkResult
import com.example.lecture.data.network.dto.TaskDto
import com.example.lecture.data.network.safeApiCall
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class AudioUploadRepository(
    private val contentResolver: ContentResolver,
    private val apiService: ApiService
) {

    suspend fun uploadAudio(
        userId: Long,
        fileUri: String,
        fileName: String,
        mimeType: String?
    ): NetworkResult<TaskDto> {
        return try {
            val uri = Uri.parse(fileUri)

            val inputStream = contentResolver.openInputStream(uri)
                ?: return NetworkResult.Error("Не удалось открыть выбранный файл")

            val fileBytes = inputStream.use { stream ->
                stream.readBytes()
            }

            val requestBody = fileBytes.toRequestBody(
                contentType = mimeType?.toMediaTypeOrNull()
                    ?: "audio/*".toMediaTypeOrNull()
            )

            val filePart = MultipartBody.Part.createFormData(
                name = "file",
                filename = fileName,
                body = requestBody
            )

            safeApiCall {
                apiService.createTask(
                    userId = userId,
                    file = filePart
                )
            }
        } catch (exception: Exception) {
            NetworkResult.Error(
                message = "Не удалось подготовить файл к загрузке: ${exception.message}"
            )
        }
    }
}