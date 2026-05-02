package com.example.lecture.feature.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lecture.data.repository.TaskRepository
import com.example.lecture.data.repository.UserRepository

// Создает UploadAudioViewModel и передает в нее нужные зависимости
// (TaskRepository и UserRepository), потому что ViewModel с параметрами
// нельзя создать автоматически без Factory.
class UploadAudioViewModelFactory(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UploadAudioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UploadAudioViewModel(taskRepository, userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}