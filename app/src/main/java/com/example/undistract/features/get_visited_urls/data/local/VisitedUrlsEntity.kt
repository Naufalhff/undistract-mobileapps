package com.example.undistract.features.get_visited_urls.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "visited_urls",
    indices = [Index(value = ["url"], unique = true)]
)
data class VisitedUrlsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "favicon")
    val favicon: ByteArray? = null,
)