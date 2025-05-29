package com.example.undistract.features.select_apps.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SelectAppsRepository {

    private val selectedAppsMap = mutableMapOf<String, Boolean>()
    private val _selectedApps = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val selectedApps: StateFlow<Map<String, Boolean>> = _selectedApps.asStateFlow()

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
}