package com.example.lecture.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lecture.data.local.db.entity.TaskEntity
import com.example.lecture.ui.components.SettingsButton
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val STATUS_COMPLETED = "COMPLETED"
private const val STATUS_TRANSCRIBING = "TRANSCRIBING"
private const val STATUS_SUMMARIZING = "SUMMARIZING"
private const val STATUS_FAILED = "FAILED"

@Composable
fun MainScreen(
    uiState: MainUiState,
    onUploadClick: () -> Unit,
    onTaskClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    onDeleteFailedTaskClick: (Long) -> Unit,
    onCancelDeleteTask: () -> Unit,
    onConfirmDeleteTask: () -> Unit,
    onDeleteErrorShown: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(uiState.deleteErrorMessage) {
        val message = uiState.deleteErrorMessage

        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message = message)
            onDeleteErrorShown()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 24.dp
                )
                .padding(top = 48.dp)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.errorMessage != null -> {
                    ErrorState(
                        message = uiState.errorMessage,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    MainContent(
                        uiState = uiState,
                        onUploadClick = onUploadClick,
                        onTaskClick = onTaskClick,
                        onDeleteFailedTaskClick = onDeleteFailedTaskClick,
                        onShowMessage = { message ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(message = message)
                            }
                        }
                    )
                }
            }

            if (uiState.taskIdPendingDeletion != null) {
                DeleteTaskDialog(
                    isDeleting = uiState.isDeletingTask,
                    onDismiss = onCancelDeleteTask,
                    onConfirm = onConfirmDeleteTask
                )
            }

            SettingsButton(
                onClick = onSettingsClick,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun MainContent(
    uiState: MainUiState,
    onUploadClick: () -> Unit,
    onTaskClick: (Long) -> Unit,
    onDeleteFailedTaskClick: (Long) -> Unit,
    onShowMessage: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderBlock(email = uiState.email)
        }

        item {
            UploadAudioCard(
                onClick = onUploadClick
            )
        }

        item {
            Text(
                text = "Последние конспекты",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (uiState.isEmpty) {
            item {
                EmptyTasksBlock()
            }
        } else {
            items(uiState.tasks.size) { index ->
                val task = uiState.tasks[index]

                TaskCard(
                    task = task,
                    onClick = {
                        handleTaskClick(
                            task = task,
                            onTaskClick = onTaskClick,
                            onShowMessage = onShowMessage
                        )
                    },
                    onDeleteClick = {
                        onDeleteFailedTaskClick(task.id)
                    }
                )
            }
        }
    }
}

private fun handleTaskClick(
    task: TaskEntity,
    onTaskClick: (Long) -> Unit,
    onShowMessage: (String) -> Unit
) {
    when (task.status) {
        STATUS_COMPLETED -> {
            onTaskClick(task.id)
        }

        STATUS_TRANSCRIBING -> {
            onShowMessage("Аудио еще распознается. Результат будет доступен позже")
        }

        STATUS_SUMMARIZING -> {
            onShowMessage("Конспект еще создается. Результат будет доступен позже")
        }

        STATUS_FAILED -> {
            val message = task.errorMessage
                ?.takeIf { it.isNotBlank() }
                ?: "Обработка задачи завершилась с ошибкой"

            onShowMessage(message)
        }

        else -> {
            onShowMessage("Задача еще не завершена. Текущий статус: ${task.status}")
        }
    }
}

@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Загрузка задач...",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = message,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodyMedium,
        modifier = modifier
    )
}

@Composable
private fun HeaderBlock(
    email: String
) {
    Column {
        Text(
            text = "Главный экран",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun UploadAudioCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "Загрузить новое аудио",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Выберите аудиофайл, чтобы получить конспект",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyTasksBlock() {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Пока нет конспектов",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Загрузите аудиофайл, чтобы получить первый конспект",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskEntity,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val summaryPreview = when {
        task.status == STATUS_FAILED -> task.errorMessage
            ?.takeIf { it.isNotBlank() }
            ?: "Обработка завершилась с ошибкой"

        task.summaryPreview.isNullOrBlank() -> getStatusDescription(task.status)

        else -> task.summaryPreview
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = task.originalFileName.ifBlank { "Аудиофайл без названия" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Статус: ${getStatusText(task.status)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            Text(
                text = summaryPreview,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Обновлено: ${formatDate(task.updatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            if (task.status == STATUS_FAILED) {
                TextButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(text = "Удалить")
                }
            }
        }
    }
}

private fun getStatusText(status: String): String {
    return when (status) {
        STATUS_COMPLETED -> "готово"
        STATUS_TRANSCRIBING -> "распознавание аудио"
        STATUS_SUMMARIZING -> "создание конспекта"
        STATUS_FAILED -> "ошибка"
        else -> status
    }
}

private fun getStatusDescription(status: String): String {
    return when (status) {
        STATUS_COMPLETED -> "Конспект готов"
        STATUS_TRANSCRIBING -> "Аудио распознается"
        STATUS_SUMMARIZING -> "Конспект создается"
        STATUS_FAILED -> "Обработка завершилась с ошибкой"
        else -> "Задача еще обрабатывается"
    }
}

@Composable
private fun DeleteTaskDialog(
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isDeleting) {
                onDismiss()
            }
        },
        title = {
            Text(text = "Удалить эту задачу?")
        },
        text = {
            Text(text = "Это действие нельзя будет отменить.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting
            ) {
                Text(
                    text = if (isDeleting) {
                        "Удаление..."
                    } else {
                        "Удалить"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text(text = "Отмена")
            }
        }
    )
}

private fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}