package com.example.undistract.features.block_permanent.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "block_permanent_table")
data class BlockPermanentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uuid: String = UUID.randomUUID().toString(),
    val appName: String,
    val packageName: String,
    val isActive: Boolean,
    val isParental: Boolean = false,
    val isSynced: Boolean = false
)