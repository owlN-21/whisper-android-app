package com.example.lecture.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lecture.data.local.db.dao.SummaryDao
import com.example.lecture.data.local.db.dao.TranscriptDao

class ResultViewModelFactory(
    private val taskId: Long,
    private val summaryDao: SummaryDao,
    private val transcriptDao: TranscriptDao
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ResultViewModel::class.java)) {
            return ResultViewModel(
                taskId = taskId,
                summaryDao = summaryDao,
                transcriptDao = transcriptDao
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}