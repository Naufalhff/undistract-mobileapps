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

    @Query("SELECT * FROM block_permanent_table WHERE packageName = :packageName")
    suspend fun getBlockPermanent(packageName: String): List<BlockPermanentEntity>

    @Query("DELETE FROM block_permanent_table WHERE id = :id")
    suspend fun deleteBlockPermanent(id: Int)

    @Query("UPDATE block_permanent_table SET isActive = :isActive WHERE id = :id")
    suspend fun updateIsActive(id: Int, isActive: Boolean)

    @Query("SELECT * FROM block_permanent_table WHERE isActive = 1")
     fun getActiveBlockPermanent(): Flow<List<BlockPermanentEntity>>

}