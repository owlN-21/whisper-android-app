package com.example.lecture

import android.app.Application
import com.example.lecture.data.local.datastore.UserPreferencesRepository

class App : Application() {

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    override fun onCreate() {
        super.onCreate()

        userPreferencesRepository = UserPreferencesRepository(this)
    }
}