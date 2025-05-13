package com.example.undistract.features.my_usage.presentation

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.undistract.features.usage_stats.UsageStatsManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

data class AppUsageInfo(
    val appName: String,
    val packageName: String,
    val appIcon: Drawable,
    val usageTimeInMillis: Long,
    val usagePercentage: Float
)

data class HourlyUsageData(
    val hour: Int,
    val usageTimeInMillis: Long
)

class MyUsageViewModel(private val context: Context) : ViewModel() {
    private val usageStatsManager = UsageStatsManager(context)
    private val packageManager = context.packageManager

    private val _appUsageStats = MutableStateFlow<List<AppUsageInfo>>(emptyList())
    val appUsageStats: StateFlow<List<AppUsageInfo>> = _appUsageStats

    private val _hourlyUsageData = MutableStateFlow<List<HourlyUsageData>>(emptyList())
    val hourlyUsageData: StateFlow<List<HourlyUsageData>> = _hourlyUsageData

    private val _totalUsage = MutableStateFlow(0L)
    val totalUsage: StateFlow<Long> = _totalUsage

    init {
        refreshUsageStats()
    }

    fun refreshUsageStats() {
        viewModelScope.launch {
            fetchTodayUsageStats()
            fetchHourlyUsageData()
        }
    }

    private suspend fun fetchTodayUsageStats() {
        withContext(Dispatchers.IO) {
            // Check for permission
            if (!usageStatsManager.hasUsageStatsPermission()) {
                usageStatsManager.requestUsageStatsPermission()
                return@withContext
            }

            // Get usage stats from the UsageStatsManager
            val appStats = usageStatsManager.getDetailedAppUsageToday()

            var totalUsageTime = 0L
            val appUsageList = mutableListOf<AppUsageInfo>()

            // Process each app's usage statistics
            appStats.forEach { (packageName, usageTime) ->
                try {
                    if (usageTime > 0) {
                        totalUsageTime += usageTime

                        val appInfo = packageManager.getApplicationInfo(packageName, 0)
                        val appName = packageManager.getApplicationLabel(appInfo).toString()
                        val appIcon = packageManager.getApplicationIcon(appInfo)

                        appUsageList.add(
                            AppUsageInfo(
                                appName = appName,
                                packageName = packageName,
                                appIcon = appIcon,
                                usageTimeInMillis = usageTime,
                                usagePercentage = 0f // Will calculate after sorting
                            )
                        )
                    }
                } catch (e: PackageManager.NameNotFoundException) {
                    // Skip apps that cannot be found
                }
            }

            // Sort by usage time (descending) and calculate percentage
            val sortedList = appUsageList.sortedByDescending { it.usageTimeInMillis }
                .map { app ->
                    val percentage = if (totalUsageTime > 0) {
                        app.usageTimeInMillis.toFloat() / totalUsageTime
                    } else {
                        0f
                    }
                    app.copy(usagePercentage = percentage)
                }

            _appUsageStats.value = sortedList
            _totalUsage.value = totalUsageTime
        }
    }

    private suspend fun fetchHourlyUsageData() {
        withContext(Dispatchers.IO) {
            if (!usageStatsManager.hasUsageStatsPermission()) {
                return@withContext
            }

            val hourlyData = usageStatsManager.getHourlyUsageToday()

            // Convert to list of HourlyUsageData objects
            val hourlyUsageList = mutableListOf<HourlyUsageData>()

            // Ensure we have data for all hours (0-23)
            for (hour in 0..23) {
                val usageTime = hourlyData[hour] ?: 0L
                hourlyUsageList.add(HourlyUsageData(hour, usageTime))
            }

            _hourlyUsageData.value = hourlyUsageList
        }
    }
}

class MyUsageViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyUsageViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyUsageViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}