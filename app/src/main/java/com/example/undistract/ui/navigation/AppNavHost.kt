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
import androidx.navigation.navigation
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.add_behavior.presentation.AddRestrictionScreen
import com.example.undistract.features.block_permanent.data.BlockPermanentRepository
import com.example.undistract.features.block_permanent.presentation.BlockPermanentViewModel
import com.example.undistract.features.block_schedules.data.BlockSchedulesRepository
import com.example.undistract.features.block_schedules.presentation.BlockSchedulesViewModel
import com.example.undistract.features.get_visited_urls.data.VisitedUrlsRepository
import com.example.undistract.features.my_usage.presentation.MyUsageScreen
import com.example.undistract.features.parental_control.presentation.ParentalControlScreen
import com.example.undistract.features.profile.presentation.ProfileScreen
import com.example.undistract.features.select_apps.data.SelectAppsRepository
import com.example.undistract.features.select_apps.presentation.SelectAppsScreen
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModelFactory
import com.example.undistract.features.usage_limit.presentation.EditUsageLimitScreen
import com.example.undistract.features.usage_limit.presentation.UsageLimitScreen
import com.example.undistract.features.variable_session.data.VariableSessionRepository
import com.example.undistract.features.variable_session.presentation.VariableSessionViewModel


@Composable
fun AppNavHost(context: Context) {
    val navController = rememberNavController()
    val database = AppDatabase.getDatabase(context)

    // Dapatkan DAO dari database
    val blockSchedulesDao = database.blockSchedulesDao()
    val variableSessionDao = database.variableSessionDao()
    val blockPermanentDao = database.blockPermanentDao()
    val visitedUrlsDao = database.visitedUrlsDao()

    // Inisialisasi repository
    val selectAppsRepository = remember { SelectAppsRepository() }
    val blockSchedulesRepository = remember { BlockSchedulesRepository(blockSchedulesDao) }
    val variableSessionRepository = remember { VariableSessionRepository(variableSessionDao) }
    val blockPermanentRepository = remember { BlockPermanentRepository(blockPermanentDao) }
    val visitedUrlsRepository = remember { VisitedUrlsRepository(visitedUrlsDao) }

    // Inisialisasi ViewModel
    val selectAppsViewModel: SelectAppsViewModel = viewModel(
        factory = SelectAppsViewModelFactory(context, visitedUrlsRepository = visitedUrlsRepository)
    )
    val blockSchedulesViewModel = BlockSchedulesViewModel(blockSchedulesRepository)
    val variableSessionViewModel = VariableSessionViewModel(variableSessionRepository)
    val blockPermanentViewModel = BlockPermanentViewModel(blockPermanentRepository)

    // List rute yang tidak menggunakan navBar
    val routesWithoutNavBar = listOf(
        "add_restriction",
        "select_apps",
        "block_permanent",
        "block_schedules",
        "variable_session",
        "set_daily_limit",
        "editUsageLimit",
        "add_restriction_main"
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
                UsageLimitScreen(
                    navController = navController,
                    context = context,
                    viewModel = selectAppsViewModel
                )
            }
            composable(BottomNavItem.ParentalControl.route) {
                ParentalControlScreen(navController = navController, context = context)
            }
            composable(BottomNavItem.Profile.route) {
                ProfileScreen(navController = navController, context = context)
            }

            navigation(
                startDestination = "add_restriction_main",
                route = "add_restriction"
            ) {
                composable("add_restriction_main")
                {
                    AddRestrictionScreen(navController = navController)
                }
                composable("select_apps")
                {
                    SelectAppsScreen(navController = navController)
                }
            }

            composable("editUsageLimit") {
                EditUsageLimitScreen(
                    context = context,
                    navController = navController
                )
            }
        }
    }
}