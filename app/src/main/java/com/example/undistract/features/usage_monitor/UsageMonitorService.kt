package com.example.undistract.features.usage_monitor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.undistract.MainActivity
import com.example.undistract.R
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepository
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepositoryImpl
import com.example.undistract.features.usage_stats.UsageStatsManager
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class UsageMonitorService : Service() {
    private val TAG = "UsageMonitorService"
    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var usageStatsManager: UsageStatsManager
    private lateinit var repository: SetaDailyLimitRepository
    
    // Notification IDs
    private val NOTIFICATION_CHANNEL_ID = "usage_limit_channel"
    private val FOREGROUND_SERVICE_ID = 1001
    private var nextNotificationId = 2000
    
    // Track which apps have already shown notifications today
    private val notifiedApps = mutableSetOf<String>()
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        
        usageStatsManager = UsageStatsManager(this)
        repository = SetaDailyLimitRepositoryImpl(
            AppDatabase.getDatabase(this).setaDailyLimitDao()
        )
        
        createNotificationChannel()
        startForeground()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        
        // Start monitoring app usage
        startMonitoring()
        
        return START_STICKY
    }
    
    private fun startMonitoring() {
        serviceScope.launch {
            try {
                while (true) {
                    checkAppUsageLimits()
                    // Check every 30 seconds for more responsive notifications
                    delay(30000)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in monitoring loop", e)
            }
        }
    }
    
    private suspend fun checkAppUsageLimits() {
        try {
            if (!usageStatsManager.hasUsageStatsPermission()) {
                Log.e(TAG, "No usage stats permission")
                return
            }
            
            // Get all active daily limits
            val limits = withContext(Dispatchers.IO) {
                repository.getAllSync()
            }
            
            limits.filter { it.isActive }.forEach { limit ->
                val usageTimeMinutes = usageStatsManager.getAppUsageTimeToday(limit.packageName)
                val progress = usageTimeMinutes.toFloat() / limit.timeLimitMinutes
                
                Log.d(TAG, "App ${limit.appName}: Used ${usageTimeMinutes}m of ${limit.timeLimitMinutes}m limit (${progress * 100}%)")
                
                // Check if usage has reached or exceeded the limit
                if (usageTimeMinutes >= limit.timeLimitMinutes && !notifiedApps.contains(limit.packageName)) {
                    // Show notification
                    showLimitReachedNotification(limit.appName, limit.packageName, usageTimeMinutes, limit.timeLimitMinutes)
                    notifiedApps.add(limit.packageName)
                    Log.d(TAG, "Added ${limit.packageName} to notified apps")
                }
                
                // Also notify at 90% of the limit as a warning
                val warningKey = "${limit.packageName}_warning"
                if (progress >= 0.9f && progress < 1.0f && !notifiedApps.contains(warningKey)) {
                    showLimitWarningNotification(limit.appName, limit.packageName, usageTimeMinutes, limit.timeLimitMinutes)
                    notifiedApps.add(warningKey)
                    Log.d(TAG, "Added $warningKey to notified apps")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking app usage limits", e)
        }
    }
    
    private fun showLimitReachedNotification(appName: String, packageName: String, usageMinutes: Long, limitMinutes: Int) {
        val notificationId = nextNotificationId++
        
        val usedHours = usageMinutes / 60
        val usedMinutesRemainder = usageMinutes % 60
        val usageText = if (usedHours > 0) "${usedHours}h ${usedMinutesRemainder}m" else "${usedMinutesRemainder}m"
        
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("Time Limit Reached")
            .setContentText("You've used $appName for $usageText, which exceeds your daily limit.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
        
        Log.d(TAG, "Showed limit reached notification for $appName")
    }
    
    private fun showLimitWarningNotification(appName: String, packageName: String, usageMinutes: Long, limitMinutes: Int) {
        val notificationId = nextNotificationId++
        
        val usedHours = usageMinutes / 60
        val usedMinutesRemainder = usageMinutes % 60
        val usageText = if (usedHours > 0) "${usedHours}h ${usedMinutesRemainder}m" else "${usedMinutesRemainder}m"
        
        val remainingMinutes = limitMinutes - usageMinutes
        val remainingText = if (remainingMinutes > 60) {
            val hours = remainingMinutes / 60
            val mins = remainingMinutes % 60
            "${hours}h ${mins}m"
        } else {
            "${remainingMinutes}m"
        }
        
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("Almost at Time Limit")
            .setContentText("You've used $appName for $usageText. Only $remainingText left for today.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
        
        Log.d(TAG, "Showed limit warning notification for $appName")
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Usage Limit Notifications"
            val descriptionText = "Notifications for app usage limits"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            
            Log.d(TAG, "Created notification channel")
        }
    }
    
    private fun startForeground() {
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("Undistract")
            .setContentText("Monitoring app usage")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        
        startForeground(FOREGROUND_SERVICE_ID, notification)
        Log.d(TAG, "Started foreground service")
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "Service destroyed")
    }
    
    // Reset notified apps at midnight
    private fun resetNotifiedAppsAtMidnight() {
        serviceScope.launch {
            try {
                while (true) {
                    val calendar = java.util.Calendar.getInstance()
                    val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
                    val currentMinute = calendar.get(java.util.Calendar.MINUTE)
                    
                    // Reset at midnight (00:00)
                    if (currentHour == 0 && currentMinute == 0) {
                        notifiedApps.clear()
                        Log.d(TAG, "Reset notified apps at midnight")
                    }
                    
                    // Check every minute
                    delay(60000)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in midnight reset loop", e)
            }
        }
    }
    
    companion object {
        fun startService(context: Context) {
            val intent = Intent(context, UsageMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            Log.d("UsageMonitorService", "Service start requested")
        }
        
        fun stopService(context: Context) {
            val intent = Intent(context, UsageMonitorService::class.java)
            context.stopService(intent)
            Log.d("UsageMonitorService", "Service stop requested")
        }
    }
} 