package com.example.undistract.features.select_apps.presentation

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.undistract.features.get_app_data.data.AppDataRepository
import com.example.undistract.features.get_app_data.domain.AppOrUrlItem
import com.example.undistract.features.select_apps.data.SelectAppsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SelectAppsViewModel(
    private val appDataRepository: AppDataRepository,
    private val selectAppsRepository: SelectAppsRepository
) : ViewModel() {

    private val _combinedItems = MutableStateFlow<List<AppOrUrlItem>>(emptyList())
    val combinedItems: StateFlow<List<AppOrUrlItem>> = _combinedItems.asStateFlow()

    // State map untuk UI
    val selectedApps = mutableStateMapOf<String, Boolean>()

    private val _selectedNotificationType = MutableStateFlow("Head Notification")
    val selectedNotificationType: StateFlow<String> = _selectedNotificationType.asStateFlow()

    init {
        loadCombinedItems()

        // Observer perubahan dari repository
        viewModelScope.launch {
            selectAppsRepository.selectedApps.collectLatest { selectedAppsMap ->
                selectedAppsMap.forEach { (identifier, isSelected) ->
                    selectedApps[identifier] = isSelected
                }
            }
        }
    }

    private fun loadCombinedItems() {
        viewModelScope.launch {
            // Dapatkan data aplikasi dan URL dari repository
            val items = appDataRepository.getCombinedList()

            // Gabungkan aplikasi dan URL
            val combined = items.sortedBy { it.name }

            _combinedItems.value = combined

            // Set status pilihan untuk setiap item
            combined.forEach { item ->
                selectedApps[item.identifier] = selectAppsRepository.isAppSelected(item.identifier)
            }
        }
    }

    // Update rute saat ini
    fun updateCurrentRoute(route: String) {
        selectAppsRepository.setCurrentRoute(route)
    }

    fun toggleAppSelection(identifier: String, isSelected: Boolean) {
        selectedApps[identifier] = isSelected
        selectAppsRepository.toggleAppSelection(identifier, isSelected)
    }

    fun getSelectedIdentifiers(): List<String> {
        return selectAppsRepository.getSelectedApps()
    }

    fun getSelectedItems(): List<AppOrUrlItem> {
        return combinedItems.value.filter { selectedApps[it.identifier] == true }
    }

    fun updateSelectedNotificationType(notificationType: String) {
        _selectedNotificationType.value = notificationType
    }
}
