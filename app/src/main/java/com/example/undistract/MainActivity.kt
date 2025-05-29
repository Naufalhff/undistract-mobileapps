package com.example.undistract

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.widget.Toast
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
import com.example.undistract.core.AppAccessibilityService
import com.example.undistract.features.usage_monitor.UsageMonitorService
import com.example.undistract.ui.navigation.AppNavHost
import com.example.undistract.ui.theme.UndistractTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    companion object {
        const val REQUEST_CODE_OVERLAY_PERMISSION = 1001
    }

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

        // Tampilkan layout splash screen
        setContentView(R.layout.activity_splash)

        // Delay selama 2 detik sebelum menampilkan UI utama
        Handler(Looper.getMainLooper()).postDelayed({
            enableEdgeToEdge()

            checkNotificationPermission()
            checkOverlayPermission()
            checkAndPromptAccessibilityService()
            startMonitoringService()

            lifecycleScope.launch {
                setContent {
                    UndistractTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            AppNavHost(
                                context = this@MainActivity,
                            )
                        }
                    }
                }
            }
        }, 2000)
    }

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
            Toast.makeText(
                this,
                "Aplikasi memerlukan izin untuk menampilkan dialog di atas aplikasi lain",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Sudah diizinkan
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun checkAndPromptAccessibilityService() {
        if (!isAccessibilityServiceEnabled(this, AppAccessibilityService::class.java)) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    private fun isAccessibilityServiceEnabled(
        context: Context,
        service: Class<out AccessibilityService>
    ): Boolean {
        val expectedComponentName = ComponentName(context, service)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)
        while (colonSplitter.hasNext()) {
            val componentName = ComponentName.unflattenFromString(colonSplitter.next())
            if (componentName != null && componentName == expectedComponentName) {
                return true
            }
        }
        return false
    }

    private fun startMonitoringService() {
        UsageMonitorService.startService(this)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
