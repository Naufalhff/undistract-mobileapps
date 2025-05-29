package com.example.undistract.features.add_limit.data.local

data class BlockScheduleData(
    val daysOfWeek: String,
    val isAllDay: Boolean,
    val startTime: String?,
    val endTime: String?,
    val isActive: Boolean,
    val isParental: Boolean
)