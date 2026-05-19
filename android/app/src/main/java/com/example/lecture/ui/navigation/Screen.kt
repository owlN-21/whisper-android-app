package com.example.lecture.ui.navigation

sealed class Screen(val route: String) {
    data object Loading : Screen("loading")
    data object Login : Screen("login")
    data object Main : Screen("main")
    data object AudioUpload : Screen("audio_upload")
    data object Settings : Screen("settings")

    data object Result : Screen("result/{taskId}") {
        fun createRoute(taskId: Long): String {
            return "result/$taskId"
        }
    }
}