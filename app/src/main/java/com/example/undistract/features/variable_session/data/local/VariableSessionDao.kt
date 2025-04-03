package com.example.undistract.features.variable_session.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface VariableSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVariableSession(data: VariableSessionEntity)

    @Query("SELECT * FROM variable_session_table WHERE packageName = :packageName")
    suspend fun getVariableSession(packageName: String): List<VariableSessionEntity>

    @Query("DELETE FROM variable_session_table WHERE packageName = :packageName")
    suspend fun deleteVariableSession(packageName: String)

    @Query("UPDATE variable_session_table SET secondsLeft = :secondsLeft WHERE packageName = :packageName")
    suspend fun updateSecondsLeft(packageName: String, secondsLeft: Int)

    @Query("UPDATE variable_session_table SET secondsLeft = secondsLeft - :secondsLeft WHERE packageName = :packageName")
    suspend fun subtractSecondsLeft(packageName: String, secondsLeft: Int)

    @Query("UPDATE variable_session_table SET isActive = :isActive WHERE packageName = :packageName")
    suspend fun updateIsActive(packageName: String, isActive: Boolean)
}
