package com.example.undistract.features.add_limit.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.undistract.features.add_limit.data.local.ScheduleData
import androidx.compose.runtime.State

class AddLimitViewModel : ViewModel() {
    private val _scheduleData = mutableStateOf<ScheduleData?>(null)
    val scheduleData: State<ScheduleData?> = _scheduleData

    fun updateScheduleData(data: ScheduleData) {
        _scheduleData.value = data
    }
}