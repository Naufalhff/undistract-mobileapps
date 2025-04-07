package com.example.undistract.features.select_apps.presentation

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.get_installed_apps.domain.AppInfo
import com.example.undistract.features.get_installed_apps.domain.GetInstalledAppsUseCase
import com.example.undistract.features.select_apps.data.SelectAppsRepository
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepositoryImpl
import com.example.undistract.features.setadaily_limit.data.local.SetaDailyLimitEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SelectAppsViewModel(
    private val getInstalledAppsUseCase: GetInstalledAppsUseCase,
    private val selectAppsRepository: SelectAppsRepository,
    private val appContext: Context
) : ViewModel() {

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    // State map untuk UI
    val selectedApps = mutableStateMapOf<String, Boolean>()

    private val _dailyLimits = MutableStateFlow<List<SetaDailyLimitEntity>>(emptyList())
    val dailyLimits: StateFlow<List<SetaDailyLimitEntity>> = _dailyLimits.asStateFlow()

    init {
        loadInstalledApps(appContext)

        // Observer perubahan dari repository
        viewModelScope.launch {
            selectAppsRepository.selectedApps.collectLatest { selectedAppsMap ->
                // Update UI state map
                selectedAppsMap.forEach { (packageName, isSelected) ->
                    selectedApps[packageName] = isSelected
                }
            }
        }
    }

    private fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            val apps = getInstalledAppsUseCase(context)
            _installedApps.value = apps

            apps.forEach { app ->
                selectedApps[app.packageName] = selectAppsRepository.isAppSelected(app.packageName)
            }
        }
    }

    // Update rute saat ini
    fun updateCurrentRoute(route: String) {
        selectAppsRepository.setCurrentRoute(route)
    }

    // Toggle selection status untuk aplikasi
    fun toggleAppSelection(packageName: String, isSelected: Boolean) {
        selectedApps[packageName] = isSelected
        selectAppsRepository.toggleAppSelection(packageName, isSelected)
    }

    // Mendapatkan daftar aplikasi yang dipilih
    fun getSelectedApps(): List<String> {
        return selectAppsRepository.getSelectedApps()
    }

    // Callback ketika rute berubah
    fun onRouteChanged(newRoute: String?) {
        selectAppsRepository.checkAndClearDataIfNeeded(newRoute)
    }

    fun getSelectedAppsInfo(): List<AppInfo> {
        return installedApps.value.filter { app ->
            selectedApps[app.packageName] == true
        }
    }

    fun refreshDailyLimits() {
        viewModelScope.launch {
            try {
                val database = AppDatabase.getDatabase(appContext)
                val repository = SetaDailyLimitRepositoryImpl(database.setaDailyLimitDao())
                repository.getAll().collect { limits ->
                    _dailyLimits.value = limits
                    // Break after first collection to avoid continuous collection
                    return@collect
                }
            } catch (e: Exception) {
                Log.e("SelectAppsViewModel", "Error refreshing daily limits", e)
            }
        }
    }
}