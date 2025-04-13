package com.example.undistract.features.block_permanent.data

import com.example.undistract.features.block_permanent.data.local.BlockPermanentDao
import com.example.undistract.features.block_permanent.data.local.BlockPermanentEntity
import kotlinx.coroutines.flow.Flow

class BlockPermanentRepository (private val dao: BlockPermanentDao) {

    suspend fun insertBlockPermanent(data: BlockPermanentEntity) {
        dao.insertBlockPermanent(data)
    }

    suspend fun getBlockPermanent(packageName: String): List<BlockPermanentEntity> {
        return dao.getBlockPermanent(packageName)
    }

    suspend fun deleteBlockPermanent(id: Int) {
        dao.deleteBlockPermanent(id)
    }

    suspend fun updateIsActive(id: Int, isActive: Boolean) {
        dao.updateIsActive(id, isActive)
    }

    fun getActiveBlockPermanent(): Flow<List<BlockPermanentEntity>> {
        return dao.getActiveBlockPermanent()
    }

    suspend fun deleteBlockPermanentById(id: Int) {
        dao.deleteBlockPermanent(id)
    }
}