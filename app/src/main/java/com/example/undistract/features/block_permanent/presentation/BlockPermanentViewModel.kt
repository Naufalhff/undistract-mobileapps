package com.example.undistract.features.block_permanent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.undistract.features.block_permanent.data.BlockPermanentRepository
import com.example.undistract.features.block_permanent.data.local.BlockPermanentEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class BlockPermanentViewModel (private val repository: BlockPermanentRepository) : ViewModel() {

    fun insertBlockPermanent(data: BlockPermanentEntity) {
        viewModelScope.launch {
            repository.insertBlockPermanent(data)
        }
    }
}