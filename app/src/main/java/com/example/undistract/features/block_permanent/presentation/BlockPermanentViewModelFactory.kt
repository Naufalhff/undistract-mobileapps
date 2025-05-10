package com.example.undistract.features.block_permanent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.undistract.features.block_permanent.data.BlockPermanentRepository

class BlockPermanentViewModelFactory(
    private val repository: BlockPermanentRepository,
    private val isParental: Boolean
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BlockPermanentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BlockPermanentViewModel(repository, isParental) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}