package com.example.undistract.features.block_schedules.data

import com.example.undistract.features.block_schedules.data.local.BlockSchedulesEntity
import com.example.undistract.features.block_schedules.data.local.BlockSchedulesDao
import kotlinx.coroutines.flow.Flow

class BlockSchedulesRepository(
    private val dao: BlockSchedulesDao
)  {

    fun getAllBlockSchedules(): List<BlockSchedulesEntity> = dao.getAllBlockSchedules()

    suspend fun addBlockSchedulesForMultipleApps(
        apps: List<Pair<String, String>>, // List pasangan (nama aplikasi, packageName)
        daysOfWeek: String,
        isAllDay: Boolean,
        startTime: String?,
        endTime: String?,
        isActive: Boolean
    ) {
        for (app in apps) {
            val schedule = BlockSchedulesEntity(
                appName = app.first,
                packageName = app.second,
                daysOfWeek = daysOfWeek,
                isAllDay = isAllDay,
                startTime = startTime,
                endTime = endTime,
                isActive = isActive
            )
            dao.insertBlockSchedules(schedule)
        }
    }
}