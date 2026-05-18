package com.example.lecture.ui.screens.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lecture.data.local.datastore.UserPreferencesRepository
import com.example.lecture.data.local.db.dao.SummaryDao
import com.example.lecture.data.local.db.dao.TaskDao
import com.example.lecture.data.local.db.dao.TranscriptDao
import com.example.lecture.data.local.db.entity.SummaryEntity
import com.example.lecture.data.local.db.entity.TaskEntity
import com.example.lecture.data.local.db.entity.TranscriptEntity
import com.example.lecture.data.network.NetworkResult
import com.example.lecture.data.repository.AudioUploadRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val STATUS_UPLOADED = "UPLOADED"
private const val STATUS_TRANSCRIBING = "TRANSCRIBING"
private const val STATUS_SUMMARIZING = "SUMMARIZING"
private const val STATUS_COMPLETED = "COMPLETED"
private const val STATUS_FAILED = "FAILED"
class AudioUploadViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val audioUploadRepository: AudioUploadRepository,
    private val taskDao: TaskDao,
    private val summaryDao: SummaryDao,
    private val transcriptDao: TranscriptDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioUploadUiState())
    val uiState: StateFlow<AudioUploadUiState> = _uiState.asStateFlow()

    fun onAudioFileSelected(
        fileName: String?,
        fileSizeBytes: Long?,
        mimeType: String?,
        uri: String
    ) {
        if (uri.isBlank()) {
            _uiState.value = AudioUploadUiState(
                errorMessage = "Не удалось получить путь к файлу"
            )
            return
        }

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
            processingErrorMessage = null,
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

        if (currentState.isBusy) {
            return
        }

        val selectedUri = currentState.selectedUri
        val selectedFileName = currentState.selectedFileName
        val selectedMimeType = currentState.selectedMimeType

        if (selectedUri.isNullOrBlank() || selectedFileName.isNullOrBlank()) {
            _uiState.value = currentState.copy(
                uploadErrorMessage = "Сначала выберите аудиофайл"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploading = true,
                isProcessing = false,
                uploadErrorMessage = null,
                processingErrorMessage = null,
                isUploadSuccessful = false,
                completedLocalTaskId = null
            )

            val userId = userPreferencesRepository.getUserId().first()

            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    uploadErrorMessage = "Пользователь не найден. Войдите в аккаунт повторно"
                )
                return@launch
            }

            val uploadResult = audioUploadRepository.uploadAudio(
                userId = userId,
                fileUri = selectedUri,
                fileName = selectedFileName,
                mimeType = selectedMimeType
            )

            when (uploadResult) {
                is NetworkResult.Success -> {
                    val taskDto = uploadResult.data
                    val now = System.currentTimeMillis()

                    val taskStatus = taskDto.status.ifBlank { STATUS_UPLOADED }

                    val localTask = TaskEntity(
                        remoteTaskId = taskDto.id,
                        userId = userId,
                        originalFileName = taskDto.fileName ?: selectedFileName,
                        localFileUri = selectedUri,
                        status = taskStatus,
                        errorMessage = null,
                        summaryPreview = null,
                        createdAt = now,
                        updatedAt = now
                    )

                    val localTaskId = taskDao.insertTask(localTask)

                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        isProcessing = true,
                        uploadedTaskId = taskDto.id,
                        isUploadSuccessful = true,
                        uploadErrorMessage = null,
                        processingStatus = taskStatus,
                        processingMessage = getProcessingMessage(taskStatus)
                    )

                    pollTaskStatus(
                        remoteTaskId = taskDto.id,
                        localTaskId = localTaskId
                    )
                }

                is NetworkResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        uploadErrorMessage = uploadResult.message,
                        isUploadSuccessful = false
                    )
                }
            }
        }
    }

    private suspend fun pollTaskStatus(
        remoteTaskId: Long,
        localTaskId: Long
    ) {
        while (true) {
            val statusResult = audioUploadRepository.getTaskStatus(remoteTaskId)

            when (statusResult) {
                is NetworkResult.Success -> {
                    val taskDto = statusResult.data
                    val status = taskDto.status.ifBlank { STATUS_UPLOADED }

                    taskDao.updateTaskStatus(
                        taskId = localTaskId,
                        status = status
                    )

                    _uiState.value = _uiState.value.copy(
                        isProcessing = true,
                        processingStatus = status,
                        processingMessage = getProcessingMessage(status),
                        processingErrorMessage = null
                    )

                    when (status) {
                        STATUS_COMPLETED -> {
                            loadAndSaveResult(
                                remoteTaskId = remoteTaskId,
                                localTaskId = localTaskId
                            )
                            return
                        }

                        STATUS_FAILED -> {
                            val errorMessage = "Обработка аудио завершилась с ошибкой. Попробуйте загрузить файл еще раз"

                            taskDao.updateTaskError(
                                taskId = localTaskId,
                                errorMessage = errorMessage,
                                status = STATUS_FAILED
                            )

                            _uiState.value = _uiState.value.copy(
                                isProcessing = false,
                                processingErrorMessage = errorMessage,
                                processingMessage = null
                            )
                            return
                        }
                    }
                }

                is NetworkResult.Error -> {
                    val errorMessage =
                        "Не удалось обновить статус обработки. Задача останется на главном экране и продолжит обновляться позже"

                    _uiState.value = _uiState.value.copy(
                        isProcessing = false,
                        processingErrorMessage = errorMessage,
                        processingMessage = null
                    )
                    return
                }
            }

            delay(POLLING_INTERVAL_MS)
        }
    }

    private suspend fun loadAndSaveResult(
        remoteTaskId: Long,
        localTaskId: Long
    ) {
        _uiState.value = _uiState.value.copy(
            processingMessage = "Получаем результат..."
        )

        val transcriptResult = audioUploadRepository.getTranscript(remoteTaskId)

        if (transcriptResult is NetworkResult.Error) {
            val errorMessage = "Не удалось получить расшифровку: ${transcriptResult.message}"

            taskDao.updateTaskError(
                taskId = localTaskId,
                errorMessage = errorMessage,
                status = STATUS_FAILED
            )

            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                processingErrorMessage = errorMessage,
                processingMessage = null
            )
            return
        }

        val summaryResult = audioUploadRepository.getSummary(remoteTaskId)

        if (summaryResult is NetworkResult.Error) {
            val errorMessage = "Не удалось получить конспект: ${summaryResult.message}"

            taskDao.updateTaskError(
                taskId = localTaskId,
                errorMessage = errorMessage,
                status = STATUS_FAILED
            )

            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                processingErrorMessage = errorMessage,
                processingMessage = null
            )
            return
        }

        val transcript = (transcriptResult as NetworkResult.Success).data
        val summary = (summaryResult as NetworkResult.Success).data

        val transcriptText = transcript.transcript

        if (transcriptText.isNullOrBlank()) {
            val errorMessage = "Сервер вернул пустую расшифровку. Проверьте аудиофайл и попробуйте снова"

            taskDao.updateTaskError(
                taskId = localTaskId,
                errorMessage = errorMessage,
                status = STATUS_FAILED
            )

            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                processingErrorMessage = errorMessage,
                processingMessage = null
            )
            return
        }

        val summaryText = summary.summary

        if (summaryText.isNullOrBlank()) {
            val errorMessage = "Сервер вернул пустой конспект. Попробуйте загрузить файл еще раз"

            taskDao.updateTaskError(
                taskId = localTaskId,
                errorMessage = errorMessage,
                status = STATUS_FAILED
            )

            _uiState.value = _uiState.value.copy(
                isProcessing = false,
                processingErrorMessage = errorMessage,
                processingMessage = null
            )
            return
        }

        transcriptDao.saveTranscript(
            TranscriptEntity(
                taskId = localTaskId,
                text = transcriptText
            )
        )

        summaryDao.saveSummary(
            SummaryEntity(
                taskId = localTaskId,
                content = summaryText
            )
        )

        taskDao.updateTaskStatus(
            taskId = localTaskId,
            status = STATUS_COMPLETED
        )

        taskDao.updateSummaryPreview(
            taskId = localTaskId,
            summaryPreview = createSummaryPreview(summaryText)
        )

        _uiState.value = _uiState.value.copy(
            isProcessing = false,
            processingStatus = STATUS_COMPLETED,
            processingMessage = "Готово",
            processingErrorMessage = null,
            completedLocalTaskId = localTaskId
        )
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

    fun clearProcessingError() {
        _uiState.value = _uiState.value.copy(
            processingErrorMessage = null
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

    private fun getProcessingMessage(status: String?): String {
        return when (status) {
            STATUS_UPLOADED -> "Файл загружен. Ожидаем начала обработки..."
            STATUS_TRANSCRIBING -> "Распознаем аудио..."
            STATUS_SUMMARIZING -> "Создаем конспект..."
            STATUS_COMPLETED -> "Обработка завершена"
            STATUS_FAILED -> "Ошибка обработки"
            else -> "Обрабатываем аудио..."
        }
    }

    private fun createSummaryPreview(summary: String): String {
        return summary
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(3)
            .joinToString(separator = "\n")
            .take(300)
    }

    private companion object {
        const val POLLING_INTERVAL_MS = 3_000L
    }
}