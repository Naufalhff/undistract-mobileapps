package com.example.undistract.features.block_schedules.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BlockSchedulesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockSchedules(data: BlockSchedulesEntity)

//    @Query("SELECT * FROM block_schedules_table")
//    suspend fun getAllBlockSchedules(): List<BlockSchedulesEntity>
//
    @Query("SELECT * FROM block_schedules_table WHERE packageName = :packageName")
    suspend fun getBlockSchedules(packageName: String): List<BlockSchedulesEntity>
//
//    @Query("DELETE FROM block_schedules_table WHERE id = :id")
//    suspend fun deleteBlockSchedulesById(id: Int)
//
//    @Query("UPDATE block_schedules_table SET isActive = :isActive WHERE id = :id")
//    suspend fun updateBlockSchedulesById(id: Int, isActive: Boolean)
}
