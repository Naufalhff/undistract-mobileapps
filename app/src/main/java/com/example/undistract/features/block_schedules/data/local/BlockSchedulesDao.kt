package com.example.undistract.features.block_schedules.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BlockSchedulesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockSchedules(data: BlockSchedulesEntity)

    @Query("SELECT * FROM block_schedules_table WHERE packageName = :packageName")
    suspend fun getBlockSchedules(packageName: String): List<BlockSchedulesEntity>
}