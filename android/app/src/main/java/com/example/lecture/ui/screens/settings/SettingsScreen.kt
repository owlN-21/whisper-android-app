package com.example.lecture.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Экран настроек")

        Text(
            text = "Позже здесь будут настройки приложения.",
            modifier = Modifier.padding(top = 16.dp)
        )

        Button(
            onClick = onBackClick,
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text("Назад")
        }
        Button(
            onClick = onLogoutClick,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Выйти из аккаунта")
        }
    }
}