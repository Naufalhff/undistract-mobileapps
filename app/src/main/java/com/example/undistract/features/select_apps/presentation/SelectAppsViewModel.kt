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

    // Tambahan untuk error handling
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadCombinedItems()
        observeSelectedApps()
        observeSelectedAppsFromRepository()
    }

    private fun observeSelectedApps() {
        viewModelScope.launch {
            try {
                snapshotFlow { selectedApps.toMap() }
                    .combine(_combinedItems) { selectedMap, items ->
                        if (items.isEmpty()) false
                        else items.all { selectedMap[it.identifier] == true }
                    }
                    .collectLatest { isAllSelected ->
                        _isSelectAll.value = isAllSelected
                    }
            } catch (exception: Exception) {
                Log.e("SelectAppsViewModel", "Error observing selected apps", exception)
            }
        }
    }

    private fun observeSelectedAppsFromRepository() {
        viewModelScope.launch {
            try {
                selectAppsRepository.selectedApps.collectLatest { selectedAppsMap ->
                    selectedAppsMap.forEach { (identifier, isSelected) ->
                        selectedApps[identifier] = isSelected
                    }
                }
            } catch (exception: Exception) {
                Log.e("SelectAppsViewModel", "Error observing selected apps from repository", exception)
                _errorMessage.value = "An error occurred while loading selected apps data."
            }
        }
    }

    private fun loadCombinedItems() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                Log.d("SelectAppsViewModel", "Starting to load combined items")

                val items = appDataRepository.getCombinedList()

                if (items.isEmpty()) {
                    Log.w("SelectAppsViewModel", "No items returned from repository")
                    _errorMessage.value = "No apps found in the system."
                    return@launch
                }

                val combined = items.sortedBy { it.name }
                _combinedItems.value = combined

                Log.d("SelectAppsViewModel", "Loaded ${combined.size} items successfully")

                // Initialize selected apps with error handling
                combined.forEach { item ->
                    try {
                        selectedApps[item.identifier] = selectAppsRepository.isAppSelected(item.identifier)
                    } catch (exception: Exception) {
                        Log.w("SelectAppsViewModel", "Error checking selection for ${item.identifier}", exception)
                        selectedApps[item.identifier] = false
                    }
                }

            } catch (securityException: SecurityException) {
                Log.e("SelectAppsViewModel", "Security error loading apps", securityException)
                _errorMessage.value = "Unable to access app list due to system permission restrictions."
            } catch (runtimeException: RuntimeException) {
                Log.e("SelectAppsViewModel", "Runtime error loading apps", runtimeException)
                _errorMessage.value = "An error occurred while loading apps. Please try again."
            } catch (exception: Exception) {
                Log.e("SelectAppsViewModel", "Unexpected error loading apps", exception)
                _errorMessage.value = "An unexpected error occurred. Please restart the app or contact support."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun retryLoadApps() {
        Log.d("SelectAppsViewModel", "Retrying to load apps")
        loadCombinedItems()
    }

    fun toggleSelectAll(identifiers: List<String>) {
        try {
            val shouldSelectAll = !identifiers.all { selectedApps[it] == true }

            identifiers.forEach { id ->
                selectedApps[id] = shouldSelectAll
            }

            selectAppsRepository.setSelectedApps(identifiers, shouldSelectAll)
            Log.d("SelectAppsViewModel", "Select all toggled: $shouldSelectAll for ${identifiers.size} items")

        } catch (exception: Exception) {
            Log.e("SelectAppsViewModel", "Error toggling select all", exception)
        }
    }

    fun toggleAppSelection(identifier: String, isSelected: Boolean) {
        try {
            selectedApps[identifier] = isSelected
            selectAppsRepository.toggleAppSelection(identifier, isSelected)
            Log.d("SelectAppsViewModel", "App selection toggled: $identifier = $isSelected")

        } catch (exception: Exception) {
            Log.e("SelectAppsViewModel", "Error toggling app selection for $identifier", exception)
        }
    }

    fun getSelectedIdentifiers(): List<String> {
        return try {
            selectAppsRepository.getSelectedApps()
        } catch (exception: Exception) {
            Log.e("SelectAppsViewModel", "Error getting selected identifiers", exception)
            emptyList()
        }
    }

    fun getSelectedItems(): List<AppOrUrlItem> {
        return try {
            combinedItems.value.filter { selectedApps[it.identifier] == true }
        } catch (exception: Exception) {
            Log.e("SelectAppsViewModel", "Error getting selected items", exception)
            emptyList()
        }
    }

    fun updateSelectedNotificationType(notificationType: String) {
        try {
            Log.d("SelectAppsViewModel", "Updating notification type to: $notificationType")
            _selectedNotificationType.value = notificationType
        } catch (exception: Exception) {
            Log.e("SelectAppsViewModel", "Error updating notification type", exception)
        }
    }
}