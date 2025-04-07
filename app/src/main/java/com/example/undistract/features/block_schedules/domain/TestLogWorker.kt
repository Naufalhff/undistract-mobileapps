package com.example.undistract.features.block_schedules.domain

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

fun scheduleLogWorker(context: Context) {
    val workRequest = OneTimeWorkRequestBuilder<LogWorker>()
        .setInitialDelay(5, TimeUnit.SECONDS) // Delay awal 5 detik
        .setConstraints(
            Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresCharging(false)
                .build()
        )
        .build()

    WorkManager.getInstance(context).enqueue(workRequest)
}


class LogWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val now = LocalDateTime.now()
        val currentDay = now.dayOfWeek
        val currentTime = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES)

        val scheduledDays = listOf(DayOfWeek.SATURDAY)
        val startTime = LocalTime.of(12, 0)
        val endTime = LocalTime.of(16, 0)

        if (currentDay in scheduledDays && currentTime in startTime..endTime) {
            Log.d("ScheduleLog", "Melakukan log pada $currentDay jam $currentTime (Dalam rentang waktu)")
        } else {
            Log.d("ScheduleLog", "Tidak sesuai jadwal, sekarang: $currentDay jam $currentTime")
        }

        scheduleLogWorker(applicationContext)

        return Result.success()
    }
}


