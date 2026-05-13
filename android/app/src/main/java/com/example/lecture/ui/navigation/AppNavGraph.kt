package com.example.lecture.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lecture.ui.screens.login.LoginScreen
import com.example.lecture.ui.screens.main.MainScreen
import com.example.lecture.ui.screens.result.ResultScreen
import com.example.lecture.ui.screens.settings.SettingsScreen
import com.example.lecture.ui.screens.upload.AudioUploadScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                onUploadClick = {
                    navController.navigate(Screen.AudioUpload.route)
                },
                onOpenResultClick = {
                    navController.navigate(Screen.Result.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.AudioUpload.route) {
            AudioUploadScreen(
                onSendAudioClick = {
                    navController.navigate(Screen.Result.route)
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                onBackToMainClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) {
                            inclusive = false
                        }
                    }
                },
                onUploadAnotherAudioClick = {
                    navController.navigate(Screen.AudioUpload.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}