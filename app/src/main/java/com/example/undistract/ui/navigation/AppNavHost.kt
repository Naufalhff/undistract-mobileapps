package com.example.undistract.ui.navigation

import BottomNavigationBar
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.undistract.features.my_usage.presentation.*
import com.example.undistract.features.usage_limit.presentation.*
import com.example.undistract.features.parental_control.presentation.*
import com.example.undistract.features.profile.presentation.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.undistract.features.add_behavior.presentation.AddRestrictionScreen
import com.example.undistract.features.select_apps.presentation.SelectAppsScreen

@Composable
fun AppNavHost(context: Context) {
fun AppNavHost() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "myUsage",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.MyUsage.route) {
                MyUsageScreen(navController = navController, context = context)
            }
            composable(BottomNavItem.UsageLimit.route) {
                UsageLimitScreen(navController = navController, context = context)
            }
            composable(BottomNavItem.ParentalControl.route) {
                ParentalControlScreen(navController = navController, context = context)
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(navController = navController, context = context)
            }
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

