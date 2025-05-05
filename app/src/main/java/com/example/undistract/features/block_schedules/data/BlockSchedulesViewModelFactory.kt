package com.example.undistract.features.block_schedules.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.undistract.features.block_schedules.presentation.BlockSchedulesViewModel

class BlockSchedulesViewModelFactory(
    private val repository: BlockSchedulesRepository,
    private val isParental: Boolean
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BlockSchedulesViewModel::class.java)) {
            return BlockSchedulesViewModel(repository, isParental) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}