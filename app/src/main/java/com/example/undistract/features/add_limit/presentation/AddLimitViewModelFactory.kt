package com.example.undistract.features.add_limit.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class AddLimitViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddLimitViewModel::class.java)) {
            return AddLimitViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}