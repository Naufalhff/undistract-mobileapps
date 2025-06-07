package com.example.undistract.features.select_apps.presentation

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.undistract.features.get_app_data.data.AppDataRepository
import com.example.undistract.features.get_app_data.domain.AppOrUrlItem
import com.example.undistract.features.select_apps.data.SelectAppsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SelectAppsViewModel(
    private val appDataRepository: AppDataRepository,
    private val selectAppsRepository: SelectAppsRepository
) : ViewModel() {

    val selectedApps = mutableStateMapOf<String, Boolean>()

    private val _combinedItems = MutableStateFlow<List<AppOrUrlItem>>(emptyList())
    val combinedItems: StateFlow<List<AppOrUrlItem>> = _combinedItems

    private val _isSelectAll = MutableStateFlow(false)
    val isSelectAll: StateFlow<Boolean> = _isSelectAll

    private val _selectedNotificationType = MutableStateFlow("Head Notification")
    val selectedNotificationType: StateFlow<String> = _selectedNotificationType.asStateFlow()

    init {
        loadCombinedItems()

        viewModelScope.launch {
            snapshotFlow { selectedApps.toMap() }
                .combine(_combinedItems) { selectedMap, items ->
                    if (items.isEmpty()) false
                    else items.all { selectedMap[it.identifier] == true }
                }
                .collectLatest {
                    _isSelectAll.value = it
                }
        }

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
            val items = appDataRepository.getCombinedList()
            val combined = items.sortedBy { it.name }

            _combinedItems.value = combined

            combined.forEach { item ->
                selectedApps[item.identifier] = selectAppsRepository.isAppSelected(item.identifier)
            }
        }
    }

    fun toggleSelectAll(identifiers: List<String>) {
        val shouldSelectAll = !identifiers.all { selectedApps[it] == true }

        identifiers.forEach { id ->
            selectedApps[id] = shouldSelectAll
        }

        selectAppsRepository.setSelectedApps(identifiers, shouldSelectAll)
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
        Log.d("SelectAppsViewModel", "Updating notification type to: $notificationType")
        _selectedNotificationType.value = notificationType
    }
}