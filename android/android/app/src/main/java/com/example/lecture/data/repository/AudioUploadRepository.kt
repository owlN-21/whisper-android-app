package com.example.lecture.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.lecture.data.network.ApiService
import com.example.lecture.data.network.NetworkResult
import com.example.lecture.data.network.dto.SummaryDto
import com.example.lecture.data.network.dto.TaskDto
import com.example.lecture.data.network.dto.TranscriptDto
import com.example.lecture.data.network.safeApiCall
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.lang.SecurityException

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
        if (fileUri.isBlank()) {
            return NetworkResult.Error(
                message = "Не удалось получить путь к выбранному файлу"
            )
        }

        if (fileName.isBlank()) {
            return NetworkResult.Error(
                message = "Не удалось получить имя выбранного файла"
            )
        }

        return try {
            val uri = Uri.parse(fileUri)

            val inputStream = contentResolver.openInputStream(uri)
                ?: return NetworkResult.Error(
                    message = "Не удалось открыть выбранный файл"
                )

            val fileBytes = inputStream.use { stream ->
                stream.readBytes()
            }

            if (fileBytes.isEmpty()) {
                return NetworkResult.Error(
                    message = "Выбранный файл пустой"
                )
            }

            val requestBody = fileBytes.toRequestBody(
                contentType = mimeType?.toMediaTypeOrNull()
                    ?: DEFAULT_AUDIO_MEDIA_TYPE.toMediaTypeOrNull()
            )

            val filePart = MultipartBody.Part.createFormData(
                name = FILE_PART_NAME,
                filename = fileName,
                body = requestBody
            )

            safeApiCall {
                apiService.createTask(
                    userId = userId,
                    file = filePart
                )
            }
        } catch (exception: SecurityException) {
            NetworkResult.Error(
                message = "Нет доступа к выбранному файлу. Выберите файл еще раз"
            )
        } catch (exception: IOException) {
            NetworkResult.Error(
                message = "Не удалось прочитать выбранный файл"
            )
        } catch (exception: Exception) {
            NetworkResult.Error(
                message = "Не удалось подготовить файл к загрузке"
            )
        }
    }

    suspend fun getUserTasks(
        userId: Long
    ): NetworkResult<List<TaskDto>> {
        return safeApiCall {
            apiService.getUserTasks(userId)
        }
    }

    suspend fun getTaskStatus(
        taskId: Long
    ): NetworkResult<TaskDto> {
        return safeApiCall {
            apiService.getTask(taskId)
        }
    }

    suspend fun getTranscript(
        taskId: Long
    ): NetworkResult<TranscriptDto> {
        return safeApiCall {
            apiService.getTranscript(taskId)
        }
    }

    suspend fun getSummary(
        taskId: Long
    ): NetworkResult<SummaryDto> {
        return safeApiCall {
            apiService.getSummary(taskId)
        }
    }

    suspend fun deleteTask(taskId: Long): NetworkResult<Unit> {
        return try {
            val response = apiService.deleteTask(taskId)

            if (response.isSuccessful) {
                NetworkResult.Success(Unit)
            } else {
                val errorBody = response.errorBody()?.string()

                NetworkResult.Error(
                    message = errorBody ?: "Ошибка удаления задачи: ${response.code()}",
                    code = response.code()
                )
            }
        } catch (e: Exception) {
            NetworkResult.Error(
                message = e.message ?: "Не удалось удалить задачу"
            )
        }
    }

    private companion object {
        const val FILE_PART_NAME = "file"
        const val DEFAULT_AUDIO_MEDIA_TYPE = "audio/*"
    }
}