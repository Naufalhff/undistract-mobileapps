package com.example.undistract.features.add_limit.data.local

import androidx.compose.runtime.MutableState

data class ScheduleData(
    val daysOfWeek: String,
    val isAllDay: Boolean,
    val startTime: String?,
    val endTime: String?,
    val isActive: Boolean,
    val isParental: Boolean
)