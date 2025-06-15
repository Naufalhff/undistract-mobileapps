package com.example.undistract.features.get_app_data.data

import android.annotation.SuppressLint
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
    private val allowedSystemApps = setOf(
        // Browser & Edukasi
        "com.android.chrome",
        "com.google.android.youtube",

        // Navigasi
        "com.google.android.apps.maps",

        // Komunikasi Profesional
        "com.google.android.gm",

        // Manajemen File & Dokumen
        "com.google.android.documentsui",
        "com.google.android.apps.docs",
        "com.adobe.reader",

        // Tools Produktif
        "com.google.android.calendar",
        "com.google.android.keep",

        // Sistem
        "com.android.settings",
        "com.android.vending",

        // (opsional) komunikasi dasar:
        "com.google.android.dialer",
        "com.android.messaging"
    )


    // 1. Ambil semua aplikasi yang bisa diluncurkan
    private fun getLaunchableApps(): List<AppOrUrlItem.AppItem> {
        val apps = mutableSetOf<AppOrUrlItem.AppItem>()

        // Approach 1: Menggunakan QUERY_ALL_PACKAGES permission
        apps.addAll(getLaunchableAppsWithQueryAll())

        // Approach 2: Menggunakan package queries untuk app yang tidak terdeteksi
        apps.addAll(getLaunchableAppsWithPackageQueries())

        return apps.distinctBy { it.identifier }.sortedBy { it.name }
    }

    // Approach 1: Menggunakan QUERY_ALL_PACKAGES
    @SuppressLint("QueryPermissionsNeeded")
    private fun getLaunchableAppsWithQueryAll(): List<AppOrUrlItem.AppItem> {
        val packageManager = context.packageManager
        val ownPackageName = context.packageName

        return try {
            // Menggunakan GET_ACTIVITIES flag untuk mendapatkan lebih banyak app
            val installedApps = packageManager.getInstalledApplications(
                PackageManager.GET_META_DATA or PackageManager.MATCH_UNINSTALLED_PACKAGES
            )

            installedApps
                .filter { appInfo ->
                    val packageName = appInfo.packageName

                    // Filter: bukan app sendiri, enabled, dan ada launch intent
                    packageName != ownPackageName &&
                            appInfo.enabled &&
                            hasLaunchIntent(packageName) &&
                            !isSystemApp(appInfo)
                }
                .map { appInfo ->
                    AppOrUrlItem.AppItem(
                        name = packageManager.getApplicationLabel(appInfo).toString(),
                        identifier = appInfo.packageName,
                        icon = appInfo.loadIcon(packageManager)
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Approach 2: Menggunakan package queries
    private fun getLaunchableAppsWithPackageQueries(): List<AppOrUrlItem.AppItem> {
        val packageManager = context.packageManager
        val ownPackageName = context.packageName

        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        return try {
            val resolvedApps = packageManager.queryIntentActivities(
                mainIntent,
                PackageManager.MATCH_ALL or PackageManager.MATCH_DISABLED_COMPONENTS
            )

            resolvedApps
                .filter { resolveInfo ->
                    val appInfo = resolveInfo.activityInfo.applicationInfo
                    val packageName = appInfo.packageName

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
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Helper function untuk cek apakah app punya launch intent
    private fun hasLaunchIntent(packageName: String): Boolean {
        val packageManager = context.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        return launchIntent != null
    }

    private fun isSystemApp(appInfo: ApplicationInfo): Boolean {
        // Allow if in whitelist
        if (appInfo.packageName in allowedSystemApps) return false

        // Otherwise check standard system flags
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