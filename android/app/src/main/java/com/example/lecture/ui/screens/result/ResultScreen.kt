package com.example.lecture.ui.screens.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lecture.ui.components.SettingsButton

@Composable
fun ResultScreen(
    onBackToMainClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onUploadAnotherAudioClick: () -> Unit
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
            Text("Экран результата")

            Text(
                text = "Здесь позже будут transcript и summary.",
                modifier = Modifier.padding(top = 16.dp)
            )

            Button(
                onClick = onBackToMainClick,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Text("На главный экран")
            }

            Button(
                onClick = onUploadAnotherAudioClick,
                modifier = Modifier.padding(top = 24.dp)
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