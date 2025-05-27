package com.example.undistract.core

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SyncViewModel(private val syncRepository: SyncRepository) : ViewModel() {

    fun syncAllData(context: Context) {
        viewModelScope.launch {
            val success = syncRepository.syncAll()
            if (success) {
                Toast.makeText(context, "Sync sukses", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Sync gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun fetchAllData(context: Context) {
        viewModelScope.launch {
            val success = syncRepository.fetchAll()
            if (success) {
                Toast.makeText(context, "Fetch sukses", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Fetch gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }
}