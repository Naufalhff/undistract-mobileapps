package com.example.undistract.features.get_visited_urls.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "visited_urls",
    indices = [Index(value = ["url"], unique = true)]
)
class VisitedUrlsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "favicon")
    val favicon: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VisitedUrlsEntity) return false

        if (id != other.id) return false
        if (url != other.url) return false
        if (favicon != null) {
            if (other.favicon == null) return false
            if (!favicon.contentEquals(other.favicon)) return false
        } else if (other.favicon != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + url.hashCode()
        result = 31 * result + (favicon?.contentHashCode() ?: 0)
        return result
    }
}
