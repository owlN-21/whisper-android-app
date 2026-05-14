package com.example.lecture.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lecture.App
import com.example.lecture.data.network.NetworkModule
import com.example.lecture.data.repository.LoginRepository
import com.example.lecture.ui.screens.login.LoginScreen
import com.example.lecture.ui.screens.login.LoginViewModel
import com.example.lecture.ui.screens.main.MainScreen
import com.example.lecture.ui.screens.result.ResultScreen
import com.example.lecture.ui.screens.settings.SettingsScreen
import com.example.lecture.ui.screens.upload.AudioUploadScreen
import kotlinx.coroutines.launch

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    val context = LocalContext.current
    val app = context.applicationContext as App
    val coroutineScope = rememberCoroutineScope()

    val loginRepository = remember {
        LoginRepository(
            apiService = NetworkModule.apiService
        )
    }

    val loginViewModel = remember {
        LoginViewModel(
            loginRepository = loginRepository,
            userPreferencesRepository = app.userPreferencesRepository
        )
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
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
                    navController.navigate(Screen.Main.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                onBackToMainClick = {
                    navController.navigate(Screen.Main.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onUploadAnotherAudioClick = {
                    navController.navigate(Screen.AudioUpload.route)
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onLogoutClick = {
                    coroutineScope.launch {
                        app.userPreferencesRepository.clearUser()

                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Main.route) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}