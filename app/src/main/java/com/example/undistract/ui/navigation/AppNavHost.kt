package com.example.undistract.ui.navigation

import BottomNavigationBar
import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.undistract.config.AppDatabase
import com.example.undistract.core.ApiClient
import com.example.undistract.features.add_behavior.presentation.AddRestrictionScreen
import com.example.undistract.features.authentication.data.AuthenticationRepository
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
import com.example.undistract.features.block_permanent.presentation.BlockPermanentScreen
import com.example.undistract.features.block_schedules.presentation.BlockSchedulesScreen
import com.example.undistract.features.variable_session.data.VariableSessionRepository
import com.example.undistract.features.variable_session.presentation.VariableSessionViewModel
import com.example.undistract.features.variable_session.presentation.VariableSessionScreen
import com.example.undistract.features.parental_control.presentation.ParentalControlScreen
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepositoryImpl
import com.example.undistract.features.setadaily_limit.presentation.SetDailyUsageLimitScreen
import com.example.undistract.features.usage_limit.presentation.EditUsageLimitScreen
import com.example.undistract.features.usage_limit.presentation.UsageLimitViewModel
import com.example.undistract.features.usage_limit.presentation.UsageLimitViewModelFactory
import com.example.undistract.features.authentication.presentation.AuthenticationViewModel
import com.example.undistract.features.authentication.presentation.CreatePINScreen
import com.example.undistract.features.authentication.presentation.ResetPINScreen
import com.example.undistract.features.authentication.presentation.VerifyOTPScreen


@Composable
fun AppNavHost(context: Context) {
    val navController = rememberNavController()
    val database = AppDatabase.getDatabase(context)

    // Dapatkan API service
    val apiService = remember { ApiClient.apiService }

    // Dapatkan DAO dari database
    val blockSchedulesDao = database.blockSchedulesDao()
    val variableSessionDao = database.variableSessionDao()
    val blockPermanentDao = database.blockPermanentDao()
    val visitedUrlsDao = database.visitedUrlsDao()
    val pinDao = database.pinDao()

    // Inisialisasi repository
    val selectAppsRepository = remember { SelectAppsRepository() }
    val blockSchedulesRepository = remember { BlockSchedulesRepository(blockSchedulesDao) }
    val variableSessionRepository = remember { VariableSessionRepository(variableSessionDao) }
    val blockPermanentRepository = remember { BlockPermanentRepository(blockPermanentDao) }
    val visitedUrlsRepository = remember { VisitedUrlsRepository(visitedUrlsDao) }
    val authenticationRepository = remember { AuthenticationRepository(apiService, pinDao) }

    // Inisialisasi ViewModel
    val selectAppsViewModel: SelectAppsViewModel = viewModel(
        factory = SelectAppsViewModelFactory(context, visitedUrlsRepository = visitedUrlsRepository)
    )
    val authenticationViewModel = AuthenticationViewModel(authenticationRepository)

    // List rute yang tidak menggunakan navBar
    val routesWithoutNavBar = listOf(
        "add_restriction?isParental={isParental}",
        "add_restriction_main",
        "select_apps",
        "block_permanent?isParental={isParental}",
        "block_schedules?isParental={isParental}",
        "variable_session?isParental={isParental}",
        "set_daily_limit?isParental={isParental}",
        "editUsageLimit?isParental={isParental}",
        "createPin?email={email}",
        "verifyOtp?email={email}"
    )

    Scaffold(
        bottomBar = {
            val currentRoute =
                navController.currentBackStackEntryAsState().value?.destination?.route
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
                ParentalControlScreen(
                    navController = navController,
                    viewModel = authenticationViewModel
                )
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
                composable(
                    "parental_usage_limit?isParental={isParental}",
                    arguments = listOf(navArgument("isParental") {
                        defaultValue = false
                        type = NavType.BoolType
                    })
                ) { backStackEntry ->
                    val isParental = backStackEntry.arguments?.getBoolean("isParental") ?: false
                    UsageLimitScreen(
                        navController = navController,
                        context = context,
                        viewModel = selectAppsViewModel,
                        isParental = isParental
                    )
                }
                composable(
                    "add_restriction?isParental={isParental}",
                    arguments = listOf(navArgument("isParental") {
                        defaultValue = false
                        type = NavType.BoolType
                    })
                ) { backStackEntry ->
                    val isParental = backStackEntry.arguments?.getBoolean("isParental") ?: false
                    AddRestrictionScreen(
                        navController = navController,
                        isParental = isParental,
                    )
                }
                composable("select_apps") {
                    SelectAppsScreen(
                        navController = navController
                    )
                }

                composable(
                    "block_permanent?isParental={isParental}",
                    arguments = listOf(navArgument("isParental") {
                        defaultValue = false
                        type = NavType.BoolType
                    })
                ) { backStackEntry ->
                    val isParental = backStackEntry.arguments?.getBoolean("isParental") ?: false
                    BlockPermanentScreen(
                        navController = navController,
                        selectAppsViewModel = selectAppsViewModel,
                        repository = blockPermanentRepository,
                        isParental = isParental
                    )
                }

                composable(
                    "block_schedules?isParental={isParental}",
                    arguments = listOf(navArgument("isParental") {
                        defaultValue = false
                        type = NavType.BoolType
                    })
                ) { backStackEntry ->
                    val isParental = backStackEntry.arguments?.getBoolean("isParental") ?: false
                    BlockSchedulesScreen(
                        navController = navController,
                        isParental = isParental,
                        repository = blockSchedulesRepository,
                        selectAppViewModel = selectAppsViewModel
                    )
                }

                composable(
                    "variable_session?isParental={isParental}",
                    arguments = listOf(navArgument("isParental") {
                        defaultValue = false
                        type = NavType.BoolType
                    })
                ) { backStackEntry ->
                    val isParental = backStackEntry.arguments?.getBoolean("isParental") ?: false
                    VariableSessionScreen(
                        navController = navController,
                        repository = variableSessionRepository,
                        selectAppViewModel = selectAppsViewModel,
                        isParental = isParental
                    )
                }
                composable(
                    "set_daily_limit?isParental={isParental}",
                    arguments = listOf(navArgument("isParental") {
                        defaultValue = false
                        type = NavType.BoolType
                    })
                ) { backStackEntry ->
                    val isParental = backStackEntry.arguments?.getBoolean("isParental") ?: false
                    val usageLimitViewModel: UsageLimitViewModel = viewModel(
                        factory = UsageLimitViewModelFactory(
                            repository = SetaDailyLimitRepositoryImpl(
                                AppDatabase.getDatabase(context).setaDailyLimitDao()
                            ),
                            blockSchedulesRepository = blockSchedulesRepository,
                            variableSessionRepository = variableSessionRepository,
                            blockPermanentRepository = blockPermanentRepository,
                            isParental = isParental
                        )
                    )
                    SetDailyUsageLimitScreen(
                        navController = navController,
                        viewModel = selectAppsViewModel,
                        usageLimitViewModel = usageLimitViewModel,
                        isParental = isParental
                    )
                }
                composable(
                    "editUsageLimit?isParental={isParental}",
                    arguments = listOf(navArgument("isParental") {
                        defaultValue = false
                        type = NavType.BoolType
                    })
                ) { backStackEntry ->
                    val isParental = backStackEntry.arguments?.getBoolean("isParental") ?: false
                    EditUsageLimitScreen(
                        context = context,
                        navController = navController,
                        viewModel = selectAppsViewModel,
                        isParental = isParental
                    )
                }
                composable(
                    "verifyOtp?email={email}",
                    arguments = listOf(navArgument("email") {
                        defaultValue = ""
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    VerifyOTPScreen(
                        navController = navController,
                        viewModel = authenticationViewModel,
                        email = email
                    )
                }
                composable(
                    "createPin?email={email}",
                    arguments = listOf(navArgument("email") {
                        defaultValue = ""
                        type = NavType.StringType
                    })
                ) { backStackEntry ->
                    val email = backStackEntry.arguments?.getString("email") ?: ""
                    CreatePINScreen(
                        navController = navController,
                        viewModel = authenticationViewModel,
                        email = email
                    )
                }
                composable("resetPin") {
                    ResetPINScreen(
                        navController = navController,
                        viewModel = authenticationViewModel
                    )
                }
            }
        }
    }
}