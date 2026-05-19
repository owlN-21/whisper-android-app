package com.example.lecture

import android.app.Application
import androidx.room.Room
import com.example.lecture.data.local.datastore.UserPreferencesRepository
import com.example.lecture.data.local.db.AppDatabase
import kotlin.jvm.java

class App : Application() {

    lateinit var userPreferencesRepository: UserPreferencesRepository
        private set

    lateinit var database: AppDatabase

        private set

    override fun onCreate() {
        super.onCreate()

        userPreferencesRepository = UserPreferencesRepository(this)

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "lecture_database"
        ).build()
    }
}