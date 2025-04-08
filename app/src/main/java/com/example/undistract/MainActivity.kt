package com.example.undistract

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.undistract.features.get_installed_apps.data.InstalledAppsRepository
import com.example.undistract.features.get_installed_apps.domain.AppInfo
import com.example.undistract.features.get_installed_apps.domain.GetInstalledAppsUseCase
import com.example.undistract.features.usage_monitor.UsageMonitorService
import com.example.undistract.ui.navigation.AppNavHost
import com.example.undistract.ui.theme.UndistractTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private lateinit var installedApps: List<AppInfo>

    // Permission launcher for notification permission
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startMonitoringService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check and request notification permission
        checkNotificationPermission()

        // Initialize data and UI
        initializeDataAndUI()
    }

    private fun initializeDataAndUI() {
        lifecycleScope.launch {
            // Load installed apps in background thread
            installedApps = loadInstalledApps()

            // Set content in main thread
            setContent {
                UndistractTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavHost(
                            context = this@MainActivity,
                            installedApps = installedApps
                        )
                    }
                }
            }
        }
    }

    private suspend fun loadInstalledApps(): List<AppInfo> {
        return withContext(Dispatchers.IO) {
            val repository = InstalledAppsRepository(this@MainActivity)
            val getInstalledAppsUseCase = GetInstalledAppsUseCase(repository)
            getInstalledAppsUseCase(this@MainActivity)
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                    startMonitoringService()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Show rationale if needed
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Request permission
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For devices below Android 13, start the service directly
            startMonitoringService()
        }
    }

    private fun startMonitoringService() {
        UsageMonitorService.startService(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Don't stop the service when the activity is destroyed
        // The service should continue running in the background
    }
}