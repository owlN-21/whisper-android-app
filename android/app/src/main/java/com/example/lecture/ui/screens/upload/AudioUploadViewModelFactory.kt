package com.example.lecture.ui.screens.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lecture.data.local.datastore.UserPreferencesRepository
import com.example.lecture.data.local.db.dao.SummaryDao
import com.example.lecture.data.local.db.dao.TaskDao
import com.example.lecture.data.local.db.dao.TranscriptDao
import com.example.lecture.data.repository.AudioUploadRepository

class AudioUploadViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val audioUploadRepository: AudioUploadRepository,
    private val taskDao: TaskDao,
    private val summaryDao: SummaryDao,
    private val transcriptDao: TranscriptDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioUploadViewModel::class.java)) {
            return AudioUploadViewModel(
                userPreferencesRepository = userPreferencesRepository,
                audioUploadRepository = audioUploadRepository,
                taskDao = taskDao,
                summaryDao = summaryDao,
                transcriptDao = transcriptDao
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}