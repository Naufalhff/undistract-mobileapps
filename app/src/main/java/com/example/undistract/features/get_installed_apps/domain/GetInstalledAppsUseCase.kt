package com.example.undistract.features.get_installed_apps.domain

import android.content.Context
import com.example.undistract.features.get_installed_apps.data.InstalledAppsRepository

class GetInstalledAppsUseCase(private val repository: InstalledAppsRepository) {
    operator fun invoke(context: Context): List<AppInfo> {
        return repository.getInstalledApps()
    }
}