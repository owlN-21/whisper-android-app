package com.example.lecture.ui.screens.main

import com.example.lecture.data.local.db.entity.TaskEntity

data class MainUiState(
    val email: String = "",
    val isLoading: Boolean = true,
    val tasks: List<TaskEntity> = emptyList(),
    val errorMessage: String? = null,
    val isEmpty: Boolean = true,

    val taskIdPendingDeletion: Long? = null,
    val isDeletingTask: Boolean = false,
    val deleteErrorMessage: String? = null
)