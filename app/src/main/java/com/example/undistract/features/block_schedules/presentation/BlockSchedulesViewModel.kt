package com.example.undistract.features.block_schedules.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.undistract.features.block_schedules.data.BlockSchedulesRepository
import kotlinx.coroutines.launch

class BlockSchedulesViewModel(
    private val repository: BlockSchedulesRepository
) : ViewModel() {

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