package com.example.lecture.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lecture.data.local.datastore.UserPreferencesRepository
import com.example.lecture.data.network.NetworkResult
import com.example.lecture.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val settingsRepository: SettingsRepository,
    private val baseUrl: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            baseUrl = baseUrl
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadUserEmail()
    }

    private fun loadUserEmail() {
        viewModelScope.launch {
            val email = userPreferencesRepository.getEmail().first().orEmpty()

            _uiState.update {
                it.copy(email = email)
            }
        }
    }

    fun checkConnection() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isCheckingConnection = true,
                    connectionMessage = null,
                    errorMessage = null
                )
            }

            when (val result = settingsRepository.checkHealth()) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isCheckingConnection = false,
                            connectionMessage = "Backend доступен",
                            errorMessage = null
                        )
                    }
                }

                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isCheckingConnection = false,
                            connectionMessage = null,
                            errorMessage = result.message ?: "Backend недоступен"
                        )
                    }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userPreferencesRepository.clearUser()

            _uiState.update {
                it.copy(isLoggedOut = true)
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                connectionMessage = null,
                errorMessage = null
            )
        }
    }
}