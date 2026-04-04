package com.example.lecture.data.repository

import android.content.Context
import android.net.Uri
import com.example.lecture.data.remote.api.TaskApi
import com.example.lecture.data.remote.dto.ProcessingTaskResponseDto
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import android.provider.OpenableColumns

class TaskRepository(
    private val context: Context,
    private val taskApi: TaskApi
) {

    suspend fun uploadAudio(userId: Long, fileUri: Uri): Result<ProcessingTaskResponseDto> {
        var tempFile: File? = null

        return try {
            val inputStream = context.contentResolver.openInputStream(fileUri)
                ?: return Result.failure(Exception("Не удалось открыть файл"))

            val originalFileName = getFileName(fileUri) ?: "audio_file"
            val mimeType = context.contentResolver.getType(fileUri) ?: "application/octet-stream"
            val extension = originalFileName.substringAfterLast('.', "")

            tempFile = if (extension.isNotBlank()) {
                File.createTempFile("upload_", ".$extension", context.cacheDir)
            } else {
                File.createTempFile("upload_", null, context.cacheDir)
            }

            inputStream.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val requestBody = tempFile.asRequestBody(mimeType.toMediaTypeOrNull())

            val multipartBody = MultipartBody.Part.createFormData(
                "file",
                originalFileName,
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
        } finally {
            tempFile?.delete()
        }
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    return it.getString(index)
                }
            }
        }

        return null
    }
}