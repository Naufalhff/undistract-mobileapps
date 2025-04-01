package com.example.undistract.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel

@Composable
fun SelectedAppsRouteObserver(
    navController: NavController,
    viewModel: SelectAppsViewModel
) {
    // Pantau perubahan rute
    val currentRoute = navController.currentBackStackEntry?.destination?.route

    // Update viewModel saat rute berubah
    LaunchedEffect(currentRoute) {
        currentRoute?.let {
            viewModel.updateCurrentRoute(it)
        }
    }

    // Pantau perubahan rute saat navigasi
    DisposableEffect(navController) {
        val listener = NavController.OnDestinationChangedListener { _, destination, _ ->
            viewModel.onRouteChanged(destination.route)
        }

        navController.addOnDestinationChangedListener(listener)

        onDispose {
            navController.removeOnDestinationChangedListener(listener)
        }
    }
}