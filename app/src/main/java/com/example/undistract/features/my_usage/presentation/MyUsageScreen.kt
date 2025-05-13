package com.example.undistract.features.my_usage.presentation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.undistract.features.get_installed_apps.data.InstalledAppsRepository
import com.example.undistract.features.get_installed_apps.domain.AppInfo
import com.example.undistract.features.get_installed_apps.domain.GetInstalledAppsUseCase
import com.example.undistract.ui.components.UnderConstructionScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MyUsageScreen (context: Context, navController: NavHostController) {
    val context = LocalContext.current
    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val repository = InstalledAppsRepository(context)
        val useCase = GetInstalledAppsUseCase(repository)
        val apps = withContext(Dispatchers.IO) {
            useCase(context)
        }
        installedApps = apps
        isLoading = false
    }

    UnderConstructionScreen()
}