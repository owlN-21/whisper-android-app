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
import com.example.lecture.ui.screens.result.ResultViewModel
import com.example.lecture.ui.screens.result.ResultViewModelFactory
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.lecture.data.repository.AudioUploadRepository
import com.example.lecture.data.repository.SettingsRepository
import com.example.lecture.ui.screens.main.MainViewModelFactory
import com.example.lecture.ui.screens.settings.SettingsViewModel
import com.example.lecture.ui.screens.settings.SettingsViewModelFactory
import com.example.lecture.ui.screens.upload.AudioUploadViewModel
import com.example.lecture.ui.screens.upload.AudioUploadViewModelFactory


@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    val context = LocalContext.current
    val app = context.applicationContext as App

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
            val audioUploadRepository = remember {
                AudioUploadRepository(
                    contentResolver = context.contentResolver,
                    apiService = NetworkModule.apiService
                )
            }

            val audioUploadViewModel: AudioUploadViewModel = viewModel(
                factory = AudioUploadViewModelFactory(
                    userPreferencesRepository = app.userPreferencesRepository,
                    audioUploadRepository = audioUploadRepository,
                    taskDao = app.database.taskDao(),
                    summaryDao = app.database.summaryDao(),
                    transcriptDao = app.database.transcriptDao()
                )
            )

            AudioUploadScreen(
                viewModel = audioUploadViewModel,
                onResultReady = { localTaskId ->
                    navController.navigate(Screen.Result.createRoute(localTaskId)) {
                        popUpTo(Screen.AudioUpload.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onBackClick = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.AudioUpload.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
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

            val taskId = backStackEntry.arguments?.getLong("taskId") ?: return@composable

            val resultViewModel: ResultViewModel = viewModel(
                factory = ResultViewModelFactory(
                    taskId = taskId,
                    summaryDao = app.database.summaryDao(),
                    transcriptDao = app.database.transcriptDao(),
                    taskDao = app.database.taskDao(),
                    audioUploadRepository = AudioUploadRepository(
                        contentResolver = context.contentResolver,
                        apiService = NetworkModule.apiService
                    )
                )
            )

            ResultScreen(
                viewModel = resultViewModel,
                onBackToMainClick = {
                    navController.navigate(Screen.Main.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onUploadAnotherAudioClick = {
                    navController.navigate(Screen.AudioUpload.route)
                },
                onDeleted = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) {
                            inclusive = false
                        }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            val context = LocalContext.current
            val app = context.applicationContext as App

            val settingsRepository = remember {
                SettingsRepository(NetworkModule.apiService)
            }

            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(
                    userPreferencesRepository = app.userPreferencesRepository,
                    settingsRepository = settingsRepository,
                    baseUrl = NetworkModule.BASE_URL
                )
            )

            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}