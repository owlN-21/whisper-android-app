package com.example.lecture.data.local.datastore.model

data class UserPreferences(
    val userId: Long? = null,
    val email: String? = null,
    val isLoggedIn: Boolean = false
)