package com.example.undistract.features.parental_control.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pin_table")
data class PinEntity(
    @PrimaryKey val id: Int = 0,
    val pin: String
)
