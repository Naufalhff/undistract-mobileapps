package com.example.undistract.features.add_limit.data.local

data class VariableSessionData (
    val secondsLeft: Int,
    val coolDownDuration: Long?,
    val coolDownEndTime: Long?,
    val isOnCooldown: Boolean,
    val isActive: Boolean,
    val isParental: Boolean
)