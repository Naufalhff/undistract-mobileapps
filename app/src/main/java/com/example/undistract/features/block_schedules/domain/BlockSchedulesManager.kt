package com.example.undistract.features.block_schedules.domain

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_BACK
import android.accessibilityservice.AccessibilityService.GLOBAL_ACTION_HOME
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.undistract.features.block_schedules.data.local.BlockSchedulesDao
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.LocalTime

// Kelas untuk mengatur logika blokir aplikasi berdasarkan waktu
class BlockScheduleManager(private val context: Context, private val dao: BlockSchedulesDao) {

    // Fungsi untuk memblokir aplikasi
    fun blockApp (service: AccessibilityService) {
        service.performGlobalAction(GLOBAL_ACTION_BACK)
        Handler(Looper.getMainLooper()).postDelayed({
            service.performGlobalAction(GLOBAL_ACTION_BACK)
        }, 200)
        Handler(Looper.getMainLooper()).postDelayed({
            service.performGlobalAction(GLOBAL_ACTION_HOME)
        }, 700)
    }

    // Fungsi untuk memeriksa apakah aplikasi diblokir dan dalam rentang waktu tertentu
    suspend fun shouldBlockApp(packageName: String, currentTime: LocalTime): Boolean {
        val blockSchedules = dao.getBlockSchedules(packageName)
        Log.d("DATABASE_TEST", "blockschedules: $blockSchedules")

        if (blockSchedules.none { it.isActive }) return false

        val todayIndex = LocalDate.now().dayOfWeek.value % 7

        return blockSchedules.any { blockSchedule ->
            // Cek apakah schedule aktif
            if (!blockSchedule.isActive) {
                return@any false // Tidak diblokir jika schedule tidak aktif
            }

            val listType = object : TypeToken<List<Boolean>>() {}.type
            val blockedDays: List<Boolean> = Gson().fromJson(blockSchedule.daysOfWeek, listType)

            // Cek apakah aplikasi harus diblokir hari ini
            val isBlockedToday = blockedDays.getOrNull(todayIndex) ?: false
            Log.d("BLOCK_TEST", "Today index: $todayIndex")
            Log.d("BLOCK_TEST", "Blocked days raw: ${blockSchedule.daysOfWeek}")
            Log.d("BLOCK_TEST", "Blocked days: $blockedDays")
            Log.d("BLOCK_TEST", "Is blocked today: $isBlockedToday")

            if (!isBlockedToday) return@any false

            val startTime = LocalTime.parse(blockSchedule.startTime)
            val endTime = LocalTime.parse(blockSchedule.endTime)

            // Cek apakah waktu saat ini ada dalam rentang waktu blokir
            isWithinBlockedTime(currentTime, startTime, endTime)
        }
    }

    // Fungsi untuk memeriksa apakah waktu saat ini berada dalam rentang blokir
    private fun isWithinBlockedTime(currentTime: LocalTime, startTime: LocalTime, endTime: LocalTime): Boolean {
        return if (startTime.isBefore(endTime)) {
            currentTime.isAfter(startTime) && currentTime.isBefore(endTime)
        } else {
            currentTime.isAfter(startTime) || currentTime.isBefore(endTime)
        }
    }

    fun getAppInfoFromPackageNames(context: Context, packageNames: List<String>): List<Pair<String, String>> {
        val packageManager: PackageManager = context.packageManager
        val appInfoList = mutableListOf<Pair<String, String>>()

        for (packageName in packageNames) {
            try {
                val appName = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                ).toString()
                appInfoList.add(Pair(appName, packageName))
            } catch (e: PackageManager.NameNotFoundException) {
                appInfoList.add(Pair("Unknown", packageName))
            }
        }

        return appInfoList
    }

}
