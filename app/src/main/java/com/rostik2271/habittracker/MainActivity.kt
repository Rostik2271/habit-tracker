package com.rostik2271.habittracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rostik2271.habittracker.ui.HabitDetailScreen
import com.rostik2271.habittracker.ui.HabitListScreen
import com.rostik2271.habittracker.ui.MonthOverviewScreen
import com.rostik2271.habittracker.viewmodel.HabitViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HabitTrackerTheme {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    HabitTrackerApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun HabitTrackerApp(viewModel: HabitViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "list") {
        composable("list") {
            HabitListScreen(
                viewModel = viewModel,
                onHabitClick = { habitId ->
                    navController.navigate("detail/$habitId")
                },
                onMonthClick = {
                    navController.navigate("month")
                }
            )
        }
        composable("detail/{habitId}") { backStackEntry ->
            val habitId = backStackEntry.arguments?.getString("habitId")?.toLongOrNull() ?: 0L
            HabitDetailScreen(
                habitId = habitId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("month") {
            MonthOverviewScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun HabitTrackerTheme(content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
