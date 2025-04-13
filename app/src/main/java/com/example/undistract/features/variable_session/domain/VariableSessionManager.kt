package com.example.undistract.features.variable_session.domain

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.undistract.core.AppAccessibilityService
import com.example.undistract.features.variable_session.data.local.VariableSessionDao
import com.example.undistract.features.variable_session.data.local.VariableSessionEntity
import com.example.undistract.features.variable_session.presentation.VariableSessionViewModel
import java.time.LocalDate
import java.time.LocalTime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VariableSessionManager(private val context: Context, private val dao: VariableSessionDao) {

    private var startTime: Long = 0L
    private var elapsedSeconds = 0L
    private val handler = Handler(Looper.getMainLooper())
    private var stopwatchRunnable: Runnable? = null
    private var hasShownToast = false

    suspend fun askLimit(packageName: String): Boolean {
        val session = dao.getVariableSession(packageName).firstOrNull()
        return session?.let {
            it.secondsLeft <= 0 && it.isActive
        } ?: false
    }

    suspend fun isLimitedApp(packageName: String): Boolean {
        val session = dao.getVariableSession(packageName).firstOrNull()
        return session?.let {
            it.secondsLeft > 0 && it.isActive
        } ?: false
    }

    fun startTimer(packageName: String, viewModel: VariableSessionViewModel) {
        startTime = System.currentTimeMillis()
        elapsedSeconds = 0L


        stopwatchRunnable = object : Runnable {
            override fun run() {
                elapsedSeconds++

                // Setiap 15 detik, lakukan pengurangan waktu dan pengecekan
                if (elapsedSeconds % 15 == 0L) {
                    CoroutineScope(Dispatchers.IO).launch {
                        // Pengurangan waktu, pastikan selesai sebelum lanjut
                        viewModel.subtractSecondsLeft(packageName, 15)

                        // Cek apakah sisa waktu kurang dari 60 detik
                        val session = dao.getVariableSession(packageName).firstOrNull()
                        session?.let{
                            if (viewModel.aMinuteLeft(packageName) && !hasShownToast) {
                                withContext(Dispatchers.Main) {
                                    showToast("This session for ${session.appName} is less than 1 minute left!")
                                    hasShownToast = true
                                }
                            }
                        }
                        Log.d("UsageTracker", "$packageName digunakan selama $elapsedSeconds detik")

                        delay(500)
                        checkAndBlockApp(packageName)
                    }
                }
                handler.postDelayed(this, 1000)
            }
        }

        hasShownToast = false
        handler.post(stopwatchRunnable!!)
    }

    suspend fun stopTimer(packageName: String, viewModel: VariableSessionViewModel) {
        stopwatchRunnable?.let {
            handler.removeCallbacks(it)
            stopwatchRunnable = null
        }

        Log.d("UsageTracker", "$packageName digunakan selama $elapsedSeconds detik")

        val remainingTime = elapsedSeconds % 15
        if (remainingTime > 0) {
            dao.subtractSecondsLeft(packageName, remainingTime.toInt())
            val session = dao.getVariableSession(packageName).firstOrNull()
            session?.let {
                if (session.secondsLeft <= 0) {
                    if (session.coolDownDuration != null && session.coolDownDuration > 0) {
                        val coolDownDuration = session.coolDownDuration * 1000
                        val coolDownEndTime = System.currentTimeMillis() + coolDownDuration

                        dao.updateCoolDownEndTime(packageName, coolDownEndTime)
                        dao.updateIsOnCoolDown(packageName, true)
                        Log.d("CD DURATION", "OK")
                    }
                }
            }
            Log.d("UsageTracker", "Mengurangi sisa waktu $remainingTime detik dari limit aplikasi.")
        }

        elapsedSeconds = 0L

        Log.d("UsageTracker", "Timer untuk $packageName telah dihentikan.")
    }

    suspend fun checkAndBlockApp(packageName: String) {
        val session = dao.getVariableSession(packageName).firstOrNull()
        val currentTime = System.currentTimeMillis()
        session?.let {
            if (session.secondsLeft <= 0) {
                if (session.coolDownDuration != null && session.coolDownDuration > 0) {
                    val coolDownDuration = session.coolDownDuration * 1000
                    val coolDownEndTime = System.currentTimeMillis() + coolDownDuration

                    dao.updateCoolDownEndTime(packageName, coolDownEndTime)
                    dao.updateIsOnCoolDown(packageName, true)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Session timed out, app blocked", Toast.LENGTH_SHORT).show()
                }
                dao.updateSecondsLeft(session.packageName, 0)
                blockApp()
            } else {
                Log.d("SessionInfo", "Sisa waktu: ${session.secondsLeft} detik")
            }
        }
    }

    suspend fun canStartNewSession(packageName: String): Boolean {
        val session = dao.getVariableSession(packageName).firstOrNull()
        val currentTime = System.currentTimeMillis()

        return session?.let {
            if (it.isOnCoolDown && it.coolDownEndTime != null && currentTime >= it.coolDownEndTime) {
                dao.updateIsOnCoolDown(packageName, false)
                return@let true
            }
            !it.isOnCoolDown
        } ?: true
    }

    fun blockApp() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(homeIntent)
    }

    fun showToast(message: String) {
        handler.post {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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
                // Menambahkan pasangan nama aplikasi dan package name
                appInfoList.add(Pair(appName, packageName))
            } catch (e: PackageManager.NameNotFoundException) {
                appInfoList.add(Pair("Unknown", packageName))
            }
        }
        return appInfoList
    }
}
