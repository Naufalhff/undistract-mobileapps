package com.example.undistract.features.variable_session.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "variable_session_table",
    primaryKeys = ["appName", "packageName"]
)
data class VariableSessionEntity(
    val appName: String,
    val packageName: String,
    val secondsLeft: Int,
    val isActive: Boolean
)

