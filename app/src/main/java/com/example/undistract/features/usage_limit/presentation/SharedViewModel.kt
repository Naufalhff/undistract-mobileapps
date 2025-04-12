package com.example.undistract.features.usage_limit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.undistract.features.usage_limit.domain.AppLimitInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SharedViewModel : ViewModel() {
    private val _appLimitInfo = MutableStateFlow<AppLimitInfo?>(null)
    val appLimitInfo: StateFlow<AppLimitInfo?> = _appLimitInfo

    fun setAppLimitInfo(appLimitInfo: AppLimitInfo) {
        viewModelScope.launch {
            _appLimitInfo.value = appLimitInfo
        }
    }

    fun clearAppLimitInfo() {
        viewModelScope.launch {
            _appLimitInfo.value = null
        }
    }
} 