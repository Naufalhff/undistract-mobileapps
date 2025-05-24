package com.example.undistract.features.authentication_parental.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

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
}
