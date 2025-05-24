package com.example.undistract.features.authentication_parental.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.undistract.features.authentication_parental.data.AuthenticationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthenticationViewModel(
    private val repository: AuthenticationRepository
) : ViewModel() {

    suspend fun verifyOtp(email: String, otp: String): Boolean {
        return repository.verifyOtp(email, otp)
    }

    fun sendOtp(email: String) {
        viewModelScope.launch {
            repository.sendOtp(email)
        }
    }

    var hasPin by mutableStateOf<Boolean?>(null)

    private val _isVerified = MutableStateFlow(false)
    val isVerified = _isVerified.asStateFlow()

    fun setVerified(value: Boolean) {
        _isVerified.value = value
    }

    fun resetVerificationStatus() {
        _isVerified.value = false
    }

    // Fungsi untuk memeriksa apakah status saat ini terverifikasi
    fun isCurrentlyVerified(): Boolean {
        return _isVerified.value
    }

    fun checkIfPinExists() {
        viewModelScope.launch {
            hasPin = repository.getPin() != null
        }
    }

    fun addPin(pin: String, email: String) {
        viewModelScope.launch {
            repository.addPin(pin, email)
        }
    }

    suspend fun verifyPin(input: String): Boolean {
        val storedPin = repository.getPin()
        return storedPin != null && storedPin.toString() == input
    }

    fun updatePin(newPin: String){
        viewModelScope.launch {
            repository.updatePin(newPin)
        }
    }

    suspend fun getEmail(): String? {
        return repository.getEmail()
    }
}

