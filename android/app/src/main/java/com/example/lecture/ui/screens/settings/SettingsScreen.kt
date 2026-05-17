package com.example.lecture.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showLogoutDialog by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(uiState.connectionMessage) {
        uiState.connectionMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onLogout()
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = {
                showLogoutDialog = false
            },
            title = {
                Text(text = "Выйти из аккаунта?")
            },
            text = {
                Text(text = "После выхода нужно будет снова войти по email.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    }
                ) {
                    Text(text = "Выйти")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                    }
                ) {
                    Text(text = "Отмена")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        SettingsContent(
            paddingValues = paddingValues,
            email = uiState.email,
            baseUrl = uiState.baseUrl,
            isCheckingConnection = uiState.isCheckingConnection,
            onCheckConnectionClick = viewModel::checkConnection,
            onBackClick = onBackClick,
            onLogoutClick = {
                showLogoutDialog = true
            }
        )
    }
}

@Composable
private fun SettingsContent(
    paddingValues: PaddingValues,
    email: String,
    baseUrl: String,
    isCheckingConnection: Boolean,
    onCheckConnectionClick: () -> Unit,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Настройки",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Текущий пользователь",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = if (email.isBlank()) "Email не найден" else email,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Backend",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = baseUrl,
            style = MaterialTheme.typography.bodyLarge
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onCheckConnectionClick,
            enabled = !isCheckingConnection
        ) {
            if (isCheckingConnection) {
                CircularProgressIndicator()
            } else {
                Text(text = "Проверить соединение")
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBackClick
        ) {
            Text(text = "Назад")
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onLogoutClick
        ) {
            Text(text = "Выйти из аккаунта")
        }
    }
}