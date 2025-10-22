package com.kevin.receipttrackr

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.kevin.receipttrackr.ui.DetailsScreen
import com.kevin.receipttrackr.ui.HomeScreen
import com.kevin.receipttrackr.ui.ImportScreen
import com.kevin.receipttrackr.ui.ReviewScreen
import com.kevin.receipttrackr.ui.SettingsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Import : Screen("import")
    object Review : Screen("review/{imageUri}") {
        fun createRoute(imageUri: String) = "review/${Uri.encode(imageUri)}"
    }
    object Details : Screen("details/{receiptId}") {
        fun createRoute(receiptId: Long) = "details/$receiptId"
    }
    object Settings : Screen("settings")
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onImportClick = { navController.navigate(Screen.Import.route) },
                onReceiptClick = { receiptId ->
                    navController.navigate(Screen.Details.createRoute(receiptId))
                },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Import.route) {
            ImportScreen(
                onImageSelected = { uri ->
                    navController.navigate(Screen.Review.createRoute(uri)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Review.route,
            arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUri = backStackEntry.arguments?.getString("imageUri") ?: ""
            val imageUri = Uri.decode(encodedUri)
            ReviewScreen(
                imageUri = imageUri,
                onSaved = { receiptId ->
                    navController.navigate(Screen.Details.createRoute(receiptId)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Details.route,
            arguments = listOf(navArgument("receiptId") { type = NavType.LongType })
        ) { backStackEntry ->
            val receiptId = backStackEntry.arguments?.getLong("receiptId") ?: 0L
            DetailsScreen(
                receiptId = receiptId,
                onBack = { navController.popBackStack() },
                onEdit = { /* TODO: implement edit flow */ }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
