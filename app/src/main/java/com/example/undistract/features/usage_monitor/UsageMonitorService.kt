package com.example.undistract.features.usage_monitor

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.undistract.MainActivity
import com.example.undistract.R
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepository
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepositoryImpl
import com.example.undistract.features.usage_stats.UsageStatsManager
import com.example.undistract.features.usage_limit.presentation.DailyLimitDialogActivity
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import java.util.jar.Manifest

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

    // Tambahkan channel ID khusus untuk pop-up notification
    private val POPUP_CHANNEL_ID = "popup_notification_channel"

    @SuppressLint("ForegroundServiceType")
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
                // Tambahkan reset notifiedApps saat service mulai
                notifiedApps.clear()
                Log.d(TAG, "Cleared notified apps list on service start")

                while (true) {
                    Log.d(TAG, "Running usage check cycle")
                    checkAppUsageLimits()
                    // Check every 15 seconds for more responsive notifications
                    delay(15000)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in monitoring loop", e)
                // Restart monitoring if it crashes
                delay(5000)
                startMonitoring()
            }
        }

        // Pastikan reset notified apps juga berjalan
        resetNotifiedAppsAtMidnight()
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

            limits.forEach { limit ->
                // Skip jika aplikasi yang sedang dibuka adalah Undistract
                if (limit.packageName == this@UsageMonitorService.packageName) {
                    Log.d(TAG, "Skipping Undistract app itself")
                    return@forEach
                }

                // Periksa apakah toggle untuk aplikasi ini aktif
                if (!limit.isActive) {
                    Log.d(TAG, "Skipping ${limit.appName} because toggle is off")
                    return@forEach
                }

                val usageTimeMinutes = usageStatsManager.getAppUsageTimeToday(limit.packageName)
                val progress = usageTimeMinutes.toFloat() / limit.timeLimitMinutes

                Log.d(TAG, "App ${limit.appName}: Used ${usageTimeMinutes}m of ${limit.timeLimitMinutes}m limit (${progress * 100}%)")

                // Check if usage has reached or exceeded the limit
                if (usageTimeMinutes >= limit.timeLimitMinutes && !notifiedApps.contains(limit.packageName)) {
                    // Verifikasi bahwa aplikasi yang sedang dibuka BUKAN Undistract sebelum menampilkan dialog
                    val currentForegroundApp = usageStatsManager.getCurrentForegroundApp()

                    if (currentForegroundApp != null) {
                        // Handle notification based on the notification type
                        when (limit.notificationType) {
                            "Head Notification" -> {
                                // Hanya tampilkan push notification untuk Head Notification
                                showHeadNotification(limit.appName, limit.packageName, usageTimeMinutes, limit.timeLimitMinutes)
                            }
                            "Pop Up Notification" -> {
                                // Pop Up Notification hanya muncul jika aplikasi aktif bukan Undistract
                                if (currentForegroundApp != this@UsageMonitorService.packageName) {
                                    showDailyLimitDialog(limit.appName, limit.packageName)
                                } else {
                                    Log.d(TAG, "Skipping pop-up for ${limit.appName} because Undistract is in foreground")
                                }
                            }
                            "Block Application" -> {
                                // Block Application hanya berlaku jika aplikasi aktif bukan Undistract
                                if (currentForegroundApp != this@UsageMonitorService.packageName) {
                                    blockApplication(limit.appName, limit.packageName)
                                } else {
                                    Log.d(TAG, "Skipping block for ${limit.appName} because Undistract is in foreground")
                                }
                            }
                        }
                        notifiedApps.add(limit.packageName)
                        Log.d(TAG, "Handled limit for ${limit.appName} with notification type: ${limit.notificationType}")
                    } else {
                        Log.d(TAG, "Skipping notification for ${limit.appName} because no foreground app detected")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking app usage limits", e)
        }
    }

    private fun showDailyLimitDialog(appName: String, packageName: String) {
        val intent = Intent(this, DailyLimitDialogActivity::class.java).apply {
            putExtra("APP_NAME", appName)
            putExtra("PACKAGE_NAME", packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(intent)
    }

    private fun showHeadNotification(appName: String, packageName: String, usageMinutes: Long, limitMinutes: Int) {
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

        Log.d(TAG, "Showed head notification for $appName")
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

    private fun blockApplication(appName: String, packageName: String) {
        // Kirim broadcast untuk memblokir aplikasi
        val intent = Intent("com.example.undistract.BLOCK_APP")
        intent.putExtra("packageName", packageName)
        intent.putExtra("appName", appName)
        sendBroadcast(intent)

        // Tampilkan notifikasi bahwa aplikasi diblokir
        showBlockedNotification(appName, packageName)

        // Tutup aplikasi yang sedang berjalan
        closeApplication(packageName)
    }

    private fun closeApplication(packageName: String) {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun showBlockedNotification(appName: String, packageName: String) {
        val notificationId = nextNotificationId++

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle("App Blocked")
            .setContentText("$appName has been blocked as you've exceeded your daily limit.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)

        Log.d(TAG, "Showed blocked notification for $appName")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Channel untuk notifikasi reguler
            val name = "Usage Limit Notifications"
            val descriptionText = "Notifications for app usage limits"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC // Tampilkan di lock screen
            }

            // Channel khusus untuk notifikasi pop-up dengan prioritas tertinggi
            val popupChannelId = "popup_notification_channel"
            val popupChannelName = "Pop-up Alerts"
            val popupChannel = NotificationChannel(popupChannelId, popupChannelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Full screen alerts for usage limits"
                enableVibration(true)
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                // Khusus untuk pop-up notification
                setBypassDnd(true) // Bypass Do Not Disturb
                setShowBadge(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            notificationManager.createNotificationChannel(popupChannel)

            Log.d(TAG, "Created notification channels")
        }
    }

    @SuppressLint("ForegroundServiceType")
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