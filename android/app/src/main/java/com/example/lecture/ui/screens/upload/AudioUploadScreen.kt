package com.example.lecture.ui.screens.upload

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lecture.ui.components.SettingsButton
import kotlinx.coroutines.launch

@Composable
fun AudioUploadScreen(
    onUploadSuccess: () -> Unit,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: AudioUploadViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) {
            viewModel.onFileSelectionCancelled()
            return@rememberLauncherForActivityResult
        }

        val fileInfo = getAudioFileInfo(context, uri)

        viewModel.onAudioFileSelected(
            fileName = fileInfo.fileName,
            fileSizeBytes = fileInfo.fileSizeBytes,
            mimeType = fileInfo.mimeType,
            uri = uri.toString()
        )
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.uploadErrorMessage) {
        uiState.uploadErrorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearUploadError()
        }
    }

    LaunchedEffect(uiState.isUploadSuccessful) {
        if (uiState.isUploadSuccessful) {
            snackbarHostState.showSnackbar("Файл загружен")
            viewModel.clearUploadSuccess()
            onUploadSuccess()
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 48.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Выбор аудиофайла",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        audioPickerLauncher.launch(
                            arrayOf(
                                "audio/mpeg",
                                "audio/wav",
                                "audio/x-wav",
                                "audio/mp4",
                                "audio/*"
                            )
                        )
                    },
                    enabled = !uiState.isUploading
                ) {
                    Text("Выбрать аудиофайл")
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (uiState.isFileSelected) {
                    SelectedAudioFileCard(uiState = uiState)
                } else {
                    Text(
                        text = "Файл пока не выбран",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.uploadSelectedAudio()
                    },
                    enabled = uiState.isFileSelected && !uiState.isUploading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Загрузить")
                    }
                }

                OutlinedButton(
                    onClick = onBackClick,
                    enabled = !uiState.isUploading,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Text("На главный экран")
                }
            }

            SettingsButton(
                onClick = onSettingsClick,
                modifier = Modifier.align(Alignment.BottomEnd)
            )
        }
    }
}

@Composable
private fun SelectedAudioFileCard(
    uiState: AudioUploadUiState
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Выбранный файл",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Название: ${uiState.selectedFileName}")
            Text("Размер: ${formatFileSize(uiState.selectedFileSizeBytes)}")
            Text("MIME type: ${uiState.selectedMimeType ?: "Неизвестно"}")

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Uri: ${uiState.selectedUri}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private data class AudioFileInfo(
    val fileName: String?,
    val fileSizeBytes: Long?,
    val mimeType: String?
)

private fun getAudioFileInfo(
    context: Context,
    uri: Uri
): AudioFileInfo {
    val contentResolver = context.contentResolver

    val mimeType = contentResolver.getType(uri)

    var fileName: String? = null
    var fileSizeBytes: Long? = null

    contentResolver.query(
        uri,
        null,
        null,
        null,
        null
    )?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)

        if (cursor.moveToFirst()) {
            if (nameIndex != -1) {
                fileName = cursor.getString(nameIndex)
            }

            if (sizeIndex != -1 && !cursor.isNull(sizeIndex)) {
                fileSizeBytes = cursor.getLong(sizeIndex)
            }
        }
    }

    return AudioFileInfo(
        fileName = fileName,
        fileSizeBytes = fileSizeBytes,
        mimeType = mimeType
    )
}

private fun formatFileSize(
    sizeBytes: Long?
): String {
    if (sizeBytes == null || sizeBytes <= 0L) {
        return "Неизвестно"
    }

    val sizeKb = sizeBytes / 1024.0
    val sizeMb = sizeKb / 1024.0

    return if (sizeMb >= 1) {
        "%.2f MB".format(sizeMb)
    } else {
        "%.2f KB".format(sizeKb)
    }
}