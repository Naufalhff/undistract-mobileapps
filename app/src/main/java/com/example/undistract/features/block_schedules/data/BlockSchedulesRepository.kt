package com.example.undistract.features.block_schedules.data

import com.example.undistract.features.block_schedules.data.local.BlockSchedulesEntity
import com.example.undistract.features.block_schedules.data.local.BlockSchedulesDao

class BlockSchedulesRepository(
    private val dao: BlockSchedulesDao
)  {

    // Menambahkan beberapa aplikasi sekaligus (setiap aplikasi sebagai baris terpisah)
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
                appName = app.first,   // Nama aplikasi
                packageName = app.second,  // Package name aplikasi
                daysOfWeek = daysOfWeek, // Hari yang dipilih (misalnya: "S, M, T, W")
                isAllDay = isAllDay, // Apakah blokir sepanjang hari?
                startTime = startTime, // Waktu mulai (null jika isAllDay = true)
                endTime = endTime, // Waktu akhir (null jika isAllDay = true)
                isActive = isActive // Status aktif atau tidak
            )
            dao.insertBlockSchedules(schedule)
        }
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