package com.example.lecture.ui.screens.upload

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lecture.ui.components.SettingsButton

@Composable
fun AudioUploadScreen(
    onSendAudioClick: () -> Unit,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Экран выбора аудио")

            Button(
                onClick = onSendAudioClick,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("Отправить аудио")
            }

            OutlinedButton(
                onClick = onBackClick,
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text("Назад")
            }
        }
        SettingsButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}