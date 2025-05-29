package com.example.undistract.features.block_permanent.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.undistract.features.block_permanent.data.BlockPermanentRepository
import com.example.undistract.features.block_permanent.data.local.BlockPermanentEntity
import com.example.undistract.features.get_app_data.domain.AppOrUrlItem
import kotlinx.coroutines.launch

class BlockPermanentViewModel (
    private val repository: BlockPermanentRepository,
) : ViewModel() {

    private fun insertBlockPermanent(data: BlockPermanentEntity) {
        viewModelScope.launch {
            repository.insertBlockPermanent(data)
        }
    }

    fun saveBlockedApps(
        selectedApps: List<AppOrUrlItem>,
        isParental: Boolean,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit
    ) {
        try {
            selectedApps.forEach { item ->
                val blockPermanentEntity = BlockPermanentEntity(
                    packageName = item.identifier,
                    appName = item.name,
                    isActive = true,
                    isParental = isParental
                )
                insertBlockPermanent(blockPermanentEntity)
                Log.d("BlockPermanentScreen", "Data saved: ${blockPermanentEntity.packageName}, ${blockPermanentEntity.appName}")
            }
            onSuccess()
        } catch (e: Exception) {
            Log.e("BlockPermanentScreen", "Error saving data: ${e.message}", e)
            onError(e)
        }
    }
}