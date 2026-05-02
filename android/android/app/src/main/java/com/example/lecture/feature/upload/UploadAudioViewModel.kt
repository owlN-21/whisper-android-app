package com.example.lecture.feature.upload

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lecture.data.repository.TaskRepository
import com.example.lecture.data.repository.UserRepository
import kotlinx.coroutines.launch

class UploadAudioViewModel(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var message by mutableStateOf("")
        private set

    fun uploadAudio(fileUri: Uri) {
        val userId = userRepository.getSavedUserId()

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