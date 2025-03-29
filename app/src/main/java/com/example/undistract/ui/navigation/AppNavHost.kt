package com.example.undistract.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.undistract.features.add_behavior.presentation.AddRestrictionScreen
import com.example.undistract.features.select_apps.presentation.SelectAppsScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "add_restriction"
    ) {
        composable("add_restriction") {
            AddRestrictionScreen(navController)
        }
        composable("select_apps") {
            SelectAppsScreen()
        }
    }
}