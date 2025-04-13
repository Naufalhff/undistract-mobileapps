package com.example.undistract.features.get_installed_apps.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.undistract.features.get_installed_apps.domain.AppInfo

class InstalledAppsRepository(private val context: Context) {

    fun getInstalledApps(): List<AppInfo> {
        val packageManager = context.packageManager

        // List aplikasi sistem yang tetap akan ditampilkan
        val allowedSystemApps = listOf(
            "com.google.android.youtube",
            "com.android.chrome",
            // Tambah aplikasi lain (Tambahkan juga dalam query di Android Manifest)
        )

        return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { app ->
                (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || app.packageName in allowedSystemApps
            }
            .map { app ->
                AppInfo(
                    name = packageManager.getApplicationLabel(app).toString(),
                    packageName = app.packageName,
                    icon = app.loadIcon(packageManager)
                )
            }
            .sortedBy { it.name }
    }
}