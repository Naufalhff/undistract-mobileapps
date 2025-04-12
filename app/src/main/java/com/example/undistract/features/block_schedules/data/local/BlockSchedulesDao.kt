package com.example.undistract.features.block_schedules.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockSchedulesDao {
    @Query("SELECT * FROM block_schedules_table")
    fun getAllBlockSchedules(): List<BlockSchedulesEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockSchedules(data: BlockSchedulesEntity)

    @Query("SELECT * FROM block_schedules_table WHERE packageName = :packageName")
    suspend fun getBlockSchedules(packageName: String): List<BlockSchedulesEntity>
}