package com.example.undistract.features.get_app_data.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.undistract.features.get_app_data.data.AppDataRepository
import com.example.undistract.features.get_app_data.domain.AppOrUrlItem
import kotlinx.coroutines.launch

class AppDataViewModel(
    private val repository: AppDataRepository
) : ViewModel() {
    private val _combinedList = MutableLiveData<List<AppOrUrlItem>>()
    val combinedList: LiveData<List<AppOrUrlItem>> = _combinedList

    fun loadCombinedData() {
        viewModelScope.launch {
            val data = repository.getCombinedList()
            _combinedList.value = data
        }
    }
}