package com.example.lecture.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lecture.data.local.db.dao.SummaryDao
import com.example.lecture.data.local.db.dao.TranscriptDao
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultViewModel(
    private val taskId: Long,
    private val summaryDao: SummaryDao,
    private val transcriptDao: TranscriptDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init {
        loadResult()
    }

    private fun loadResult() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val summary = summaryDao.getSummaryByTaskId(taskId)
            val transcript = transcriptDao.getTranscriptByTaskId(taskId)

            if (summary == null && transcript == null) {
                _uiState.value = ResultUiState(
                    isLoading = false,
                    errorMessage = "Результат для этой задачи не найден"
                )
                return@launch
            }

            _uiState.value = ResultUiState(
                isLoading = false,
                summary = summary?.content,
                transcript = transcript?.text,
                errorMessage = null
            )
        }
    }
}