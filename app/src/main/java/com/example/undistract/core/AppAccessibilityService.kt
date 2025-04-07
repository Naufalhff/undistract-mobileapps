package com.example.undistract.core

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.block_permanent.data.BlockPermanentRepository
import com.example.undistract.features.block_permanent.data.local.BlockPermanentEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AppAccessibilityService : AccessibilityService() {

    private lateinit var repository: BlockPermanentRepository
    private lateinit var blockedApps: List<BlockPermanentEntity>

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("AccessibilityService", "Service connected")

        val database = AppDatabase.getDatabase(applicationContext)
        repository = BlockPermanentRepository(database.blockPermanentDao())

        loadBlockedApps()
    }

    private fun loadBlockedApps() {
        coroutineScope.launch {
            blockedApps = repository.getActiveBlockPermanent()
            Log.d("AccessibilityService", "Blocked apps loaded: ${blockedApps.map { it.packageName }}")
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString()
        Log.d("AccessibilityService", "AccessibilityEvent received for package: $packageName")

        if (packageName != null) {
            Log.d("AccessibilityService", "Checking if $packageName is blocked...")

            if (::blockedApps.isInitialized) {
                if (isAppBlocked(packageName)) {
                    val appName = getAppName(packageName)
                    Log.d("AccessibilityService", "App is blocked: $packageName ($appName)")

                    Toast.makeText(
                        this,
                        "Aplikasi $appName telah diblokir!",
                        Toast.LENGTH_SHORT
                    ).show()

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


    private fun isAppBlocked(packageName: String): Boolean {
        return blockedApps.any { it.packageName == packageName && it.isActive }
    }

    private fun getAppName(packageName: String): String {
        return blockedApps.find { it.packageName == packageName }?.appName ?: packageName
    }

    override fun onInterrupt() {
        // Handle jika service terganggu
    }
}