package com.example.lecture.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.lecture.data.local.datastore.UserPreferencesRepository
import com.example.lecture.data.local.db.dao.TaskDao
import com.example.lecture.data.local.db.entity.TaskEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val taskDao: TaskDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState

    private var observeTasksJob: Job? = null

    init {
        loadData()
    }

    fun refresh() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            val userId = userPreferencesRepository.getUserId().first()
            val email = userPreferencesRepository.getEmail().first()

            if (userId == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        email = email.orEmpty(),
                        tasks = emptyList(),
                        isEmpty = true,
                        errorMessage = "Пользователь не найден"
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    email = email.orEmpty(),
                    errorMessage = null
                )
            }

            observeTasks(userId)
        }
    }

    private fun observeTasks(userId: Long) {
        observeTasksJob?.cancel()

        observeTasksJob = viewModelScope.launch {
            taskDao.getTasksByUserId(userId).collect { tasks ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tasks = tasks,
                        isEmpty = tasks.isEmpty(),
                        errorMessage = null
                    )
                }
            }
        }
    }
}

class MainViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val taskDao: TaskDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(
                userPreferencesRepository = userPreferencesRepository,
                taskDao = taskDao
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}