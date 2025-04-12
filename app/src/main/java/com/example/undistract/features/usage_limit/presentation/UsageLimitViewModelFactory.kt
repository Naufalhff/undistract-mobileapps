package com.example.undistract.features.usage_limit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepository
import com.example.undistract.features.block_schedules.data.BlockSchedulesRepository

class UsageLimitViewModelFactory(
    private val repository: SetaDailyLimitRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UsageLimitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UsageLimitViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}