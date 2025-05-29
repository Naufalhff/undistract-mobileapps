package com.example.undistract.features.select_apps.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SelectAppsRepository {

    private val selectedAppsMap = mutableMapOf<String, Boolean>()
    private val _selectedApps = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val selectedApps: StateFlow<Map<String, Boolean>> = _selectedApps.asStateFlow()
    private val _isSelectAll = MutableStateFlow(false)
    val isSelectAll: StateFlow<Boolean> = _isSelectAll

    // Menyimpan status aplikasi (selected/unselected)
    fun toggleAppSelection(packageName: String, isSelected: Boolean) {
        selectedAppsMap[packageName] = isSelected
        _selectedApps.value = selectedAppsMap.toMap()
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

    fun selectAllApps(apps: List<String>) {
        apps.forEach { packageName ->
            selectedAppsMap[packageName] = true
        }
        _selectedApps.value = selectedAppsMap.toMap()
    }

    fun unselectAllApps(apps: List<String>) {
        apps.forEach { packageName ->
            selectedAppsMap[packageName] = false
        }
        _selectedApps.value = selectedAppsMap.toMap()
    }
}