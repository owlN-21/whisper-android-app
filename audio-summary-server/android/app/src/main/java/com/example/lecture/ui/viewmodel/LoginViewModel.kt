package com.example.lecture.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lecture.data.repository.UserRepository
import kotlinx.coroutines.launch


class LoginViewModel(
    private val userRepository: UserRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var message by mutableStateOf("")
        private set

    fun login(email: String) {
        if (email.isBlank()) {
            message = "Введите email"
            return
        }

        viewModelScope.launch {
            isLoading = true

            val result = userRepository.loginOrCreateUser(email)

            result.onSuccess { user ->
                message = "Успех. userId = ${user.id}, email = ${user.email}"
            }

            result.onFailure { error ->
                message = error.message ?: "Неизвестная ошибка"
            }

            isLoading = false
        }
    }
}