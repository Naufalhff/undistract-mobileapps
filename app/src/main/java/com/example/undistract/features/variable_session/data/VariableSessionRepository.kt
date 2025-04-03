package com.example.undistract.features.variable_session.data

import com.example.undistract.features.block_schedules.data.local.BlockSchedulesEntity
import com.example.undistract.features.variable_session.data.local.VariableSessionDao
import com.example.undistract.features.variable_session.data.local.VariableSessionEntity

class VariableSessionRepository(
    private val dao: VariableSessionDao
)  {

    suspend fun getVariableSession(packageName: String): List<VariableSessionEntity> {
        return dao.getVariableSession(packageName)
    }

    suspend fun addVariableSessionForMultipleApps(
        apps: List<Pair<String, String>>,
        secondsLeft: Int,
        isActive: Boolean
    ) {
        for (app in apps) {
            val variableSession = VariableSessionEntity(
                appName = app.first,   // Nama aplikasi
                packageName = app.second,  // Package name aplikasi
                secondsLeft = secondsLeft,
                isActive = isActive
            )
            dao.insertVariableSession(variableSession)
        }
    }

    suspend fun updateSecondsLeft(
        packageName: String,
        secondsLeft: Int
    ) {
        dao.updateSecondsLeft(packageName, secondsLeft)
    }

    suspend fun subtractSecondsLeft(
        packageName: String,
        secondsLeft: Int
    ) {
        dao.subtractSecondsLeft(packageName, secondsLeft)
    }

    suspend fun updateIsActive(
        packageName: String,
        isActive: Boolean
    ) {
        dao.updateIsActive(packageName, isActive)
    }

//    // Mendapatkan semua jadwal blokir yang tersimpan
//    suspend fun getAllBlockSchedules(): List<BlockSchedulesEntity> {
//        return dao.getAllBlockSchedules()
//    }
//
//    // Mendapatkan jadwal berdasarkan packageName aplikasi
//    suspend fun getBlockSchedulesForApp(packageName: String): List<BlockSchedulesEntity> {
//        return dao.getBlockSchedules(packageName)
//    }
//
//    // Menghapus jadwal blokir berdasarkan id
//    suspend fun deleteBlockSchedules(id: Int) {
//        dao.deleteBlockSchedulesById(id)
//    }
//
//    // Mengupdate status aktif/tidaknya pemblokiran
//    suspend fun updateBlockScheduleStatus(scheduleId: Int, isActive: Boolean) {
//        dao.updateBlockSchedulesById(scheduleId, isActive)
//    }
}