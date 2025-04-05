package com.example.undistract.features.variable_session.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.undistract.features.variable_session.data.VariableSessionRepository
import com.example.undistract.features.variable_session.data.local.VariableSessionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VariableSessionViewModel(
    private val repository: VariableSessionRepository
) : ViewModel() {

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> get() = _showDialog

    suspend fun getVariableSession(packageName: String): List<VariableSessionEntity> {
        return repository.getVariableSession(packageName)
    }

    fun addVariableSession(
        apps: List<Pair<String, String>>,
        secondsLeft: Int,
        coolDownDuration: Long?,
        coolDownEndTime: Long?,
        isOnCooldown: Boolean,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            repository.addVariableSessionForMultipleApps(apps, secondsLeft, coolDownDuration, coolDownEndTime, isOnCooldown, isActive)
        }
    }

    fun updateSecondsLeft(
        packageName: String,
        secondsLeft: Int
    ) {
        viewModelScope.launch {
            repository.updateSecondsLeft(packageName, secondsLeft)
        }
    }

    fun subtractSecondsLeft(
        packageName: String,
        secondsLeft: Int
    ) {
        viewModelScope.launch {
            repository.subtractSecondsLeft(packageName, secondsLeft)
        }
    }

    fun updateIsActive(
        packageName: String,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            repository.updateIsActive(packageName, isActive)
        }
    }

    suspend fun stillHas60Second(packageName: String): Boolean {
        val session = repository.getVariableSession(packageName).firstOrNull()
        return session?.let {
            it.secondsLeft >= 60
        } ?: false
    }

    fun dismissDialog() {
        _showDialog.value = false
    }

//    fun deleteBlockSchedule(id: Int) {
//        viewModelScope.launch {
//            repository.deleteBlockSchedules(id)
//        }
//    }
}