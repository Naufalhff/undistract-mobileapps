package com.example.undistract.features.get_visited_urls.data

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.example.undistract.features.get_visited_urls.data.local.VisitedUrlsDao
import com.example.undistract.features.get_visited_urls.data.local.VisitedUrlsEntity
import kotlinx.coroutines.flow.Flow

class VisitedUrlsRepository(private val dao: VisitedUrlsDao) {
    // 1. Fungsi Insert dengan parameter URL dan Favicon
    suspend fun insertUrl(url: String, favicon: ByteArray? = null) {
        Log.d("InsertDebug", "Insert called with URL: $url, favicon size: ${favicon?.size ?: 0}")

        val entity = VisitedUrlsEntity(
            url = url,
            favicon = favicon
        )

        dao.insert(entity)
    }


    // 2. Fungsi untuk mengambil semua URL sebagai Flow
    fun getAllUrls(): Flow<List<VisitedUrlsEntity>> {
        return dao.getAllUrls()
    }

    // 3. Fungsi untuk mengonversi favicon ke Drawable
    fun getFaviconDrawable(entity: VisitedUrlsEntity, context: Context): Drawable? {
        val faviconBytes = entity.favicon ?: return null
        val bitmap = BitmapFactory.decodeByteArray(faviconBytes, 0, faviconBytes.size)
        return BitmapDrawable(context.resources, bitmap)
    }
}