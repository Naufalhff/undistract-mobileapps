package com.example.undistract.features.usage_limit.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.undistract.ui.theme.Purple40
import com.example.undistract.features.usage_stats.UsageStatsManager
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepository
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepositoryImpl
import kotlinx.coroutines.launch

class DailyLimitDialogActivity : ComponentActivity() {

    private lateinit var setaDailyLimitRepository: SetaDailyLimitRepository
    private lateinit var usageStatsManager: UsageStatsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inisialisasi database dan repository
        val database = AppDatabase.getDatabase(this)
        setaDailyLimitRepository = SetaDailyLimitRepositoryImpl(database.setaDailyLimitDao())
        usageStatsManager = UsageStatsManager(this)

        // Tambahkan log untuk debugging
        Log.d("DailyLimitDialogActivity", "Activity created")

        val packageName = intent.getStringExtra("PACKAGE_NAME")
        val appName = intent.getStringExtra("APP_NAME") ?: "this app"

        // Periksa ulang batas penggunaan saat dialog dibuka
        lifecycleScope.launch {
            val limit = setaDailyLimitRepository.getByPackageName(packageName ?: "")
            if (limit != null) {
                val usageTimeMinutes = usageStatsManager.getAppUsageTimeToday(packageName ?: "")
                
                if (usageTimeMinutes >= limit.timeLimitMinutes) {
                    setContent {
                        DailyLimitDialogContent(
                            appName = appName,
                            onDismiss = {
                                // Reset cache penggunaan untuk aplikasi ini
                                usageStatsManager.clearCacheFor(packageName ?: "")
                                closeApp(packageName)
                                finish()
                            }
                        )
                    }
                } else {
                    // Jika tidak melewati batas, langsung tutup aktivitas
                    finish()
                }
            } else {
                finish()
            }
        }
    }

    private fun closeApp(packageName: String?) {
        if (packageName != null) {
            val homeIntent = Intent(Intent.ACTION_MAIN)
            homeIntent.addCategory(Intent.CATEGORY_HOME)
            homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(homeIntent)
        }
    }
}

@Composable
fun DailyLimitDialogContent(appName: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Daily Limit Reached",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "You have reached your daily usage limit for $appName. Please take a break!",
                fontSize = 16.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Purple40)
            ) {
                Text("OK", color = Color.White)
            }
        }
    )
}