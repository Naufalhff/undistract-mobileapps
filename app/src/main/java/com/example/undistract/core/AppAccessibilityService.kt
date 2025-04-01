package com.example.undistract.core

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import androidx.compose.runtime.Composable
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.block_schedules.data.local.BlockSchedulesDao
import com.example.undistract.features.block_schedules.domain.BlockScheduleManager
import com.example.undistract.features.variable_session.data.VariableSessionRepository
import com.example.undistract.features.variable_session.domain.VariableSessionManager
import com.example.undistract.features.variable_session.presentation.VariableSessionDialogActivity
import com.example.undistract.features.variable_session.presentation.VariableSessionViewModel
import com.google.firebase.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime

class AppAccessibilityService : AccessibilityService() {

    private lateinit var blockScheduleManager: BlockScheduleManager
    private lateinit var variableSessionManager: VariableSessionManager
    private lateinit var variableSessionRepository: VariableSessionRepository
    private lateinit var variableSessionViewModel: VariableSessionViewModel
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var lastPackageName: String? = null
    private var lastStartTime: Long = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val context = this
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: "Nama Package gagal diambil"
            val currentTime = LocalTime.now()
            val currentTimeMillis = System.currentTimeMillis()

            Log.d("DEBUG_ACCESSIBILITY", "Event Type: ${event.eventType}, Package Name: $packageName")

            // Block on Schedules
            serviceScope.launch {
                if (blockScheduleManager.shouldBlockApp(packageName, currentTime)) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Aplikasi ini diblokir!", Toast.LENGTH_SHORT).show()
                    }
                    Log.d("BlockApp", "Menutup aplikasi: $packageName")
                    blockScheduleManager.blockApp()  // Menjalankan blok aplikasi
                }

                if (variableSessionManager.askLimit(packageName)) {
                    // Memulai FloatingDialogService alih-alih VariableSessionActivity
                    val intent = Intent(context, VariableSessionDialogActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Penting agar bisa berjalan di luar MainActivity
                    context.startActivity(intent)  // Memulai FloatingDialogService untuk menampilkan dialog mengambang
                }
            }


//            // Variable Session
//            if (packageName != lastPackageName) {
//                // Aplikasi berpindah, simpan durasi penggunaan aplikasi sebelumnya
//                lastPackageName?.let { previousPackage ->
//                    val durationMinutes = (currentTimeMillis - lastStartTime) / 60000
//                    serviceScope.launch {
//                        variableSessionManager.updateSessionTime(previousPackage, durationMinutes.toInt())
//                    }
//                }
//
//                // Perbarui package yang sedang digunakan
//                lastPackageName = packageName
//                lastStartTime = currentTimeMillis
//
//                // Cek apakah aplikasi yang baru dibuka masuk dalam daftar yang dibatasi
//                serviceScope.launch {
//                    val isLimited = variableSessionManager.isLimitedApp(packageName)
//                    if (isLimited) {
//                        variableSessionManager.reduceMinutesLeft(packageName)
//                    }
//                }
//            }
        }
    }

    override fun onInterrupt() {
        Log.d("BlockApp", "Service terputus!")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("ACCESSIBILITY_SERVICE", "Service connected")

        // Inisialisasi database dan dao
        val database = AppDatabase.getDatabase(this)
        val blockSchedulesDao = database.blockSchedulesDao()
        val variableSessionDao = database.variableSessionDao()

        // Inisialisasi manager
        blockScheduleManager = BlockScheduleManager(this, blockSchedulesDao)
        variableSessionManager = VariableSessionManager(this, variableSessionDao)
        variableSessionRepository = VariableSessionRepository(variableSessionDao)
        variableSessionViewModel = VariableSessionViewModel(variableSessionRepository)

        // Setup service info untuk accessibility service
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        serviceInfo = info

        // Cek apakah ini pertama kali setelah instalasi
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val isFirstRun = sharedPreferences.getBoolean("isFirstRun", true)

        if (isFirstRun) {
            Log.d("ACCESSIBILITY_SERVICE", "First time setup, running handler")

            Handler(Looper.getMainLooper()).postDelayed({
                Log.d("ACCESSIBILITY_SERVICE", "Restarting service for better event detection")
                disableSelf()  // Menonaktifkan layanan sementara
            }, 1000)

            sharedPreferences.edit().putBoolean("isFirstRun", false).apply()
        } else {
            Log.d("ACCESSIBILITY_SERVICE", "Service already initialized, skipping handler")
            sharedPreferences.edit().putBoolean("isFirstRun", true).apply()
        }
    }
}
