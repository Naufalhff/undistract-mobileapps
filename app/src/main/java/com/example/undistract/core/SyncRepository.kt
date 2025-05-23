package com.example.undistract.core

import android.util.Log
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
    private val apiService: ApiService
) {
    suspend fun syncAll(): Boolean = withContext(Dispatchers.IO) {
        val blockSchedules = blockSchedulesDao.getAll()
        val variableSessions = variableSessionDao.getAll()
        val blockPermanents = blockPermanentDao.getAll()
        val dailyLimits = setaDailyLimitDao.getAll()

        val body = SyncRequestBody(blockSchedules, variableSessions, blockPermanents, dailyLimits)
        val jsonBody = Gson().toJson(body)
        Log.d("SyncPayload", jsonBody)
        val response = apiService.syncAllData(body)
        return@withContext response.isSuccessful
    }
}