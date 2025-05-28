package com.example.undistract.features.authentication_parental.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.undistract.features.block_schedules.data.local.BlockSchedulesEntity

@Dao
interface PinDao {

    @Query("SELECT pin FROM pin_table WHERE id = 0 LIMIT 1")
    suspend fun getPin(): String?

    @Query("SELECT email FROM pin_table WHERE id = 0 LIMIT 1")
    suspend fun getEmail(): String?

    @Query("UPDATE pin_table SET pin = :newPin WHERE id = 0")
    suspend fun updatePin(newPin: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePin(pin: PinEntity)

    @Query("DELETE FROM pin_table WHERE id = 0")
    suspend fun deletePin()

    // Query sync remote
    @Query("SELECT * FROM pin_table")
    fun getAll(): List<PinEntity>

    @Query("DELETE FROM pin_table")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<PinEntity>)

    @Query("UPDATE pin_table SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAllAsSynced()
}
