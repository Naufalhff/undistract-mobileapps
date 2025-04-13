package com.example.undistract.features.setadaily_limit.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SetaDailyLimitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(setaDailyLimitEntity: SetaDailyLimitEntity): Long

    @Query("SELECT * FROM daily_limits_table")
    fun getAll(): Flow<List<SetaDailyLimitEntity>>

    @Query("SELECT * FROM daily_limits_table WHERE packageName = :packageName")
    suspend fun getByPackageName(packageName: String): SetaDailyLimitEntity?

    @Query("DELETE FROM daily_limits_table WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM daily_limits_table WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)

    @Query("UPDATE daily_limits_table SET isActive = :isActive WHERE id = :id")
    suspend fun updateActiveState(id: Int, isActive: Boolean)

    @Update
    suspend fun update(entity: SetaDailyLimitEntity)

    @Query("SELECT * FROM daily_limits_table")
    suspend fun getAllSync(): List<SetaDailyLimitEntity>

    @Query("SELECT * FROM daily_limits_table WHERE id = :id")
    suspend fun getById(id: Int): SetaDailyLimitEntity?
}