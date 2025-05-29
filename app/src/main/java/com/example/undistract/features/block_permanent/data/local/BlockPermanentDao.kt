package com.example.undistract.features.block_permanent.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockPermanentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlockPermanent(data: BlockPermanentEntity)

    @Query("DELETE FROM block_permanent_table WHERE id = :id")
    suspend fun deleteBlockPermanent(id: Int)

    @Query("UPDATE block_permanent_table SET isActive = :isActive WHERE id = :id")
    suspend fun updateIsActive(id: Int, isActive: Boolean)

    @Query("SELECT * FROM block_permanent_table WHERE isActive = 1")
     fun getActiveBlockPermanent(): Flow<List<BlockPermanentEntity>>

    @Query("SELECT * FROM block_permanent_table")
    fun getAllBlockPermanent(): Flow<List<BlockPermanentEntity>>

    @Query("SELECT * FROM block_permanent_table WHERE isParental = :isParental")
    fun getBlockPermanentByParentalFlag(isParental: Boolean): Flow<List<BlockPermanentEntity>>

    // Query sync remote
    @Query("SELECT * FROM block_permanent_table")
    fun getAll(): List<BlockPermanentEntity>

    @Query("DELETE FROM block_permanent_table WHERE isSynced = 1")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<BlockPermanentEntity>)

    @Query("UPDATE block_permanent_table SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAllAsSynced()
}