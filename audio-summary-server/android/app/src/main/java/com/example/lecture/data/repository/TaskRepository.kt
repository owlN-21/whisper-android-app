package com.example.lecture.data.repository

import android.content.Context
import android.net.Uri
import com.example.lecture.data.remote.api.TaskApi
import com.example.lecture.data.remote.dto.ProcessingTaskResponseDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class TaskRepository(
    private val context: Context,
    private val taskApi: TaskApi
) {

    suspend fun uploadAudio(userId: Long, fileUri: Uri): Result<ProcessingTaskResponseDto> {
        return try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: return Result.failure(Exception("Не удалось открыть файл"))

            val tempFile = File.createTempFile("upload_", ".tmp", context.cacheDir)

            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val requestBody = tempFile
                .asRequestBody("audio/*".toMediaTypeOrNull())

            val multipartBody = MultipartBody.Part.createFormData(
                "file",
                tempFile.name,
                requestBody
            )

            val response = taskApi.uploadAudio(userId, multipartBody)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(Exception("Пустой ответ от сервера"))
                }
            } else {
                Result.failure(Exception("Ошибка загрузки. Код: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}