package com.example.undistract.features.setadaily_limit.presentation

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.get_installed_apps.domain.AppInfo
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepository
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepositoryImpl
import com.example.undistract.features.setadaily_limit.data.local.SetaDailyLimitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetaDailyLimitViewModel(
    private val repository: SetaDailyLimitRepository,
    private val appContext: Context? = null
) : ViewModel() {
    private val TAG = "SetaDailyLimitViewModel"

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps.asStateFlow()

    val selectedApps = mutableStateMapOf<String, Boolean>()

    private val _saveResult = MutableStateFlow<SaveResult>(SaveResult.Idle)
    val saveResult: StateFlow<SaveResult> = _saveResult.asStateFlow()

    private val _dailyLimits = MutableStateFlow<List<SetaDailyLimitEntity>>(emptyList())
    val dailyLimits: StateFlow<List<SetaDailyLimitEntity>> = _dailyLimits.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAll().collect { limits ->
                Log.d(TAG, "Received ${limits.size} limits from database")
                _dailyLimits.value = limits
            }
        }
    }

    fun addDailyLimit(setaDailyLimitEntity: SetaDailyLimitEntity) {
        viewModelScope.launch {
            _saveResult.value = SaveResult.Loading
            try {
                Log.d(TAG, "Adding daily limit for: ${setaDailyLimitEntity.appName}")
                val id = withContext(Dispatchers.IO) {
                    repository.insert(setaDailyLimitEntity)
                }
                Log.d(TAG, "Daily limit added successfully with ID: $id")
                _saveResult.value = SaveResult.Success
            } catch (e: Exception) {
                Log.e(TAG, "Error adding daily limit", e)
                e.printStackTrace()
                _saveResult.value = SaveResult.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addMultipleDailyLimits(entities: List<SetaDailyLimitEntity>, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            _saveResult.value = SaveResult.Loading
            try {
                Log.d(TAG, "Adding ${entities.size} daily limits")

                entities.forEach { entity ->
                    Log.d(TAG, "Adding daily limit for: ${entity.appName}")
                    val id = withContext(Dispatchers.IO) {
                        repository.insert(entity)
                    }
                    Log.d(TAG, "Daily limit added successfully with ID: $id")
                }

                _saveResult.value = SaveResult.Success
                onComplete(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error adding daily limits", e)
                e.printStackTrace()
                _saveResult.value = SaveResult.Error(e.message ?: "Unknown error")
                onComplete(false)
            }
        }
    }

    // Fungsi untuk mendapatkan daftar aplikasi yang dipilih beserta informasi lengkap
    fun getSelectedAppsInfo(): List<AppInfo> {
        return installedApps.value.filter { app ->
            selectedApps[app.packageName] == true
        }
    }



    sealed class SaveResult {
        object Idle : SaveResult()
        object Loading : SaveResult()
        object Success : SaveResult()
        data class Error(val message: String) : SaveResult()
    }
}