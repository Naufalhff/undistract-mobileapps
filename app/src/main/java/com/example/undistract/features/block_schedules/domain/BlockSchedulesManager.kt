package com.example.undistract.features.block_schedules.domain

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import com.example.undistract.core.AppAccessibilityService
import com.example.undistract.features.block_schedules.data.local.BlockSchedulesDao
import java.time.LocalDate
import java.time.LocalTime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

// Kelas untuk mengatur logika blokir aplikasi berdasarkan waktu
class BlockScheduleManager(private val context: Context, private val dao: BlockSchedulesDao) {

    // Fungsi untuk memblokir aplikasi
    fun blockApp() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(homeIntent)
    }

    // Fungsi untuk memeriksa apakah aplikasi diblokir dan dalam rentang waktu tertentu
    suspend fun shouldBlockApp(packageName: String, currentTime: LocalTime): Boolean {
        // Ambil semua jadwal blokir untuk aplikasi tersebut
        val blockSchedules = dao.getBlockSchedules(packageName)
        Log.d("DATABASE_TEST", "blockschedules: $blockSchedules")

        // Ambil hari saat ini dalam bentuk indeks (0 = Senin, 6 = Minggu)
        val todayIndex = LocalDate.now().dayOfWeek.value % 7 // Ubah agar 0 = Senin, 6 = Minggu

        return blockSchedules.any { blockSchedule ->
            // Konversi dari JSON String ke List<Boolean>
            val listType = object : TypeToken<List<Boolean>>() {}.type
            val blockedDays: List<Boolean> = Gson().fromJson(blockSchedule.daysOfWeek, listType)

            // Cek apakah aplikasi harus diblokir hari ini
            val isBlockedToday = blockedDays.getOrNull(todayIndex) ?: false
            Log.d("BLOCK_TEST", "Today index: $todayIndex")
            Log.d("BLOCK_TEST", "Blocked days raw: ${blockSchedule.daysOfWeek}")
            Log.d("BLOCK_TEST", "Blocked days: $blockedDays")
            Log.d("BLOCK_TEST", "Is blocked today: $isBlockedToday")

            if (!isBlockedToday) {
                return@any false // Tidak diblokir hari ini
            }

            // Parse waktu mulai dan berakhir
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
        } else { // Kasus rentang waktu melewati tengah malam
            currentTime.isAfter(startTime) || currentTime.isBefore(endTime)
        }
    }

    fun getAppInfoFromPackageNames(context: Context, packageNames: List<String>): List<Pair<String, String>> {
        val packageManager: PackageManager = context.packageManager
        val appInfoList = mutableListOf<Pair<String, String>>()

        for (packageName in packageNames) {
            try {
                // Mendapatkan nama aplikasi berdasarkan package name
                val appName = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                ).toString()
                // Menambahkan pasangan nama aplikasi dan package name
                appInfoList.add(Pair(appName, packageName))
            } catch (e: PackageManager.NameNotFoundException) {
                // Jika package tidak ditemukan, bisa menangani error di sini
                appInfoList.add(Pair("Unknown", packageName))
            }
        }

        return appInfoList
    }

}
