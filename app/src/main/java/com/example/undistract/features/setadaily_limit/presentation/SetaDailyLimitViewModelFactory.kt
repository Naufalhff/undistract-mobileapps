package com.example.undistract.features.setadaily_limit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepositoryImpl

class SetaDailyLimitViewModelFactory(
    private val repository: SetaDailyLimitRepositoryImpl,
    private val isParental: Boolean
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SetaDailyLimitViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SetaDailyLimitViewModel(repository, isParental = isParental) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}