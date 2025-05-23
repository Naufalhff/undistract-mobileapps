package com.example.undistract.features.setadaily_limit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "daily_limits_table")
data class SetaDailyLimitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uuid: String = UUID.randomUUID().toString(),
    val appName: String,
    val packageName: String,
    val icon: String,
    val timeLimitMinutes: Int,
    val isActive: Boolean = true,
    val isParental: Boolean = false,
    val notificationType: String = "Head Notification", // Default is Head Notification
)