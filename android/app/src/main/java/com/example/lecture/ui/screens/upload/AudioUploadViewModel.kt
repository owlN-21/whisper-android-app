package com.example.lecture.ui.screens.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lecture.data.local.datastore.UserPreferencesRepository
import com.example.lecture.data.local.db.dao.TaskDao
import com.example.lecture.data.local.db.entity.TaskEntity
import com.example.lecture.data.network.NetworkResult
import com.example.lecture.data.repository.AudioUploadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class AudioUploadViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val audioUploadRepository: AudioUploadRepository,
    private val taskDao: TaskDao
) : ViewModel() {

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
            errorMessage = null,
            uploadErrorMessage = null,
            isUploadSuccessful = false
        )
    }

    fun onFileSelectionCancelled() {
        _uiState.value = _uiState.value.copy(
            errorMessage = "Файл не выбран"
        )
    }

    fun uploadSelectedAudio() {
        val currentState = _uiState.value

        val selectedUri = currentState.selectedUri
        val selectedFileName = currentState.selectedFileName

        if (selectedUri == null || selectedFileName == null) {
            _uiState.value = currentState.copy(
                uploadErrorMessage = "Сначала выберите аудиофайл"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploading = true,
                uploadErrorMessage = null,
                isUploadSuccessful = false
            )

            val userId = userPreferencesRepository.getUserId().first()

            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    uploadErrorMessage = "Пользователь не найден. Войдите в аккаунт повторно"
                )
                return@launch
            }

            val result = audioUploadRepository.uploadAudio(
                userId = userId,
                fileUri = selectedUri,
                fileName = selectedFileName,
                mimeType = currentState.selectedMimeType
            )

            when (result) {
                is NetworkResult.Success -> {
                    val taskDto = result.data
                    val now = System.currentTimeMillis()

                    val localTask = TaskEntity(
                        remoteTaskId = taskDto.id,
                        userId = userId,
                        originalFileName = taskDto.fileName ?: selectedFileName,
                        localFileUri = selectedUri,
                        status = taskDto.status,
                        errorMessage = null,
                        summaryPreview = null,
                        createdAt = now,
                        updatedAt = now
                    )

                    taskDao.insertTask(localTask)

                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        uploadedTaskId = taskDto.id,
                        isUploadSuccessful = true,
                        uploadErrorMessage = null
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        uploadErrorMessage = result.message,
                        isUploadSuccessful = false
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null
        )
    }

    fun clearUploadError() {
        _uiState.value = _uiState.value.copy(
            uploadErrorMessage = null
        )
    }

    fun clearUploadSuccess() {
        _uiState.value = _uiState.value.copy(
            isUploadSuccessful = false
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