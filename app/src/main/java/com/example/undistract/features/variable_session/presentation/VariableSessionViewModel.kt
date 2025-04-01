package com.example.undistract.features.variable_session.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.undistract.features.variable_session.data.VariableSessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VariableSessionViewModel(
    private val repository: VariableSessionRepository
) : ViewModel() {

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> get() = _showDialog

    fun addVariableSession(
        apps: List<Pair<String, String>>,
        minutesLeft: Int,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            repository.addVariableSessionForMultipleApps(apps, minutesLeft, isActive)
        }
    }

    fun checkApp(packageName: String) {
        viewModelScope.launch {
            Log.d("LOG_VIEWMODEL","VIEW MODEL OK")
            if (packageName == "com.google.android.youtube") {
                _showDialog.value = true
                Log.d("LOG_VIEWMODEL","Show Dialog: ${_showDialog.value}")
            } else {
                _showDialog.value = false
            }
        }
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