package com.example.undistract.features.parental_control.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.undistract.features.parental_control.data.ParentalControlRepository
import com.example.undistract.features.parental_control.data.local.PinDao
import com.example.undistract.features.parental_control.data.local.PinEntity
import kotlinx.coroutines.launch

class ParentalControlViewModel(
    private val repository: ParentalControlRepository
) : ViewModel() {
    var hasPin by mutableStateOf<Boolean?>(null)

    fun checkIfPinExists() {
        viewModelScope.launch {
            hasPin = repository.getPin() != null
        }
    }

    fun addPin(pin: String) {
        viewModelScope.launch {
            repository.addPin(pin)
        }
    }

    suspend fun verifyPin(input: String): Boolean {
        val storedPin = repository?.getPin().toString()
        return storedPin == input
    }
}

