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
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.add_behavior.presentation.AddRestrictionScreen
import com.example.undistract.features.block_permanent.data.BlockPermanentRepository
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
import com.example.undistract.features.block_schedules.data.BlockSchedulesRepository
import com.example.undistract.features.block_schedules.data.local.BlockSchedulesDao
import com.example.undistract.features.block_schedules.presentation.BlockSchedulesScreen
import com.example.undistract.features.block_schedules.presentation.BlockSchedulesViewModel
import com.example.undistract.features.variable_session.data.VariableSessionRepository
import com.example.undistract.features.variable_session.presentation.VariableSessionScreen
import com.example.undistract.features.variable_session.presentation.VariableSessionViewModel
import com.example.undistract.navigation.SelectedAppsRouteObserver
import com.example.undistract.features.block_permanent.presentation.BlockPermanentViewModel
import com.example.undistract.features.block_permanent.presentation.BlockPermanentViewModelFactory
import com.example.undistract.features.block_permanent.data.local.BlockPermanentDao
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepositoryImpl
import com.example.undistract.features.setadaily_limit.presentation.SetDailyUsageLimitScreen
import com.example.undistract.features.usage_limit.presentation.EditUsageLimitScreen
import com.example.undistract.features.usage_limit.presentation.UsageLimitViewModel
import com.example.undistract.features.usage_limit.presentation.UsageLimitViewModelFactory


@Composable
fun AppNavHost(context: Context, installedApps: List<AppInfo>) {
    val navController = rememberNavController()
    val database = AppDatabase.getDatabase(context)



    // Dapatkan DAO dari database
    val blockSchedulesDao = database.blockSchedulesDao()
    val variableSessionDao = database.variableSessionDao()
    val blockPermanentDao = database.blockPermanentDao()

    // Inisialisasi repository & dao
    val selectAppsRepository = remember { SelectAppsRepository() }
    val blockSchedulesRepository = remember { BlockSchedulesRepository(blockSchedulesDao) }
    val variableSessionRepository = remember { VariableSessionRepository(variableSessionDao) }
    val blockPermanentRepository = remember { BlockPermanentRepository(blockPermanentDao) }

    // Inisialisasi ViewModel
    val selectAppsViewModel: SelectAppsViewModel = viewModel(
        factory = SelectAppsViewModelFactory(context, selectAppsRepository)
    )
    val blockSchedulesViewModel = BlockSchedulesViewModel(blockSchedulesRepository)
    val variableSessionViewModel = VariableSessionViewModel(variableSessionRepository)
    val blockPermanentViewModel = BlockPermanentViewModel(blockPermanentRepository)

    // Observer untuk memantau perubahan rute
    SelectedAppsRouteObserver(navController, selectAppsViewModel)

    // List rute yang tidak menggunakan navBar
    val routesWithoutNavBar = listOf(
        "add_restriction",
        "select_apps",
        "block_permanent",
        "block_schedules",
        "variable_session",
        "set_daily_limit",
        "editUsageLimit"

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
                    selectAppsViewModel = selectAppsViewModel,
                    blockPermanentViewModel = blockPermanentViewModel
                )
            }
            composable("block_schedules")
            {
                BlockSchedulesScreen(
                    navController = navController,
                    viewModel = blockSchedulesViewModel,
                    selectAppViewModel = selectAppsViewModel
                )
            }
            composable("variable_session")
            {
                VariableSessionScreen(
                    navController = navController,
                    viewModel = variableSessionViewModel,
                    selectAppViewModel = selectAppsViewModel
                )
            }

            composable("set_daily_limit") {
                val usageLimitViewModel: UsageLimitViewModel = viewModel(
                    factory = UsageLimitViewModelFactory(
                        repository = SetaDailyLimitRepositoryImpl(
                            AppDatabase.getDatabase(context).setaDailyLimitDao()
                        ),
                        blockSchedulesRepository = blockSchedulesRepository, // Tambahkan ini
                        variableSessionRepository = variableSessionRepository,
                        blockPermanentRepository = blockPermanentRepository
                    )
                )
                SetDailyUsageLimitScreen(
                    navController = navController,
                    viewModel = selectAppsViewModel,
                    usageLimitViewModel = usageLimitViewModel
                )
            }

            composable("editUsageLimit") {
                EditUsageLimitScreen(
                    context = context,
                    navController = navController,
                    viewModel = selectAppsViewModel,
                )
            }
        }
    }
}