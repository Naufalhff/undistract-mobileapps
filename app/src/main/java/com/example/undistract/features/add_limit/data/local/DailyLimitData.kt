package com.example.undistract.features.add_limit.data.local

import com.example.undistract.R
import com.example.undistract.features.get_app_data.domain.AppOrUrlItem
import com.example.undistract.features.setadaily_limit.data.local.SetaDailyLimitEntity

// DailyLimitData.kt
data class DailyLimitData(
    val selectedHours: Int,
    val selectedMinutes: Int,
    val selectedApps: List<AppOrUrlItem>,
    val isParental: Boolean,
    val notificationType: String
) {
    val timeLimitMinutes: Int
        get() = (selectedHours * 60) + selectedMinutes

    fun toEntities(): List<SetaDailyLimitEntity> {
        return selectedApps.map { app ->
            val iconString = try {
                app.icon.toString()
            } catch (e: Exception) {
                R.drawable.app_logo.toString()
            }

            SetaDailyLimitEntity(
                appName = app.name,
                packageName = app.identifier,
                icon = iconString,
                timeLimitMinutes = timeLimitMinutes,
                isParental = isParental,
                notificationType = notificationType
            )
        }
    }

    fun isValid(): Boolean {
        return selectedApps.isNotEmpty() && (selectedHours > 0 || selectedMinutes > 0)
    }
}