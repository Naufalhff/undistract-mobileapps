package com.example.undistract.features.add_limit.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.undistract.features.add_limit.data.local.BlockScheduleData
import androidx.compose.runtime.State
import com.example.undistract.features.add_limit.data.local.DailyLimitData
import com.example.undistract.features.add_limit.data.local.VariableSessionData

// AddLimitViewModel.kt
class AddLimitViewModel : ViewModel() {
    private val _scheduleData = mutableStateOf<BlockScheduleData?>(null)
    private val _sessionData = mutableStateOf<VariableSessionData?>(null)
    private val _dailyLimitData = mutableStateOf<DailyLimitData?>(null)

    val scheduleData: State<BlockScheduleData?> = _scheduleData
    val sessionData: State<VariableSessionData?> = _sessionData
    val dailyLimitData: State<DailyLimitData?> = _dailyLimitData

    fun updateScheduleData(data: BlockScheduleData) {
        _scheduleData.value = data
    }

    fun updateSessionData(data: VariableSessionData) {
        _sessionData.value = data
    }

    fun updateDailyLimitData(data: DailyLimitData) {
        _dailyLimitData.value = data
    }

    // Get daily limit data for AddLimitScreen to use
    fun getDailyLimitData(): DailyLimitData? = _dailyLimitData.value
}