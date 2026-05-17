package com.example.lecture.ui.screens.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    onUploadAnotherAudioClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Не удалось открыть результат",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(text = uiState.errorMessage ?: "Неизвестная ошибка")

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(onClick = onBackToMainClick) {
                        Text("На главный экран")
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Результат обработки",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    ResultTextCard(
                        title = "Конспект",
                        text = uiState.summary ?: "Конспект отсутствует"
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    ResultTextCard(
                        title = "Расшифровка",
                        text = uiState.transcript ?: "Расшифровка отсутствует"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onBackToMainClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("На главный экран")
                    }

                    OutlinedButton(
                        onClick = onUploadAnotherAudioClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                    ) {
                        Text("Загрузить другое аудио")
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        SettingsButton(
            onClick = onSettingsClick,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
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