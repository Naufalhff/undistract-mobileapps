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
                        if (viewModel.stillHas60Second(packageName) && !hasShownToast) {
                            withContext(Dispatchers.Main) {
                                showToast("$packageName: Waktu tersisa kurang dari 1 menit!")
                                hasShownToast = true
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



    fun stopTimer(packageName: String, viewModel: VariableSessionViewModel) {
        stopwatchRunnable?.let {
            handler.removeCallbacks(it) // Hentikan callback jika masih berjalan
            stopwatchRunnable = null // Pastikan stopwatchRunnable tidak digunakan lagi
        }

        Log.d("UsageTracker", "$packageName digunakan selama $elapsedSeconds detik")

        // Hitung sisa waktu yang belum dikurangi ke database
        val remainingTime = elapsedSeconds % 30
        if (remainingTime > 0) {
            viewModel.subtractSecondsLeft(packageName, remainingTime.toInt())
            Log.d("UsageTracker", "Mengurangi sisa waktu $remainingTime detik dari limit aplikasi.")
        }

        // Reset elapsedSeconds agar timer siap untuk digunakan lagi
        elapsedSeconds = 0L

        Log.d("UsageTracker", "Timer untuk $packageName telah dihentikan.")
    }

    suspend fun checkAndBlockApp(packageName: String) {
        val session = dao.getVariableSession(packageName).firstOrNull()
        session?.let {
            if (session.secondsLeft <= 0) {
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

    private fun blockApp() {
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

    @Composable
    fun ShowDialog(){
        var isOn by remember { mutableStateOf("Off") }
        var hours by remember { mutableStateOf("") }
        var minutes by remember { mutableStateOf("") }
        var showDialog by remember { mutableStateOf(false) }

        // Konteks untuk dialog
        val context = LocalContext.current
        if (showDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Dialog(
                    onDismissRequest = { showDialog = false },
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        color = Color(0xFFFAF9F9)  // Background color applied directly to Surface
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Cool Down Period",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                OutlinedTextField(
                                    value = hours,
                                    onValueChange = { newValue ->
                                        hours = newValue.toIntOrNull()?.toString() ?: ""
                                    },
                                    label = { Text("Hours") },
                                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = minutes,
                                    onValueChange = { newValue ->
                                        minutes = newValue.toIntOrNull()?.toString() ?: ""
                                    },
                                    label = { Text("Minutes") },
                                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = { showDialog = false }) {
                                    Text(text = "Cancel")
                                }
                                TextButton(onClick = {
                                    showDialog = false
                                    isOn = if (minutes.isNotEmpty() && minutes != "0" || hours.isNotEmpty() && hours != "0") "On" else "Off"
                                }) {
                                    Text(text = "OK")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
