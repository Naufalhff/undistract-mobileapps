package com.example.undistract.features.usage_limit.presentation

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.undistract.R
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepository
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepositoryImpl
import com.example.undistract.features.setadaily_limit.data.local.SetaDailyLimitEntity
import com.example.undistract.features.usage_stats.UsageStatsManager
import com.example.undistract.features.block_schedules.data.BlockSchedulesRepository
import com.example.undistract.features.block_schedules.data.local.BlockSchedulesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import com.example.undistract.features.variable_session.data.VariableSessionRepository
import com.example.undistract.features.variable_session.data.local.VariableSessionEntity
import com.example.undistract.features.block_permanent.data.BlockPermanentRepository
import com.example.undistract.features.block_permanent.data.local.BlockPermanentEntity

class UsageLimitViewModel(
    private val repository: SetaDailyLimitRepository,
    private val blockSchedulesRepository: BlockSchedulesRepository,
    private val variableSessionRepository: VariableSessionRepository,
    private val blockPermanentRepository: BlockPermanentRepository
) : ViewModel() {
    private val TAG = "UsageLimitViewModel"

    private val appContext: Context? = null

    private val _dailyLimits = MutableStateFlow<List<SetaDailyLimitEntity>>(emptyList())
    val dailyLimits: StateFlow<List<SetaDailyLimitEntity>> = _dailyLimits.asStateFlow()

    private val _appUsageProgress = MutableStateFlow<Map<String, Float>>(emptyMap())
    val appUsageProgress: StateFlow<Map<String, Float>> = _appUsageProgress.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var usageStatsManager: UsageStatsManager? = null
    private var trackingJob: kotlinx.coroutines.Job? = null

    // Tambahkan Flow untuk blockedApps
    val blockedApps: Flow<List<BlockSchedulesEntity>> = blockSchedulesRepository.getAllBlockSchedules()

    private val _variableSessions = MutableStateFlow<List<VariableSessionEntity>>(emptyList())
    val variableSessions: StateFlow<List<VariableSessionEntity>> get() = _variableSessions

    private val _blockPermanentApps = MutableStateFlow<List<BlockPermanentEntity>>(emptyList())
    val blockPermanentApps: StateFlow<List<BlockPermanentEntity>> get() = _blockPermanentApps

    init {
        viewModelScope.launch {
            repository.getAll().collect { limits ->
                _dailyLimits.value = limits
                if (limits.isNotEmpty()) {
                    Log.d(TAG, "Received ${limits.size} limits from database")
                    if (usageStatsManager != null) {
                        updateAppUsageProgress(true)
                    }
                } else {
                    // Handle empty state
                    _isLoading.value = false
                }
            }
        }

        viewModelScope.launch {
            variableSessionRepository.getAllVariableSession().collect { sessions ->
                _variableSessions.value = sessions
            }
        }

        viewModelScope.launch {
            blockPermanentRepository.getActiveBlockPermanent().collect { apps ->
                _blockPermanentApps.value = apps
            }
        }
    }

    fun initUsageTracking(context: Context) {
        if (usageStatsManager == null) {
            usageStatsManager = UsageStatsManager(context)

            if (usageStatsManager?.hasUsageStatsPermission() == true) {
                // Immediately update usage stats when initialized
                viewModelScope.launch {
                    updateAppUsageProgress(true)
                    startUsageTracking()
                }
            } else {
                _isLoading.value = false
                usageStatsManager?.requestUsageStatsPermission()
            }
        } else {
            // If already initialized, just refresh
            refreshUsageStats()
        }
    }

    private fun startUsageTracking() {
        // Cancel any existing tracking job
        trackingJob?.cancel()

        trackingJob = viewModelScope.launch {
            while (true) {
                updateAppUsageProgress(false)
                // Update every 30 seconds
                delay(30000)
            }
        }
    }

    private suspend fun updateAppUsageProgress(isInitialLoad: Boolean = false) {
        try {
            if (isInitialLoad) {
                _isLoading.value = true
            }

            // Use Dispatchers.IO for database and usage stats operations
            withContext(Dispatchers.IO) {
                val progressMap = mutableMapOf<String, Float>()

                _dailyLimits.value.forEach { limit ->
                    // Only track progress for active apps
                    if (limit.isActive) {
                        val progress = usageStatsManager?.calculateProgress(
                            limit.packageName,
                            limit.timeLimitMinutes
                        ) ?: 0f

                        progressMap[limit.packageName] = progress
                        Log.d(TAG, "App: ${limit.appName}, Usage: $progress of limit")
                    } else {
                        // For inactive apps, set progress to 0
                        progressMap[limit.packageName] = 0f
                    }
                }

                _appUsageProgress.value = progressMap
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error updating app usage progress", e)
        } finally {
            if (isInitialLoad) {
                _isLoading.value = false
            }
        }
    }

    // Toggle active state for an app
    fun toggleAppActiveState(id: Int, isActive: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleActiveState(id, isActive)
                Log.d(TAG, "Toggled app ID $id to active=$isActive")
                // Force refresh usage stats
                updateAppUsageProgress(false)
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling app active state", e)
            }
        }
    }

    // Force an immediate update of usage stats
    fun refreshUsageStats() {
        viewModelScope.launch {
            updateAppUsageProgress(true)
        }
    }

    // Get progress for a specific app
    fun getAppProgress(packageName: String): Float {
        return _appUsageProgress.value[packageName] ?: 0f
    }

    override fun onCleared() {
        super.onCleared()
        trackingJob?.cancel()
    }

    fun addDailyLimit(setaDailyLimitEntity: SetaDailyLimitEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Adding daily limit for: ${setaDailyLimitEntity.appName}")
                repository.insert(setaDailyLimitEntity)
                Log.d(TAG, "Daily limit added successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding daily limit", e)
                e.printStackTrace()
            }
        }
    }

    fun refreshLimits() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Refreshing daily limits from database")
                // The repository.getAll() is already a Flow, so it will automatically update
                // But we can force a refresh by collecting the latest values
                repository.getAll().collect { limits ->
                    Log.d(TAG, "Refreshed ${limits.size} limits from database")
                    _dailyLimits.value = limits
                    // Break after first collection to avoid continuous collection
                    return@collect
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing daily limits", e)
            }
        }
    }

    fun deleteUsageLimit(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteLimit(id)
                // Refresh the list after deletion
                refreshUsageStats()
            } catch (e: Exception) {
                Log.e("UsageLimitViewModel", "Error deleting usage limit", e)
            }
        }
    }
    // Function moved from SelectAppsViewModel
    fun refreshDailyLimits() {
        viewModelScope.launch {
            try {
                if (appContext != null) {
                    val database = AppDatabase.getDatabase(appContext)
                    val tempRepository = SetaDailyLimitRepositoryImpl(database.setaDailyLimitDao())
                    tempRepository.getAll().collect { limits ->
                        _dailyLimits.value = limits
                        // Break after first collection to avoid continuous collection
                        return@collect
                    }
                } else {
                    // Use the injected repository if appContext is null
                    repository.getAll().collect { limits ->
                        _dailyLimits.value = limits
                        // Break after first collection to avoid continuous collection
                        return@collect
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing daily limits", e)
            }
        }
    }

    fun updateDailyLimit(id: Int, newLimitMinutes: Int) {
        viewModelScope.launch {
            try {
                val entity = repository.getById(id)
                entity?.let {
                    val updatedEntity = it.copy(timeLimitMinutes = newLimitMinutes)
                    repository.update(updatedEntity)
                    refreshUsageStats()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating daily limit", e)
            }
        }
    }

    fun deleteDailyLimitById(id: Int) {
        viewModelScope.launch {
            repository.deleteDailyLimitById(id)
            refreshLimits()
        }
    }

    // Fungsi untuk mengambil icon aplikasi
    fun getAppIcon(context: Context, packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            ContextCompat.getDrawable(context, R.drawable.app_logo) // Fallback icon
        }
    }

    // Fungsi untuk mengupdate status toggle
    fun toggleBlockSchedule(id: Int, isActive: Boolean) {
//        viewModelScope.launch {
//            blockSchedulesRepository.updateBlockScheduleActiveState(id, isActive)
//        }
    }

    fun toggleVariableSessionActiveState(packageName: String, isActive: Boolean) {
        viewModelScope.launch {
            variableSessionRepository.updateIsActive(packageName, isActive)
        }
    }

    fun updateVariableSessionUsage(packageName: String, secondsUsed: Int) {
        viewModelScope.launch {
            variableSessionRepository.subtractSecondsLeft(packageName, secondsUsed)
        }
    }

    fun toggleBlockPermanentActiveState(id: Int, isActive: Boolean) {
        viewModelScope.launch {
            blockPermanentRepository.updateIsActive(id, isActive)

        }
    }

    fun fetchBlockPermanent(packageName: String) {
        viewModelScope.launch {
            val blockPermanentApps = blockPermanentRepository.getBlockPermanent(packageName)
            // Lakukan sesuatu dengan blockPermanentApps
        }
    }

    fun deleteBlockScheduleById(id: Int) {
        viewModelScope.launch {
            blockSchedulesRepository.deleteBlockSchedules(id)
            refreshBlockedApps()
        }
    }

    fun deleteVariableSessionById(id: String) {
        viewModelScope.launch {
            variableSessionRepository.deleteVariableSessionById(id)
            refreshVariableSessions()
        }
    }

    fun deleteBlockPermanentById(id: Int) {
        viewModelScope.launch {
            blockPermanentRepository.deleteBlockPermanentById(id)
            refreshBlockPermanentApps()
        }
    }

    fun refreshBlockedApps() {
        viewModelScope.launch {
            blockSchedulesRepository.getAllBlockSchedules().collect { apps ->
                // Update state if needed
            }
        }
    }

    fun refreshVariableSessions() {
        viewModelScope.launch {
            variableSessionRepository.getAllVariableSession().collect { sessions ->
                // Update state if needed
            }
        }
    }

    fun refreshBlockPermanentApps() {
        viewModelScope.launch {
            blockPermanentRepository.getActiveBlockPermanent().collect { apps ->
                // Update state if needed
            }
        }
    }
}