package com.example.lecture.ui.screens.upload

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioUploadViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AudioUploadUiState())
    val uiState: StateFlow<AudioUploadUiState> = _uiState.asStateFlow()

    fun onAudioFileSelected(
        fileName: String?,
        fileSizeBytes: Long?,
        mimeType: String?,
        uri: String
    ) {
        if (fileName.isNullOrBlank()) {
            _uiState.value = AudioUploadUiState(
                errorMessage = "Не удалось получить имя файла"
            )
            return
        }

        if (fileSizeBytes == null || fileSizeBytes <= 0L) {
            _uiState.value = AudioUploadUiState(
                errorMessage = "Не удалось получить размер файла"
            )
            return
        }

        if (!isSupportedAudioFile(fileName, mimeType)) {
            _uiState.value = AudioUploadUiState(
                errorMessage = "Неподдерживаемый формат файла. Выберите mp3, wav или m4a"
            )
            return
        }

        _uiState.value = AudioUploadUiState(
            selectedFileName = fileName,
            selectedFileSizeBytes = fileSizeBytes,
            selectedMimeType = mimeType,
            selectedUri = uri,
            errorMessage = null
        )
    }

    fun onFileSelectionCancelled() {
        _uiState.value = _uiState.value.copy(
            errorMessage = "Файл не выбран"
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }

    private fun isSupportedAudioFile(
        fileName: String,
        mimeType: String?
    ): Boolean {
        val extension = fileName.substringAfterLast('.', missingDelimiterValue = "")
            .lowercase()

        val isSupportedExtension = extension in setOf("mp3", "wav", "m4a")

        val isSupportedMimeType = mimeType in setOf(
            "audio/mpeg",
            "audio/mp3",
            "audio/wav",
            "audio/x-wav",
            "audio/mp4",
            "audio/m4a",
            "audio/x-m4a"
        )

        return isSupportedExtension || isSupportedMimeType
    }
}