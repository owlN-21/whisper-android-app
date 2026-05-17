package com.example.lecture.ui.screens.settings

data class SettingsUiState(
    val email: String = "",
    val baseUrl: String = "",
    val isCheckingConnection: Boolean = false,
    val connectionMessage: String? = null,
    val errorMessage: String? = null,
    val isLoggedOut: Boolean = false
)