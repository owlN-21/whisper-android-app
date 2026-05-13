package com.example.lecture.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.lecture.data.local.datastore.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val USER_PREFERENCES_NAME = "user_preferences"

private val Context.userDataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

class UserPreferencesRepository(
    private val context: Context
) {

    private object PreferencesKeys {
        val USER_ID = longPreferencesKey("user_id")
        val EMAIL = stringPreferencesKey("email")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
    }

    val userPreferences: Flow<UserPreferences> =
        context.userDataStore.data.map { preferences ->
            UserPreferences(
                userId = preferences[PreferencesKeys.USER_ID],
                email = preferences[PreferencesKeys.EMAIL],
                isLoggedIn = preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
            )
        }

    fun getUserId(): Flow<Long?> {
        return context.userDataStore.data.map { preferences ->
            preferences[PreferencesKeys.USER_ID]
        }
    }

    fun getEmail(): Flow<String?> {
        return context.userDataStore.data.map { preferences ->
            preferences[PreferencesKeys.EMAIL]
        }
    }

    fun isLoggedIn(): Flow<Boolean> {
        return context.userDataStore.data.map { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
        }
    }

    suspend fun saveUser(
        userId: Long,
        email: String
    ) {
        context.userDataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_ID] = userId
            preferences[PreferencesKeys.EMAIL] = email
            preferences[PreferencesKeys.IS_LOGGED_IN] = true
        }
    }

    suspend fun clearUser() {
        context.userDataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.USER_ID)
            preferences.remove(PreferencesKeys.EMAIL)
            preferences[PreferencesKeys.IS_LOGGED_IN] = false
        }
    }
}