package com.example.lecture.ui.screens.result

data class ResultUiState(
    val isLoading: Boolean = true,
    val summary: String? = null,
    val transcript: String? = null,
    val errorMessage: String? = null,
    val isDeleting: Boolean = false,
    val deleteErrorMessage: String? = null,
    val isDeleted: Boolean = false
) {
    val hasResult: Boolean
        get() = !summary.isNullOrBlank() && !transcript.isNullOrBlank()
}