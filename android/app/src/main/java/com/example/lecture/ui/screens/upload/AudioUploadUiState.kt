package com.example.lecture.ui.screens.upload

data class AudioUploadUiState(
    val selectedFileName: String? = null,
    val selectedFileSizeBytes: Long? = null,
    val selectedMimeType: String? = null,
    val selectedUri: String? = null,
    val errorMessage: String? = null
) {
    val isFileSelected: Boolean
        get() = selectedUri != null && errorMessage == null
}