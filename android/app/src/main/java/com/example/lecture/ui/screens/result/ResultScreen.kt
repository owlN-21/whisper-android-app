package com.example.lecture.ui.screens.result

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lecture.ui.components.SettingsButton

@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    onBackToMainClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onUploadAnotherAudioClick: () -> Unit,
    onDeleted: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.deleteErrorMessage) {
        uiState.deleteErrorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onDeleted()
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
                .padding(24.dp)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.errorMessage != null -> {
                    ErrorState(
                        message = uiState.errorMessage.orEmpty(),
                        onBackToMainClick = onBackToMainClick,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                uiState.hasResult -> {
                    ResultContent(
                        uiState = uiState,
                        onBackToMainClick = onBackToMainClick,
                        onDeleteClick = {
                            showDeleteDialog = true
                        }
                    )
                }

                else -> {
                    ErrorState(
                        message = "Данные результата отсутствуют",
                        onBackToMainClick = onBackToMainClick,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            SettingsButton(
                onClick = {
                    if (!uiState.isDeleting) {
                        onSettingsClick()
                    }
                },
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!uiState.isDeleting) {
                    showDeleteDialog = false
                }
            },
            title = {
                Text("Удалить этот конспект?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteTask()
                    },
                    enabled = !uiState.isDeleting
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    },
                    enabled = !uiState.isDeleting
                ) {
                    Text("Отмена")
                }
            }
        )
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
            text = "Загрузка результата...",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onBackToMainClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Не удалось открыть результат",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = message,
            color = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onBackToMainClick) {
            Text("На главный экран")
        }
    }
}

@Composable
private fun ResultContent(
    uiState: ResultUiState,
    onBackToMainClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Назад",
                modifier = Modifier
                    .clickable(
                        enabled = !uiState.isDeleting,
                        onClick = onBackToMainClick
                    )
                    .size(28.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Результат обработки",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        ResultTextCard(
            title = "Конспект",
            text = uiState.summary.orEmpty()
        )

        Spacer(modifier = Modifier.height(16.dp))

        ResultTextCard(
            title = "Расшифровка",
            text = uiState.transcript.orEmpty()
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onDeleteClick,
            enabled = !uiState.isDeleting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isDeleting) {
                CircularProgressIndicator()
            } else {
                Text("Удалить конспект")
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
private fun ResultTextCard(
    title: String,
    text: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}