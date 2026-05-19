package com.example.lecture.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.lecture.data.local.datastore.UserPreferencesRepository
import com.example.lecture.data.repository.SettingsRepository

class SettingsViewModelFactory(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val settingsRepository: SettingsRepository,
    private val baseUrl: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(
                userPreferencesRepository = userPreferencesRepository,
                settingsRepository = settingsRepository,
                baseUrl = baseUrl
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}