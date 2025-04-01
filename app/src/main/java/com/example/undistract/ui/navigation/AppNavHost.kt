package com.example.undistract.ui.navigation

import BottomNavigationBar
import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.undistract.features.add_behavior.presentation.AddRestrictionScreen
import com.example.undistract.features.get_installed_apps.domain.AppInfo
import com.example.undistract.features.my_usage.presentation.MyUsageScreen
import com.example.undistract.features.parental_control.presentation.ParentalControlScreen
import com.example.undistract.features.profile.presentation.ProfileScreen
import com.example.undistract.features.select_apps.data.SelectAppsRepository
import com.example.undistract.features.select_apps.presentation.SelectAppsScreen
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModelFactory
import com.example.undistract.features.usage_limit.presentation.UsageLimitScreen
import com.example.undistract.features.block_permanent.presentation.BlockPermanentScreen
import com.example.undistract.navigation.SelectedAppsRouteObserver


@Composable
fun AppNavHost(context: Context, installedApps: List<AppInfo>) {
    val navController = rememberNavController()

    // Inisialisasi repository
    val selectAppsRepository = remember { SelectAppsRepository() }

    // Inisialisasi ViewModel
    val selectAppsViewModel: SelectAppsViewModel = viewModel(
        factory = SelectAppsViewModelFactory(context, selectAppsRepository)
    )

    // Observer untuk memantau perubahan rute
    SelectedAppsRouteObserver(navController, selectAppsViewModel)

    // List rute yang tidak menggunakan navBar
    val routesWithoutNavBar = listOf(
        "add_restriction",
        "select_apps",
        "block_permanent"
    )

    Scaffold(
        bottomBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            if (currentRoute !in routesWithoutNavBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.MyUsage.route,
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
            composable("add_restriction") {
                AddRestrictionScreen(
                    navController = navController,
                    viewModel = selectAppsViewModel)
            }
            composable("select_apps") {
                SelectAppsScreen(
                    context = context,
                    navController = navController,
                    viewModel = selectAppsViewModel
                )
            }
            composable("block_permanent")
            {
                BlockPermanentScreen(
                    navController = navController,
                    viewModel = selectAppsViewModel
                )
            }
        }
    }
}
