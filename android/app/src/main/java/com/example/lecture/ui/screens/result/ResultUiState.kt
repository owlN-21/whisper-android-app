package com.example.lecture.ui.screens.result

data class ResultUiState(
    val isLoading: Boolean = true,
    val summary: String? = null,
    val transcript: String? = null,
    val errorMessage: String? = null
)