package com.example.undistract.features.select_apps.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SelectAppsRepository {
    // Daftar rute yang diperbolehkan menyimpan data selected apps
    private val allowedRoutes = setOf(
        "select_apps",
        "add_restriction",
        "block_permanent",
        "block_schedules",
        "variable_session"
    )

    private val selectedAppsMap = mutableMapOf<String, Boolean>()

    private val _selectedApps = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val selectedApps: StateFlow<Map<String, Boolean>> = _selectedApps.asStateFlow()

    private var currentRoute: String? = null

    // Mengatur rute saat ini dan memeriksa apakah termasuk rute yang diperbolehkan
    fun setCurrentRoute(route: String) {
        currentRoute = route

        if (!isAllowedRoute(route)) {
            clearAllSelectedApps()
        }
    }

    // Memeriksa apakah rute termasuk dalam daftar yang diperbolehkan
    fun isAllowedRoute(route: String): Boolean {
        return allowedRoutes.any { allowedRoute ->
            route.contains(allowedRoute, ignoreCase = true)
        }
    }

    // Menyimpan status aplikasi (selected/unselected)
    fun toggleAppSelection(packageName: String, isSelected: Boolean) {
        if (currentRoute != null && isAllowedRoute(currentRoute!!)) {
            selectedAppsMap[packageName] = isSelected
            _selectedApps.value = selectedAppsMap.toMap()
        }
    }

    // Mendapatkan status aplikasi
    fun isAppSelected(packageName: String): Boolean {
        return selectedAppsMap[packageName] ?: false
    }

    // Mendapatkan semua aplikasi yang dipilih
    fun getSelectedApps(): List<String> {
        return selectedAppsMap.filter { it.value }.keys.toList()
    }

    // Menghapus semua data aplikasi yang dipilih
    fun clearAllSelectedApps() {
        selectedAppsMap.clear()
        _selectedApps.value = emptyMap()
    }

    // Memeriksa apakah perlu membersihkan data saat rute berubah
    fun checkAndClearDataIfNeeded(newRoute: String?) {
        if (newRoute == null || !isAllowedRoute(newRoute)) {
            clearAllSelectedApps()
        }
    }
}