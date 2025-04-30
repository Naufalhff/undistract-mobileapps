package com.example.undistract.features.get_installed_apps.data

import android.content.Context
import android.content.Intent
import com.example.undistract.features.get_installed_apps.domain.AppInfo

class InstalledAppsRepository(private val context: Context) {

    fun getInstalledApps(): List<AppInfo> {
        val packageManager = context.packageManager

        // Intent untuk aplikasi yang dapat diluncurkan
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        // Mendapatkan aplikasi yang sesuai dengan intent
        val resolvedApps = packageManager.queryIntentActivities(intent, 0)

        return resolvedApps.map { resolveInfo ->
            val appInfo = resolveInfo.activityInfo.applicationInfo
            AppInfo(
                name = packageManager.getApplicationLabel(appInfo).toString(),
                packageName = appInfo.packageName,
                icon = appInfo.loadIcon(packageManager)
            )
        }.sortedBy { it.name }
    }
}
