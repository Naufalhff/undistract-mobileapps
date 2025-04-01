package com.example.undistract.features.variable_session.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.undistract.features.variable_session.data.VariableSessionRepository

class VariableSessionViewModelFactory(private val repository: VariableSessionRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VariableSessionViewModel::class.java)) {
            return VariableSessionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
