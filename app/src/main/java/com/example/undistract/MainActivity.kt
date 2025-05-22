package com.example.undistract

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        const val REQUEST_CODE_OVERLAY_PERMISSION = 1001
    }

    private lateinit var installedApps: List<AppInfo>

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Start the monitoring service if permission is granted
            startMonitoringService()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display (from second implementation)
        enableEdgeToEdge()

        // Check and request notification permission on Android 13+
        checkNotificationPermission()
        
        // Check and request overlay permission
        checkOverlayPermission()

        // Start the monitoring service
        startMonitoringService()

        val repository = InstalledAppsRepository(this)
        val getInstalledAppsUseCase = GetInstalledAppsUseCase(repository)

        lifecycleScope.launch {
            installedApps = getInstalledAppsUseCase(this@MainActivity)

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

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
            Toast.makeText(this, 
                "Aplikasi memerlukan izin untuk menampilkan dialog di atas aplikasi lain", 
                Toast.LENGTH_LONG).show()
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