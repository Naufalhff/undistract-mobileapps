package com.example.undistract.features.setadaily_limit.presentation

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.undistract.features.get_installed_apps.domain.AppInfo
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepository
import com.example.undistract.features.setadaily_limit.data.local.SetaDailyLimitEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SetaDailyLimitViewModel(
    private val repository: SetaDailyLimitRepository,
    private val appContext: Context? = null,
    private val isParental: Boolean
) : ViewModel() {
    private val TAG = "SetaDailyLimitViewModel"

    private val _saveResult = MutableStateFlow<SaveResult>(SaveResult.Idle)
    val saveResult: StateFlow<SaveResult> = _saveResult.asStateFlow()

    private val _dailyLimits = MutableStateFlow<List<SetaDailyLimitEntity>>(emptyList())

    init {
        viewModelScope.launch {
            repository.getAll(isParental).collect { limits ->
                Log.d(TAG, "Received ${limits.size} limits from database")
                _dailyLimits.value = limits
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

    sealed class SaveResult {
        object Idle : SaveResult()
        object Loading : SaveResult()
        object Success : SaveResult()
        data class Error(val message: String) : SaveResult()
    }
}