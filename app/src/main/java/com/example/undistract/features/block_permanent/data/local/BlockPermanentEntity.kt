package com.example.undistract.features.block_permanent.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_permanent_table")
data class BlockPermanentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val appName: String,
    val packageName: String,
    val isActive: Boolean
)