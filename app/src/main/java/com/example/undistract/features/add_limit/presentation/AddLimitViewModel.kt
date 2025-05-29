package com.example.undistract.features.add_limit.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.undistract.features.add_limit.data.local.BlockScheduleData
import androidx.compose.runtime.State
import com.example.undistract.features.add_limit.data.local.VariableSessionData

class AddLimitViewModel : ViewModel() {
    private val _scheduleData = mutableStateOf<BlockScheduleData?>(null)
    private val _sessionData = mutableStateOf<VariableSessionData?>(null)
    val scheduleData: State<BlockScheduleData?> = _scheduleData
    val sessionData: State<VariableSessionData?> = _sessionData

    fun updateScheduleData(data: BlockScheduleData) {
        _scheduleData.value = data
    }

    fun updateSessionData(data: VariableSessionData) {
        _sessionData.value = data
    }
}