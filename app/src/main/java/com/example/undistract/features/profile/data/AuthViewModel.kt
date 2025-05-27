package com.example.undistract.features.profile.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginResult = MutableStateFlow<Boolean?>(null)
    val loginResult: StateFlow<Boolean?> = _loginResult

    private val _registerResult = MutableStateFlow<Boolean?>(null)
    val registerResult: StateFlow<Boolean?> = _registerResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            val success = repository.login(email, password)
            _loginResult.value = success
        }
    }

    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            val success = repository.register(username, email, password)
            _registerResult.value = success
        }
    }

    fun getUserId(): Int = repository.getLoggedInUserId()

    fun getToken(): String? = repository.getAuthToken()
}
