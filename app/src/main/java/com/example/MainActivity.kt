package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.local.TraceDatabase
import com.example.data.repository.TraceRepository
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ParentsScreen
import com.example.ui.screens.ProfileSelectionScreen
import com.example.ui.screens.RewardsScreen
import com.example.ui.screens.DiplomaScreen
import com.example.ui.screens.TracingScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.TracingViewModel
import com.example.ui.viewmodel.TracingViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize local SQLite database & repository offline
    val database = TraceDatabase.getDatabase(applicationContext)
    val repository = TraceRepository(database.traceDao())
    val factory = TracingViewModelFactory(application, repository)

    setContent {
      MyApplicationTheme {
        val navController = rememberNavController()
        
        // Retrieve standard ViewModel bound to the app context
        val viewModel: TracingViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
            factory = factory
        )

        NavHost(
          navController = navController,
          startDestination = "profiles",
          modifier = Modifier.fillMaxSize()
        ) {
          // 0. Eye-catching Child Welcome Screen
          composable("welcome") {
            WelcomeScreen(
              viewModel = viewModel,
              onStartGame = {
                navController.navigate("dashboard") {
                  popUpTo("profiles") { inclusive = true }
                }
              }
            )
          }

          // 0.25. Profile Selection Screen
          composable("profiles") {
            ProfileSelectionScreen(
              viewModel = viewModel,
              onProfileSelected = {
                navController.navigate("dashboard") {
                  popUpTo("profiles") { inclusive = true }
                }
              },
              onCreateNewProfile = {
                navController.navigate("onboarding")
              },
              onNavigateToParents = {
                navController.navigate("parents")
              }
            )
          }

          // 0.5. Child Onboarding / Profile Setup Screen
          composable("onboarding") {
            OnboardingScreen(
              viewModel = viewModel,
              onNavigateToDashboard = {
                navController.navigate("dashboard") {
                  popUpTo("onboarding") { inclusive = true }
                }
              }
            )
          }

          // 1. Interactive Child Dashboard
          composable("dashboard") {
            DashboardScreen(
              viewModel = viewModel,
              onNavigateToTracing = { charId ->
                navController.navigate("tracing/$charId")
              },
              onNavigateToParents = {
                navController.navigate("parents")
              },
              onNavigateToProfiles = {
                navController.navigate("profiles") {
                  popUpTo("dashboard") { inclusive = true }
                }
              },
              onNavigateToRewards = {
                navController.navigate("rewards")
              },
              onNavigateToDiploma = {
                navController.navigate("diploma")
              }
            )
          }

          // 2. Tactile Tracing Canvas Panel
          composable(
            route = "tracing/{charId}",
            arguments = listOf(navArgument("charId") { type = NavType.StringType })
          ) { backStackEntry ->
            val charId = backStackEntry.arguments?.getString("charId") ?: "1"
            TracingScreen(
              viewModel = viewModel,
              characterId = charId,
              onNavigateBack = {
                navController.popBackStack()
              }
            )
          }

          // 3. Parental Progress Report Zone
          composable("parents") {
            ParentsScreen(
              viewModel = viewModel,
              onNavigateBack = {
                navController.popBackStack()
              }
            )
          }

          // 4. Rewards Album Zone
          composable("rewards") {
            RewardsScreen(
              viewModel = viewModel,
              onNavigateBack = {
                navController.popBackStack()
              },
              onNavigateToDiploma = {
                navController.navigate("diploma")
              }
            )
          }

          // 5. Diploma Zone
          composable("diploma") {
            DiplomaScreen(
              viewModel = viewModel,
              onNavigateBack = {
                navController.popBackStack()
              }
            )
          }
        }
      }
    }
  }
}
