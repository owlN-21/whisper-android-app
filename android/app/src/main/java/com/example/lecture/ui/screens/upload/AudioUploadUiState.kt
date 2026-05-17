package com.example.lecture.ui.screens.upload


data class AudioUploadUiState(
    val selectedFileName: String? = null,
    val selectedFileSizeBytes: Long? = null,
    val selectedMimeType: String? = null,
    val selectedUri: String? = null,

    val errorMessage: String? = null,

    val isUploading: Boolean = false,
    val uploadErrorMessage: String? = null,
    val uploadedTaskId: Long? = null,
    val isUploadSuccessful: Boolean = false,

    val isProcessing: Boolean = false,
    val processingStatus: String? = null,
    val processingMessage: String? = null,
    val processingErrorMessage: String? = null,

    val completedLocalTaskId: Long? = null
) {
    val isFileSelected: Boolean
        get() = selectedUri != null && errorMessage == null

    val isBusy: Boolean
        get() = isUploading || isProcessing
}