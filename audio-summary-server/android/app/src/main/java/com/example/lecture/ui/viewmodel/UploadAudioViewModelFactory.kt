package com.example.lecture.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lecture.data.local.UserSessionStorage
import com.example.lecture.data.repository.TaskRepository


class UploadAudioViewModelFactory(
    private val taskRepository: TaskRepository,
    private val userSessionStorage: UserSessionStorage
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UploadAudioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UploadAudioViewModel(taskRepository, userSessionStorage) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}