package com.example.lecture.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lecture.data.local.db.dao.SummaryDao
import com.example.lecture.data.local.db.dao.TaskDao
import com.example.lecture.data.local.db.dao.TranscriptDao
import com.example.lecture.data.network.NetworkResult
import com.example.lecture.data.repository.AudioUploadRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val ERROR_CODE_PROCESSING_TASK_NOT_FOUND = "PROCESSING_TASK_NOT_FOUND"

class ResultViewModel(
    private val taskId: Long,
    private val summaryDao: SummaryDao,
    private val transcriptDao: TranscriptDao,
    private val taskDao: TaskDao,
    private val audioUploadRepository: AudioUploadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init {
        loadResult()
    }

    private fun loadResult() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                if (taskId <= 0L) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Некорректный идентификатор задачи"
                        )
                    }
                    return@launch
                }

                val task = taskDao.getTaskById(taskId)

                if (task == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Задача не найдена"
                        )
                    }
                    return@launch
                }

                val summary = summaryDao.getSummaryByTaskId(taskId)
                val transcript = transcriptDao.getTranscriptByTaskId(taskId)

                val summaryText = summary?.content
                val transcriptText = transcript?.text

                when {
                    summaryText.isNullOrBlank() && transcriptText.isNullOrBlank() -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Результат для этой задачи не найден"
                            )
                        }
                    }

                    summaryText.isNullOrBlank() -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Конспект для этой задачи не найден"
                            )
                        }
                    }

                    transcriptText.isNullOrBlank() -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Расшифровка для этой задачи не найдена"
                            )
                        }
                    }

                    else -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                summary = summaryText,
                                transcript = transcriptText,
                                errorMessage = null
                            )
                        }
                    }
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Не удалось открыть результат"
                    )
                }
            }
        }
    }

    fun deleteTask() {
        val currentState = _uiState.value

        if (currentState.isDeleting) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDeleting = true,
                    deleteErrorMessage = null
                )
            }

            try {
                if (taskId <= 0L) {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            deleteErrorMessage = "Некорректный идентификатор задачи"
                        )
                    }
                    return@launch
                }

                val task = taskDao.getTaskById(taskId)

                if (task == null) {
                    deleteLocalTaskData(taskId)

                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            isDeleted = true
                        )
                    }
                    return@launch
                }

                val remoteTaskId = task.remoteTaskId

                if (remoteTaskId != null) {
                    when (val result = audioUploadRepository.deleteTask(remoteTaskId)) {
                        is NetworkResult.Success -> {
                            deleteLocalTaskData(taskId)
                        }

                        is NetworkResult.Error -> {
                            if (isTaskAlreadyDeletedOnBackend(result.message)) {
                                deleteLocalTaskData(taskId)
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isDeleting = false,
                                        deleteErrorMessage = result.message
                                    )
                                }
                                return@launch
                            }
                        }
                    }
                } else {
                    deleteLocalTaskData(taskId)
                }

                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        isDeleted = true
                    )
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        deleteErrorMessage = exception.message ?: "Не удалось удалить конспект"
                    )
                }
            }
        }
    }

    private fun isTaskAlreadyDeletedOnBackend(message: String): Boolean {
        return message.contains("404", ignoreCase = true) ||
                message.contains(ERROR_CODE_PROCESSING_TASK_NOT_FOUND, ignoreCase = true) ||
                message.contains("Processing task not found", ignoreCase = true) ||
                message.contains("not found", ignoreCase = true) ||
                message.contains("Данные не найдены", ignoreCase = true)
    }

    private suspend fun deleteLocalTaskData(taskId: Long) {
        summaryDao.deleteSummaryByTaskId(taskId)
        transcriptDao.deleteTranscriptByTaskId(taskId)
        taskDao.deleteTaskById(taskId)
    }
}