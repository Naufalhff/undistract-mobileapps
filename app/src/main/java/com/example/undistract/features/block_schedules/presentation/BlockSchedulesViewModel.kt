package com.example.undistract.features.block_schedules.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.undistract.features.block_schedules.data.BlockSchedulesRepository
import com.example.undistract.features.block_schedules.data.local.BlockSchedulesEntity
import kotlinx.coroutines.launch

class BlockSchedulesViewModel(
    private val repository: BlockSchedulesRepository,
    private val isParental: Boolean
) : ViewModel() {

    val blockedSchedules: LiveData<List<BlockSchedulesEntity>> =
        repository.getAllBlockSchedules(isParental).asLiveData()

    private fun addBlockSchedules(
        apps: List<Pair<String, String>>,
        daysOfWeek: String,
        isAllDay: Boolean,
        startTime: String?,
        endTime: String?,
        isActive: Boolean,
        isParental: Boolean
    ) {
        viewModelScope.launch {
            repository.addBlockSchedulesForMultipleApps(
                apps, daysOfWeek, isAllDay, startTime, endTime, isActive,
                isParental = isParental
            )
        }
    }

    fun saveBlockSchedule(
        apps: List<Pair<String, String>>,
        daysOfWeek: String,
        isAllDay: Boolean,
        startTime: String?,
        endTime: String?,
        isActive: Boolean,
        isParental: Boolean,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        viewModelScope.launch {
            try {
                addBlockSchedules(
                    apps = apps,
                    daysOfWeek = daysOfWeek,
                    isAllDay = isAllDay,
                    startTime = startTime,
                    endTime = endTime,
                    isActive = isActive,
                    isParental = isParental
                )
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    fun deleteBlockSchedule(id: Int) {
        viewModelScope.launch {
            repository.deleteBlockSchedules(id)
        }
    }
}