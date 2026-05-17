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
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val summary = summaryDao.getSummaryByTaskId(taskId)
            val transcript = transcriptDao.getTranscriptByTaskId(taskId)

            if (summary == null && transcript == null) {
                _uiState.value = ResultUiState(
                    isLoading = false,
                    errorMessage = "Результат для этой задачи не найден"
                )
                return@launch
            }

            _uiState.value = ResultUiState(
                isLoading = false,
                summary = summary?.content,
                transcript = transcript?.text,
                errorMessage = null
            )
        }
    }

    fun deleteTask() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDeleting = true,
                    deleteErrorMessage = null
                )
            }

            try {
                val task = taskDao.getTaskById(taskId)

                if (task == null) {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            deleteErrorMessage = "Задача не найдена"
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
                            val message = result.message.orEmpty()

                            val isTaskAlreadyDeletedOnBackend =
                                message.contains("404", ignoreCase = true) ||
                                        message.contains("PROCESSING_TASK_NOT_FOUND", ignoreCase = true) ||
                                        message.contains("Processing task not found", ignoreCase = true) ||
                                        message.contains("not found", ignoreCase = true)

                            if (isTaskAlreadyDeletedOnBackend) {
                                deleteLocalTaskData(taskId)
                            } else {
                                _uiState.update {
                                    it.copy(
                                        isDeleting = false,
                                        deleteErrorMessage = result.message ?: "Не удалось удалить задачу на сервере"
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
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        deleteErrorMessage = e.message ?: "Не удалось удалить конспект"
                    )
                }
            }
        }
    }

    private suspend fun deleteLocalTaskData(taskId: Long) {
        summaryDao.deleteSummaryByTaskId(taskId)
        transcriptDao.deleteTranscriptByTaskId(taskId)
        taskDao.deleteTaskById(taskId)
    }
}