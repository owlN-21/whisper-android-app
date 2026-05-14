package com.example.lecture.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lecture.App
import com.example.lecture.data.network.NetworkModule
import com.example.lecture.data.repository.LoginRepository
import com.example.lecture.ui.screens.loading.LoadingScreen
import com.example.lecture.ui.screens.login.LoginScreen
import com.example.lecture.ui.screens.login.LoginViewModel
import com.example.lecture.ui.screens.main.MainScreen
import com.example.lecture.ui.screens.main.MainViewModel
import com.example.lecture.ui.screens.result.ResultScreen
import com.example.lecture.ui.screens.settings.SettingsScreen
import com.example.lecture.ui.screens.upload.AudioUploadScreen
import kotlinx.coroutines.launch
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.lecture.ui.screens.main.MainViewModelFactory


@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    val context = LocalContext.current
    val app = context.applicationContext as App
    val coroutineScope = rememberCoroutineScope()

    val userPreferences by app.userPreferencesRepository.userPreferences.collectAsState(
        initial = null
    )

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
        startDestination = Screen.Loading.route
    ) {
        composable(Screen.Loading.route) {
            LoadingScreen()

            LaunchedEffect(userPreferences) {
                val preferences = userPreferences

                if (preferences != null) {
                    val isSessionValid =
                        preferences.isLoggedIn &&
                                preferences.userId != null &&
                                !preferences.email.isNullOrBlank()

                    val startRoute =
                        if (isSessionValid) {
                            Screen.Main.route
                        } else {
                            Screen.Login.route
                        }

                    navController.navigate(startRoute) {
                        popUpTo(Screen.Loading.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            }
        }

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Screen.Main.route) {
            val mainViewModel: MainViewModel = viewModel(
                factory = MainViewModelFactory(
                    userPreferencesRepository = app.userPreferencesRepository,
                    taskDao = app.database.taskDao()
                )
            )

            val mainUiState by mainViewModel.uiState.collectAsState()

            MainScreen(
                uiState = mainUiState,
                onUploadClick = {
                    navController.navigate(Screen.AudioUpload.route)
                },
                onTaskClick = { taskId ->
                    navController.navigate(Screen.Result.createRoute(taskId))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(Screen.AudioUpload.route) {
            AudioUploadScreen(
                onSendAudioClick = {
                    navController.navigate(Screen.Main.route)
                },
                onBackClick = {
                    navController.navigate(Screen.Main.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("taskId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->

            val taskId = backStackEntry.arguments?.getLong("taskId")

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
                            popUpTo(0) {
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