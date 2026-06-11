package com.example.quizloop

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.quizloop.feature.auth.AuthScreen
import com.example.quizloop.feature.auth.AuthViewModel
import com.example.quizloop.feature.auth.AuthViewModelFactory
import com.example.quizloop.feature.create.CreateQuizScreen
import com.example.quizloop.feature.home.HomeScreen
import com.example.quizloop.ui.game.GameScreen
import com.example.quizloop.ui.game.GameViewModel
import com.example.quizloop.ui.game.GameViewModelFactory
import com.example.quizloop.ui.multiplayer.MultiplayerLobbyScreen
import com.example.quizloop.ui.settings.SettingsScreen
import com.example.quizloop.feature.profile.ProfileScreen
import com.example.quizloop.feature.profile.ProfileViewModel
import com.example.quizloop.feature.profile.ProfileViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizLoopApp(viewModel: QuizLoopViewModel, mainActivity: MainActivity) {
    val navController = rememberNavController()
    val appContainer = (mainActivity.application as QuizLoopApplication).container

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("QuizLoop", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        val quizzes by viewModel.quizzes.collectAsState()
        NavHost(
            navController = navController,
            startDestination = "auth",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("auth") {
                val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(appContainer.authRepository))
                AuthScreen(
                    authViewModel = authViewModel,
                    onContinueAsGuest = {
                        navController.navigate("home") {
                            popUpTo("auth") { inclusive = true }
                        }
                    },
                    onLoginSuccess = {
                        navController.navigate("home") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                )
            }
            composable("home") {
                HomeScreen(
                    quizzes = quizzes,
                    onNavigateToCreateQuiz = { navController.navigate("create_quiz") },
                    onNavigateToMultiplayer = { navController.navigate("multiplayer_lobby") },
                    onNavigateToSinglePlayer = { quizId -> navController.navigate("game?quizId=$quizId") },
                    onNavigateToProfile = { navController.navigate("profile") }
                )
            }
            composable("create_quiz") {
                CreateQuizScreen(
                    onSaveQuiz = { quiz -> viewModel.addQuiz(quiz) },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("multiplayer_lobby") {
                MultiplayerLobbyScreen(
                    quizzes = quizzes,
                    onNavigateToGame = { quizId, roomCode ->
                        val route = if (roomCode != null) {
                            "game?roomCode=$roomCode"
                        } else {
                            "game?quizId=$quizId"
                        }
                        navController.navigate(route)
                    }
                )
            }
            composable(
                route = "game?quizId={quizId}&roomCode={roomCode}",
                arguments = listOf(
                    navArgument("quizId") { nullable = true; type = NavType.StringType },
                    navArgument("roomCode") { nullable = true; type = NavType.StringType }
                )
            ) {
                backStackEntry ->
                val gameViewModel: GameViewModel = viewModel(
                    factory = GameViewModelFactory(appContainer.quizRepository, appContainer.userProfileRepository)
                )

                val quizId = backStackEntry.arguments?.getString("quizId")
                val roomCode = backStackEntry.arguments?.getString("roomCode")

                gameViewModel.initGame(quizId, roomCode)

                GameScreen(viewModel = gameViewModel)
            }
            composable("settings") {
                SettingsScreen(onBack = { navController.popBackStack() })
            }
            composable("profile") {
                val profileViewModel: ProfileViewModel = viewModel(factory = ProfileViewModelFactory(appContainer.userProfileRepository))
                ProfileScreen(viewModel = profileViewModel)
            }
        }
    }
}
