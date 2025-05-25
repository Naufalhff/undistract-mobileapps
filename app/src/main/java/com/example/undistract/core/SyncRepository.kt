package com.example.undistract.core

import android.content.Context
import android.util.Log
import com.example.undistract.features.authentication_parental.data.local.PinDao
import com.example.undistract.features.block_permanent.data.local.BlockPermanentDao
import com.example.undistract.features.block_schedules.data.local.BlockSchedulesDao
import com.example.undistract.features.setadaily_limit.data.local.SetaDailyLimitDao
import com.example.undistract.features.variable_session.data.local.VariableSessionDao
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SyncRepository(
    private val blockSchedulesDao: BlockSchedulesDao,
    private val variableSessionDao: VariableSessionDao,
    private val blockPermanentDao: BlockPermanentDao,
    private val setaDailyLimitDao: SetaDailyLimitDao,
    private val pinDao: PinDao,
    private val apiService: ApiService,
    private val context: Context
) {

    suspend fun fetchAll(): Boolean = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("userId", -1)

        if (userId == -1) {
            return@withContext false
        }

        val response = apiService.fetchAllData(userId)

        if (response.isSuccessful) {
            val data = response.body() ?: return@withContext false

            // Kosongkan semua data lokal dengan isSynced = true
            blockSchedulesDao.clearAll()
            variableSessionDao.clearAll()
            blockPermanentDao.clearAll()
            setaDailyLimitDao.clearAll()
            pinDao.clearAll()

            // Masukkan data baru
            blockSchedulesDao.insertAll(data.blockSchedules)
            variableSessionDao.insertAll(data.variableSessions)
            blockPermanentDao.insertAll(data.blockPermanents)
            setaDailyLimitDao.insertAll(data.dailyLimits)
            val modifiedPins = data.userParents.map { pin ->
                pin.copy(id = 0)
            }
            pinDao.insertAll(modifiedPins)

            return@withContext true
        } else {
            return@withContext false
        }
    }


    suspend fun syncAll(): Boolean = withContext(Dispatchers.IO) {
        val blockSchedules = blockSchedulesDao.getAll().map { it.copy(isSynced = true) }
        val variableSessions = variableSessionDao.getAll().map { it.copy(isSynced = true) }
        val blockPermanents = blockPermanentDao.getAll().map { it.copy(isSynced = true) }
        val dailyLimits = setaDailyLimitDao.getAll().map { it.copy(isSynced = true) }
        val userParents = pinDao.getAll().map { it.copy(isSynced = true) }

        val existingUuids = ExistingUuids(
            blockSchedules = blockSchedules.map { it.uuid },
            variableSessions = variableSessions.map { it.uuid },
            blockPermanents = blockPermanents.map { it.uuid },
            dailyLimits = dailyLimits.map { it.uuid }
        )

        val body = SyncRequestBody(blockSchedules, variableSessions, blockPermanents, dailyLimits, userParents, existingUuids)
        val jsonBody = Gson().toJson(body)
        Log.d("SyncPayload", jsonBody)

        val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        val userId = prefs.getInt("userId", -1)

        if (userId == -1) {
            return@withContext false
        }
        val response = apiService.syncAllData(userId, body)
        if (response.isSuccessful) {
            blockSchedulesDao.markAllAsSynced()
            variableSessionDao.markAllAsSynced()
            blockPermanentDao.markAllAsSynced()
            setaDailyLimitDao.markAllAsSynced()
            pinDao.markAllAsSynced()
        }
        return@withContext response.isSuccessful
    }
}