package com.example.lecture.ui.screens.login

import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lecture.data.local.datastore.UserPreferencesRepository
import com.example.lecture.data.network.NetworkResult
import com.example.lecture.data.repository.LoginRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginRepository: LoginRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.update {
            it.copy(
                email = email,
                errorMessage = null
            )
        }
    }

    fun onContinueClick() {
        val currentState = uiState.value

        if (currentState.isLoading) {
            return
        }

        val email = currentState.email.trim()

        if (email.isBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Введите email")
            }
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _uiState.update {
                it.copy(errorMessage = "Введите корректный email")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    email = email,
                    isLoading = true,
                    errorMessage = null
                )
            }

            when (val result = loginRepository.loginOrRegister(email)) {
                is NetworkResult.Success -> {
                    val user = result.data

                    if (user.email.isBlank()) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Сервер вернул некорректные данные пользователя"
                            )
                        }
                        return@launch
                    }

                    userPreferencesRepository.saveUser(
                        userId = user.id,
                        email = user.email
                    )

                    Log.d("LoginViewModel", "Регистрация или вход прошли успешно")

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoginSuccessful = true
                        )
                    }
                }

                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = result.message
                        )
                    }
                }
            }
        }
    }

    fun resetLoginSuccessState() {
        _uiState.update {
            it.copy(isLoginSuccessful = false)
        }
    }
}