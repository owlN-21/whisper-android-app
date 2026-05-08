package com.example.lecture.data.local

import android.content.Context


class UserSessionStorage(context: Context) {

    private val prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveUserId(userId: Long) {
        prefs.edit()
            .putLong(KEY_USER_ID, userId)
            .apply()
    }

    fun getUserId(): Long? {
        val value = prefs.getLong(KEY_USER_ID, -1L)
        return if (value == -1L) null else value
    }

    fun clearUserId() {
        prefs.edit()
            .remove(KEY_USER_ID)
            .apply()
    }

    companion object {
        private const val KEY_USER_ID = "user_id"
    }
}