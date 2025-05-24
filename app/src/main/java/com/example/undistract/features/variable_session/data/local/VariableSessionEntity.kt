package com.example.undistract.features.variable_session.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "variable_session_table",
    primaryKeys = ["appName", "packageName"]
)
data class VariableSessionEntity(
    val uuid: String = UUID.randomUUID().toString(),
    val appName: String,
    val packageName: String,
    val secondsLeft: Int,
    val coolDownDuration: Long?,
    val coolDownEndTime: Long?,
    val isOnCoolDown: Boolean,
    val isActive: Boolean,
    val isParental: Boolean,
    val isSynced: Boolean = false
)
