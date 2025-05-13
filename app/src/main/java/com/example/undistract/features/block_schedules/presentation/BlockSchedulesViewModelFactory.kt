package com.example.undistract.features.select_apps.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.block_schedules.data.BlockSchedulesRepository
import com.example.undistract.features.block_schedules.presentation.BlockSchedulesViewModel

class BlockSchedulesViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BlockSchedulesViewModel::class.java)) {
            val database = AppDatabase.getDatabase(context)
            val dao = database.blockSchedulesDao()
            val repository = BlockSchedulesRepository(dao)

            @Suppress("UNCHECKED_CAST")
            return BlockSchedulesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
