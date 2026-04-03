package com.example.lecture.ui.viewmodel

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lecture.data.local.UserSessionStorage
import com.example.lecture.data.repository.TaskRepository
import kotlinx.coroutines.launch


class UploadAudioViewModel(
    private val taskRepository: TaskRepository,
    private val userSessionStorage: UserSessionStorage
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var message by mutableStateOf("")
        private set

    fun uploadAudio(fileUri: Uri) {
        val userId = userSessionStorage.getUserId()

        if (userId == null) {
            message = "Пользователь не найден. Сначала выполните вход."
            return
        }

        viewModelScope.launch {
            isLoading = true

            val result = taskRepository.uploadAudio(userId, fileUri)

            result.onSuccess { task ->
                message = "taskId=${task.id}, status=${task.status}, file=${task.originalFilename}"
            }

            result.onFailure { error ->
                message = error.message ?: "Ошибка загрузки файла"
            }

            isLoading = false
        }
    }
}