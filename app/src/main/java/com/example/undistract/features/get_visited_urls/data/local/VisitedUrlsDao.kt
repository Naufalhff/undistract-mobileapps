package com.example.undistract.features.get_visited_urls.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitedUrlsDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: VisitedUrlsEntity)

    @Query("SELECT * FROM visited_urls")
    fun getAllUrls(): Flow<List<VisitedUrlsEntity>>
}