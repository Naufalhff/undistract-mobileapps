package com.example.undistract.features.block_schedules.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.undistract.features.block_schedules.data.BlockSchedulesRepository
import com.example.undistract.features.block_schedules.data.local.BlockSchedulesEntity
import kotlinx.coroutines.launch

class BlockSchedulesViewModel(
    private val repository: BlockSchedulesRepository
) : ViewModel() {

    val blockedSchedules: List<BlockSchedulesEntity> = repository.getAllBlockSchedules()

    fun addBlockSchedules(
        apps: List<Pair<String, String>>,
        daysOfWeek: String,
        isAllDay: Boolean,
        startTime: String?,
        endTime: String?,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            repository.addBlockSchedulesForMultipleApps(apps, daysOfWeek, isAllDay, startTime, endTime, isActive)
        }
    }

//    fun deleteBlockSchedule(id: Int) {
//        viewModelScope.launch {
//            repository.deleteBlockSchedules(id)
//        }
//    }
}