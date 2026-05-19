package com.example.lecture.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lecture.data.local.datastore.UserPreferencesRepository
import com.example.lecture.data.local.db.dao.SummaryDao
import com.example.lecture.data.local.db.dao.TaskDao
import com.example.lecture.data.local.db.dao.TranscriptDao
import com.example.lecture.data.local.db.entity.SummaryEntity
import com.example.lecture.data.local.db.entity.TaskEntity
import com.example.lecture.data.local.db.entity.TranscriptEntity
import com.example.lecture.data.network.NetworkResult
import com.example.lecture.data.network.dto.TaskDto
import com.example.lecture.data.repository.AudioUploadRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STATUS_UPLOADED = "UPLOADED"
private const val STATUS_COMPLETED = "COMPLETED"
private const val STATUS_FAILED = "FAILED"
private const val STATUS_TRANSCRIBING = "TRANSCRIBING"
private const val STATUS_SUMMARIZING = "SUMMARIZING"

class MainViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val taskDao: TaskDao,
    private val summaryDao: SummaryDao,
    private val transcriptDao: TranscriptDao,
    private val audioUploadRepository: AudioUploadRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var observeTasksJob: Job? = null
    private var unfinishedTasksPollingJob: Job? = null
    private val refreshingTaskIds = mutableSetOf<Long>()

    init {
        loadData()
    }

    fun refresh() {
        loadData()
    }

    fun requestDeleteTask(taskId: Long) {
        _uiState.update {
            it.copy(
                taskIdPendingDeletion = taskId,
                deleteErrorMessage = null
            )
        }
    }

    fun cancelDeleteTask() {
        _uiState.update {
            it.copy(
                taskIdPendingDeletion = null,
                deleteErrorMessage = null
            )
        }
    }

    fun clearDeleteError() {
        _uiState.update {
            it.copy(deleteErrorMessage = null)
        }
    }

    fun confirmDeleteTask() {
        val taskId = _uiState.value.taskIdPendingDeletion ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDeletingTask = true,
                    deleteErrorMessage = null
                )
            }

            try {
                val task = taskDao.getTaskById(taskId)

                if (task == null) {
                    _uiState.update {
                        it.copy(
                            isDeletingTask = false,
                            taskIdPendingDeletion = null
                        )
                    }
                    return@launch
                }

                val remoteTaskId = task.remoteTaskId

                if (remoteTaskId == null) {
                    deleteTaskLocally(taskId)

                    _uiState.update {
                        it.copy(
                            isDeletingTask = false,
                            taskIdPendingDeletion = null
                        )
                    }

                    return@launch
                }

                when (val result = audioUploadRepository.deleteTask(remoteTaskId)) {
                    is NetworkResult.Success -> {
                        deleteTaskLocally(taskId)

                        _uiState.update {
                            it.copy(
                                isDeletingTask = false,
                                taskIdPendingDeletion = null
                            )
                        }
                    }

                    is NetworkResult.Error -> {
                        if (isTaskAlreadyDeletedOnBackend(result)) {
                            deleteTaskLocally(taskId)

                            _uiState.update {
                                it.copy(
                                    isDeletingTask = false,
                                    taskIdPendingDeletion = null
                                )
                            }
                        } else {
                            _uiState.update {
                                it.copy(
                                    isDeletingTask = false,
                                    deleteErrorMessage = result.message
                                )
                            }
                        }
                    }
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isDeletingTask = false,
                        deleteErrorMessage = exception.message
                            ?: "Не удалось удалить задачу"
                    )
                }
            }
        }
    }

    private suspend fun deleteTaskLocally(taskId: Long) {
        summaryDao.deleteSummaryByTaskId(taskId)
        transcriptDao.deleteTranscriptByTaskId(taskId)
        taskDao.deleteTaskById(taskId)
    }

    private fun isTaskAlreadyDeletedOnBackend(
        result: NetworkResult.Error
    ): Boolean {
        return result.code == 404 ||
                result.message.contains("PROCESSING_TASK_NOT_FOUND", ignoreCase = true) ||
                result.message.contains("404", ignoreCase = true)
    }

    override fun onCleared() {
        super.onCleared()
        observeTasksJob?.cancel()
        unfinishedTasksPollingJob?.cancel()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            try {
                val userId = userPreferencesRepository.getUserId().first()
                val email = userPreferencesRepository.getEmail().first()

                if (userId == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            email = email.orEmpty(),
                            tasks = emptyList(),
                            isEmpty = true,
                            errorMessage = "Пользователь не найден. Выполните вход заново"
                        )
                    }
                    return@launch
                }

                if (email.isNullOrBlank()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            email = "",
                            tasks = emptyList(),
                            isEmpty = true,
                            errorMessage = "Email пользователя не найден. Выполните вход заново"
                        )
                    }
                    return@launch
                }

                _uiState.update {
                    it.copy(
                        email = email,
                        errorMessage = null
                    )
                }

                syncTasksFromBackend(userId)

                observeTasks(userId)
                startUnfinishedTasksPolling()

            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tasks = emptyList(),
                        isEmpty = true,
                        errorMessage = exception.message
                            ?: "Не удалось загрузить данные главного экрана"
                    )
                }
            }
        }
    }

    private suspend fun syncTasksFromBackend(userId: Long) {
        when (val result = audioUploadRepository.getUserTasks(userId)) {
            is NetworkResult.Success -> {
                result.data.forEach { taskDto ->
                    saveOrUpdateBackendTask(
                        userId = userId,
                        taskDto = taskDto
                    )
                }
            }

            is NetworkResult.Error -> {
                _uiState.update {
                    it.copy(
                        errorMessage = result.message
                    )
                }
            }
        }
    }

    private suspend fun saveOrUpdateBackendTask(
        userId: Long,
        taskDto: TaskDto
    ) {
        val remoteTaskId = taskDto.id

        val fileName = taskDto.fileName
            ?.takeIf { it.isNotBlank() }
            ?: "audio_${remoteTaskId}"

        val status = taskDto.status.takeIf { it.isNotBlank() }
            ?: STATUS_UPLOADED

        val localTask = taskDao.getTaskByRemoteTaskId(
            userId = userId,
            remoteTaskId = remoteTaskId
        )

        val localTaskId = if (localTask == null) {
            taskDao.insertTask(
                TaskEntity(
                    remoteTaskId = remoteTaskId,
                    userId = userId,
                    originalFileName = fileName,
                    localFileUri = null,
                    status = status
                )
            )
        } else {
            taskDao.updateTaskFromBackend(
                taskId = localTask.id,
                originalFileName = fileName,
                status = status
            )

            localTask.id
        }

        when (status) {
            STATUS_COMPLETED -> {
                loadCompletedResultIfNeeded(
                    localTaskId = localTaskId,
                    remoteTaskId = remoteTaskId
                )
            }

            STATUS_FAILED -> {
                taskDao.updateTaskError(
                    taskId = localTaskId,
                    errorMessage = "Обработка аудио завершилась с ошибкой",
                    status = STATUS_FAILED
                )
            }
        }
    }

    private suspend fun loadCompletedResultIfNeeded(
        localTaskId: Long,
        remoteTaskId: Long
    ) {
        val localSummary = summaryDao.getSummaryByTaskId(localTaskId)
        val localTranscript = transcriptDao.getTranscriptByTaskId(localTaskId)

        if (localSummary != null && localTranscript != null) {
            taskDao.updateTaskStatus(
                taskId = localTaskId,
                status = STATUS_COMPLETED
            )

            taskDao.updateSummaryPreview(
                taskId = localTaskId,
                summaryPreview = createSummaryPreview(localSummary.content)
            )

            return
        }

        loadCompletedResult(
            localTaskId = localTaskId,
            remoteTaskId = remoteTaskId
        )
    }

    private fun observeTasks(userId: Long) {
        observeTasksJob?.cancel()

        observeTasksJob = viewModelScope.launch {
            try {
                taskDao.getTasksByUserId(userId).collect { tasks ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            tasks = tasks,
                            isEmpty = tasks.isEmpty(),
                            errorMessage = null
                        )
                    }

                    refreshUnfinishedTasks(tasks)
                }
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tasks = emptyList(),
                        isEmpty = true,
                        errorMessage = exception.message
                            ?: "Не удалось загрузить список задач"
                    )
                }
            }
        }
    }

    private fun startUnfinishedTasksPolling() {
        unfinishedTasksPollingJob?.cancel()

        unfinishedTasksPollingJob = viewModelScope.launch {
            while (true) {
                val currentTasks = _uiState.value.tasks

                if (currentTasks.any { it.isUnfinishedRemoteTask() }) {
                    refreshUnfinishedTasks(currentTasks)
                }

                delay(UNFINISHED_TASKS_POLLING_INTERVAL_MS)
            }
        }
    }

    private fun refreshUnfinishedTasks(tasks: List<TaskEntity>) {
        tasks
            .filter { it.isUnfinishedRemoteTask() }
            .forEach { task ->
                refreshTaskFromBackend(task)
            }
    }

    private fun TaskEntity.isUnfinishedRemoteTask(): Boolean {
        return status != STATUS_COMPLETED &&
                status != STATUS_FAILED &&
                remoteTaskId != null
    }

    private fun refreshTaskFromBackend(task: TaskEntity) {
        val localTaskId = task.id
        val remoteTaskId = task.remoteTaskId ?: return

        if (refreshingTaskIds.contains(localTaskId)) {
            return
        }

        refreshingTaskIds.add(localTaskId)

        viewModelScope.launch {
            try {
                when (val statusResult = audioUploadRepository.getTaskStatus(remoteTaskId)) {
                    is NetworkResult.Success -> {
                        val remoteStatus = statusResult.data.status

                        if (remoteStatus.isBlank()) {
                            return@launch
                        }

                        when (remoteStatus) {
                            STATUS_COMPLETED -> {
                                loadCompletedResult(
                                    localTaskId = localTaskId,
                                    remoteTaskId = remoteTaskId
                                )
                            }

                            STATUS_FAILED -> {
                                taskDao.updateTaskError(
                                    taskId = localTaskId,
                                    errorMessage = "Обработка аудио завершилась с ошибкой. Попробуйте загрузить файл еще раз",
                                    status = STATUS_FAILED
                                )
                            }

                            STATUS_UPLOADED,
                            STATUS_TRANSCRIBING,
                            STATUS_SUMMARIZING -> {
                                taskDao.updateTaskStatus(
                                    taskId = localTaskId,
                                    status = remoteStatus
                                )
                            }

                            else -> {
                                taskDao.updateTaskStatus(
                                    taskId = localTaskId,
                                    status = remoteStatus
                                )
                            }
                        }
                    }

                    is NetworkResult.Error -> {
                        // Временная сетевая ошибка не должна переводить задачу в FAILED.
                        // Оставляем текущий локальный статус и попробуем снова позже.
                    }
                }
            } finally {
                refreshingTaskIds.remove(localTaskId)
            }
        }
    }

    private suspend fun loadCompletedResult(
        localTaskId: Long,
        remoteTaskId: Long
    ) {
        val transcriptResult = audioUploadRepository.getTranscript(remoteTaskId)

        if (transcriptResult is NetworkResult.Error) {
            taskDao.updateTaskError(
                taskId = localTaskId,
                errorMessage = transcriptResult.message,
                status = STATUS_FAILED
            )
            return
        }

        val summaryResult = audioUploadRepository.getSummary(remoteTaskId)

        if (summaryResult is NetworkResult.Error) {
            taskDao.updateTaskError(
                taskId = localTaskId,
                errorMessage = summaryResult.message,
                status = STATUS_FAILED
            )
            return
        }

        val transcript = (transcriptResult as NetworkResult.Success).data.transcript
        val summary = (summaryResult as NetworkResult.Success).data.summary

        if (transcript.isNullOrBlank()) {
            taskDao.updateTaskError(
                taskId = localTaskId,
                errorMessage = "Сервер вернул пустую расшифровку",
                status = STATUS_FAILED
            )
            return
        }

        if (summary.isNullOrBlank()) {
            taskDao.updateTaskError(
                taskId = localTaskId,
                errorMessage = "Сервер вернул пустой конспект",
                status = STATUS_FAILED
            )
            return
        }

        transcriptDao.saveTranscript(
            TranscriptEntity(
                taskId = localTaskId,
                text = transcript
            )
        )

        summaryDao.saveSummary(
            SummaryEntity(
                taskId = localTaskId,
                content = summary
            )
        )

        taskDao.updateTaskStatus(
            taskId = localTaskId,
            status = STATUS_COMPLETED
        )

        taskDao.updateSummaryPreview(
            taskId = localTaskId,
            summaryPreview = createSummaryPreview(summary)
        )
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
        const val UNFINISHED_TASKS_POLLING_INTERVAL_MS = 5_000L
    }
}

class MainViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val taskDao: TaskDao,
    private val summaryDao: SummaryDao,
    private val transcriptDao: TranscriptDao,
    private val audioUploadRepository: AudioUploadRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                userPreferencesRepository = userPreferencesRepository,
                taskDao = taskDao,
                summaryDao = summaryDao,
                transcriptDao = transcriptDao,
                audioUploadRepository = audioUploadRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}