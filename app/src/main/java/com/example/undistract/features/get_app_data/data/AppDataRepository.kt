package com.example.undistract.features.get_app_data.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import androidx.core.content.ContextCompat
import com.example.undistract.R
import com.example.undistract.features.get_app_data.domain.AppOrUrlItem
import com.example.undistract.features.get_visited_urls.data.VisitedUrlsRepository
import kotlinx.coroutines.flow.first
import android.content.pm.PackageManager

class AppDataRepository(
    private val context: Context,
    private val visitedUrlsRepository: VisitedUrlsRepository
) {
    // 1. Ambil semua aplikasi yang bisa diluncurkan
    private fun getLaunchableApps(): List<AppOrUrlItem.AppItem> {
        val packageManager = context.packageManager
        val ownPackageName = context.packageName

        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolvedApps = packageManager.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)

        return resolvedApps
            .filter { resolveInfo ->
                val appInfo = resolveInfo.activityInfo.applicationInfo
                val packageName = appInfo.packageName

                // Saring: bukan aplikasi sendiri, enabled, bukan sistem launcher
                packageName != ownPackageName &&
                        appInfo.enabled &&
                        !isSystemApp(appInfo)
            }
            .map { resolveInfo ->
                val appInfo = resolveInfo.activityInfo.applicationInfo
                AppOrUrlItem.AppItem(
                    name = packageManager.getApplicationLabel(appInfo).toString(),
                    identifier = appInfo.packageName,
                    icon = appInfo.loadIcon(packageManager)
                )
            }
    }

    private fun isSystemApp(appInfo: ApplicationInfo): Boolean {
        return (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0
    }

    // 2. Ambil semua URL dari database sebagai AppOrUrlItem.UrlItem
    private suspend fun getVisitedUrlsAsItems(): List<AppOrUrlItem.UrlItem> {
        val urls = visitedUrlsRepository.getAllUrls().first()
        return urls.map { entity ->
            AppOrUrlItem.UrlItem(
                name = entity.url,
                identifier = entity.url,
                icon = visitedUrlsRepository.getFaviconDrawable(entity, context)
                    ?: ContextCompat.getDrawable(context, R.drawable.construction_icon)
            )
        }
    }

    // 3. Gabungkan dan urutkan
    suspend fun getCombinedList(): List<AppOrUrlItem> {
        val apps = getLaunchableApps()
        val urls = getVisitedUrlsAsItems()
        return (apps + urls).sorted()
    }
}