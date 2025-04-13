package com.example.undistract.features.usage_stats

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

class UsageStatsManager(private val context: Context) {
    private val TAG = "UsageStatsManager"
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    // Cache untuk menyimpan hasil perhitungan usage time
    private val usageTimeCache = mutableMapOf<String, Pair<Long, Long>>() // packageName -> (timestamp, usageTime)
    private val CACHE_VALIDITY_MS = 10000L // Cache valid selama 10 detik

    // Check if we have permission to access usage stats
    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    // Open settings to request usage stats permission
    fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    // Get today's usage time for a specific app in minutes
    suspend fun getAppUsageTimeToday(packageName: String): Long = withContext(Dispatchers.IO) {
        if (!hasUsageStatsPermission()) {
            Log.e(TAG, "No usage stats permission")
            return@withContext 0
        }

        // Check cache first
        val currentTime = System.currentTimeMillis()
        usageTimeCache[packageName]?.let { (timestamp, usageTime) ->
            if (currentTime - timestamp < CACHE_VALIDITY_MS) {
                Log.d(TAG, "Using cached usage time for $packageName: $usageTime minutes")
                return@withContext usageTime
            }
        }

        try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            val endTime = System.currentTimeMillis()

            val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
            var totalTimeInForeground = 0L
            var lastEventTime = 0L
            var isAppInForeground = false

            val usageEvent = UsageEvents.Event()
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(usageEvent)

                if (usageEvent.packageName == packageName) {
                    when (usageEvent.eventType) {
                        UsageEvents.Event.ACTIVITY_RESUMED -> {
                            isAppInForeground = true
                            lastEventTime = usageEvent.timeStamp
                        }
                        UsageEvents.Event.ACTIVITY_PAUSED -> {
                            if (isAppInForeground) {
                                totalTimeInForeground += usageEvent.timeStamp - lastEventTime
                                isAppInForeground = false
                            }
                        }
                    }
                }
            }

            // If app is still in foreground, add time until now
            if (isAppInForeground && lastEventTime > 0) {
                totalTimeInForeground += System.currentTimeMillis() - lastEventTime
            }

            // Convert milliseconds to minutes
            val usageTimeMinutes = TimeUnit.MILLISECONDS.toMinutes(totalTimeInForeground)

            // Cache the result
            usageTimeCache[packageName] = currentTime to usageTimeMinutes

            return@withContext usageTimeMinutes
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app usage time", e)
            return@withContext 0
        }
    }

    // Calculate progress (0.0 to 1.0) based on usage time and limit
    suspend fun calculateProgress(packageName: String, timeLimitMinutes: Int): Float {
        val usageTimeMinutes = getAppUsageTimeToday(packageName)
        return (usageTimeMinutes.toFloat() / timeLimitMinutes).coerceIn(0f, 1f)
    }

    // Format usage time as a string (e.g., "1h 30m / 2h 0m")
    suspend fun formatUsageTime(packageName: String, limitMinutes: Int): String {
        val usageMinutes = getAppUsageTimeToday(packageName).toInt()

        val usedHours = usageMinutes / 60
        val usedMinutes = usageMinutes % 60

        val limitHours = limitMinutes / 60
        val limitMinutesRemainder = limitMinutes % 60

        return "${usedHours}h ${usedMinutes}m / ${limitHours}h ${limitMinutesRemainder}m"
    }

    // Format usage time from minutes (for use in non-suspend contexts)
    fun formatTimeMinutes(usageMinutes: Int, limitMinutes: Int): String {
        val usedHours = usageMinutes / 60
        val usedMinutes = usageMinutes % 60

        val limitHours = limitMinutes / 60
        val limitMinutesRemainder = limitMinutes % 60

        return "${usedHours}h ${usedMinutes}m / ${limitHours}h ${limitMinutesRemainder}m"
    }

    // Clear cache
    fun clearCache() {
        usageTimeCache.clear()
    }

    // Reset notified apps at midnight
    fun resetNotifiedApps() {
        usageTimeCache.clear()
        Log.d(TAG, "Reset notified apps list")
    }

    // Check if usage limit has been reached
    suspend fun hasReachedLimit(packageName: String, limitMinutes: Int): Boolean {
        val usageTimeMinutes = getAppUsageTimeToday(packageName)
        return usageTimeMinutes >= limitMinutes
    }
}