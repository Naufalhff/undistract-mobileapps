package com.example.undistract.features.block_schedules.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_schedules_table")
data class BlockSchedulesEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appName: String,
    val packageName: String,
    val daysOfWeek: String,
    val isAllDay: Boolean,
    val startTime: String?,
    val endTime: String?,
    val isActive: Boolean
)
