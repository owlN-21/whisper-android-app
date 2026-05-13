package com.example.lecture.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Main : Screen("main")
    data object AudioUpload : Screen("audio_upload")
    data object Result : Screen("result")
    data object Settings : Screen("settings")
}