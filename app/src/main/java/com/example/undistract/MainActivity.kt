package com.example.undistract

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.undistract.features.get_installed_apps.data.InstalledAppsRepository
import com.example.undistract.features.get_installed_apps.domain.AppInfo
import com.example.undistract.features.get_installed_apps.domain.GetInstalledAppsUseCase
import com.example.undistract.ui.navigation.AppNavHost
import com.example.undistract.ui.theme.UndistractTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var installedApps: List<AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = InstalledAppsRepository(this)
        val getInstalledAppsUseCase = GetInstalledAppsUseCase(repository)

        lifecycleScope.launch {
            installedApps = getInstalledAppsUseCase(this@MainActivity)

            setContent {
                UndistractTheme {
                    AppNavHost(context = this@MainActivity, installedApps = installedApps)
                }
            }
        }
    }
}