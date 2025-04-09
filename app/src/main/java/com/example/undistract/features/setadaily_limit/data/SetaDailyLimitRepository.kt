package com.example.undistract.features.setadaily_limit.data

import android.util.Log
import com.example.undistract.features.setadaily_limit.data.local.SetaDailyLimitDao
import com.example.undistract.features.setadaily_limit.data.local.SetaDailyLimitEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface SetaDailyLimitRepository {
    suspend fun insert(setaDailyLimitEntity: SetaDailyLimitEntity): Long
    fun getAll(): Flow<List<SetaDailyLimitEntity>>
    suspend fun getByPackageName(packageName: String): SetaDailyLimitEntity?
    suspend fun deleteById(id: Int)
    suspend fun deleteByPackageName(packageName: String)
    suspend fun toggleActiveState(id: Int, isActive: Boolean)
    suspend fun update(entity: SetaDailyLimitEntity)
    suspend fun getAllSync(): List<SetaDailyLimitEntity>
    suspend fun getById(id: Int): SetaDailyLimitEntity?
    suspend fun deleteLimit(id: Int)
    suspend fun deleteDailyLimitById(id: Int)
}

class SetaDailyLimitRepositoryImpl(private val dao: SetaDailyLimitDao) : SetaDailyLimitRepository {
    private val TAG = "SetaDailyLimitRepo"

    override suspend fun insert(setaDailyLimitEntity: SetaDailyLimitEntity): Long {
        try {
            Log.d(TAG, "Inserting limit for: ${setaDailyLimitEntity.appName}")
            val id = dao.insert(setaDailyLimitEntity)
            Log.d(TAG, "Insert successful with ID: $id")
            return id
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting limit", e)
            e.printStackTrace()
            throw e  // Re-throw exception to be caught by ViewModel
        }
    }

    override fun getAll(): Flow<List<SetaDailyLimitEntity>> {
        Log.d(TAG, "Getting all daily limits")
        return dao.getAll()
    }

    override suspend fun getByPackageName(packageName: String): SetaDailyLimitEntity? {
        Log.d(TAG, "Getting limit for package: $packageName")
        return dao.getByPackageName(packageName)
    }

    override suspend fun deleteById(id: Int) {
        try {
            Log.d(TAG, "Deleting limit with ID: $id")
            dao.deleteById(id)
            Log.d(TAG, "Delete successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting limit", e)
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun deleteByPackageName(packageName: String) {
        try {
            Log.d(TAG, "Deleting limit for package: $packageName")
            dao.deleteByPackageName(packageName)
            Log.d(TAG, "Delete successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting limit", e)
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun toggleActiveState(id: Int, isActive: Boolean) {
        try {
            Log.d(TAG, "Toggling active state for ID: $id to $isActive")
            dao.updateActiveState(id, isActive)
            Log.d(TAG, "Toggle successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error toggling active state", e)
            e.printStackTrace()
            throw e
        }
    }

    override suspend fun update(entity: SetaDailyLimitEntity) {
        try {
            Log.d(TAG, "Updating entity: ${entity.appName}")
            dao.update(entity)
            Log.d(TAG, "Update successful")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating entity", e)
            e.printStackTrace()
            throw e
        }
    }

    // Get all limits synchronously (not as Flow)
    override suspend fun getAllSync(): List<SetaDailyLimitEntity> {
        return try {
            dao.getAllSync()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all limits synchronously", e)
            emptyList()
        }
    }

    // Get app by ID
    override suspend fun getById(id: Int): SetaDailyLimitEntity? {
        return try {
            dao.getById(id)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting app by ID: $id", e)
            null
        }
    }

    override suspend fun deleteLimit(id: Int) {
        withContext(Dispatchers.IO) {
            dao.deleteById(id)
        }
    }

    override suspend fun deleteDailyLimitById(id: Int) {
        dao.deleteById(id)
    }
}