package com.example.undistract.features.setadaily_limit.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_limits_table")
data class SetaDailyLimitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appName: String,
    val packageName: String,
    val icon: String,
    val timeLimitMinutes: Int,
    val isActive: Boolean = true
)