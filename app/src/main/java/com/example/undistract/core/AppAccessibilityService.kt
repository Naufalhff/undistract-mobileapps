package com.example.undistract.core

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.block_permanent.data.BlockPermanentRepository
import com.example.undistract.features.block_permanent.data.local.BlockPermanentEntity
import com.example.undistract.features.block_schedules.domain.BlockScheduleManager
import com.example.undistract.features.variable_session.data.VariableSessionRepository
import com.example.undistract.features.variable_session.domain.VariableSessionManager
import com.example.undistract.features.variable_session.presentation.VariableSessionDialogActivity
import com.example.undistract.features.variable_session.presentation.VariableSessionViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalTime

class AppAccessibilityService : AccessibilityService() {

    private lateinit var blockScheduleManager: BlockScheduleManager
    private lateinit var variableSessionManager: VariableSessionManager
    private lateinit var variableSessionRepository: VariableSessionRepository
    private lateinit var variableSessionViewModel: VariableSessionViewModel
    private lateinit var blockPermanentRepository: BlockPermanentRepository
    private lateinit var blockedApps: List<BlockPermanentEntity>
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())
    private var lastPackageName: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val context = this

        // Ignore ketika user mengetik
        val keyboardPackages = listOf(
            "com.google.android.inputmethod.latin",
            "com.microsoft.swiftkey",
            "com.samsung.android.honeyboard"
        )

        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: "Nama Package gagal diambil"
            val currentTime = LocalTime.now()

            Log.d("DEBUG_ACCESSIBILITY", "Event Type: ${event.eventType}, Package Name: $packageName")

            serviceScope.launch {

                // BLOCK ON SCHEDULES
                if (blockScheduleManager.shouldBlockApp(packageName, currentTime)) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "This app is blocked!", Toast.LENGTH_SHORT).show()
                    }
                    Log.d("BlockApp", "Menutup aplikasi: $packageName")
                    blockScheduleManager.blockApp()
                    return@launch
                }

                // VARIABLE SESSION LIMIT
                if (!variableSessionManager.canStartNewSession(packageName)) {
                    withContext(Dispatchers.Main) {
                        variableSessionManager.showToast("This app is still on cool down period!")
                    }
                    variableSessionManager.blockApp()
                    return@launch
                }

                if (variableSessionManager.askLimit(packageName)) {
                    withContext(Dispatchers.Main) {
                        val intent = Intent(context, VariableSessionDialogActivity::class.java).apply {
                            putExtra("PACKAGE_NAME", packageName)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }
                }

                if (variableSessionManager.isLimitedApp(packageName)) {
                    if (lastPackageName != packageName && !keyboardPackages.contains(packageName)) {
                        lastPackageName?.let { previousPackage ->
                            variableSessionManager.stopTimer(previousPackage, variableSessionViewModel)
                        }

                        variableSessionManager.startTimer(packageName, variableSessionViewModel)
                        lastPackageName = packageName
                    }
                } else {
                    if (lastPackageName != null && !keyboardPackages.contains(packageName)) {
                        lastPackageName?.let { previousPackage ->
                            variableSessionManager.stopTimer(previousPackage, variableSessionViewModel)
                        }
                        lastPackageName = null
                    }
                }

                // BLOCK PERMANENT
                Log.d("AccessibilityService", "Checking if $packageName is blocked...")

                if (::blockedApps.isInitialized) {
                    if (isAppBlocked(packageName)) {
                        val appName = getAppName(packageName)
                        Log.d("AccessibilityService", "App is blocked: $packageName ($appName)")
                        withContext(Dispatchers.Main){
                            Toast.makeText(context, "App $appName is blocked!", Toast.LENGTH_SHORT).show()
                        }
                        val intent = Intent(Intent.ACTION_MAIN)
                        intent.addCategory(Intent.CATEGORY_HOME)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    } else {
                        Log.d("AccessibilityService", "App is not blocked: $packageName")
                    }
                } else {
                    Log.d("AccessibilityService", "Blocked apps not initialized yet.")
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d("BlockApp", "Service terputus!")
    }
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("ACCESSIBILITY_SERVICE", "Service connected")

        // Inisialisasi database dan dao
        val database = AppDatabase.getDatabase(this)
        val blockSchedulesDao = database.blockSchedulesDao()
        val variableSessionDao = database.variableSessionDao()
        blockPermanentRepository = BlockPermanentRepository(database.blockPermanentDao())

        // Inisialisasi manager
        blockScheduleManager = BlockScheduleManager(this, blockSchedulesDao)
        variableSessionManager = VariableSessionManager(this, variableSessionDao)
        variableSessionRepository = VariableSessionRepository(variableSessionDao)
        variableSessionViewModel = VariableSessionViewModel(variableSessionRepository)
        loadBlockedApps()

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

    private fun loadBlockedApps() {
        coroutineScope.launch {
            blockedApps = blockPermanentRepository.getActiveBlockPermanent()
            Log.d("AccessibilityService", "Blocked apps loaded: ${blockedApps.map { it.packageName }}")
        }
    }
    private fun isAppBlocked(packageName: String): Boolean {
        return blockedApps.any { it.packageName == packageName && it.isActive }
    }

    private fun getAppName(packageName: String): String {
        return blockedApps.find { it.packageName == packageName }?.appName ?: packageName
    }
}
