package com.example.undistract.features.variable_session.domain

import android.content.Context
import android.content.Intent
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
import java.time.LocalDate
import java.time.LocalTime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class VariableSessionManager(private val context: Context, private val dao: VariableSessionDao) {

    suspend fun askLimit(packageName: String): Boolean {
        val session = dao.getVariableSession(packageName)?.firstOrNull()

        return session?.let {
            it.minutesLeft <= 0 && it.isActive
        } ?: false
    }


    suspend fun isLimitedApp(packageName: String): Boolean {
        val session = dao.getVariableSession(packageName)?.firstOrNull() // Ambil item pertama jika ada
        return session != null
    }

    suspend fun reduceMinutesLeft(packageName: String) {
        val session = dao.getVariableSession(packageName)?.firstOrNull()
        session?.let {
            if (it.minutesLeft > 0) {
                dao.updateMinutesLeft(packageName, it.minutesLeft - 1)
                Log.d("VariableSessionManager", "Reduced minutesLeft for $packageName to ${it.minutesLeft - 1}")
            }
        } ?: Log.d("VariableSessionManager", "Session not found for $packageName")
    }

    suspend fun updateSessionTime(packageName: String, duration: Int) {
        val session = dao.getVariableSession(packageName)?.firstOrNull()
        session?.let {
            val updatedMinutes = it.minutesLeft + duration
            dao.updateMinutesLeft(packageName, updatedMinutes)
            Log.d("VariableSessionManager", "Updated minutesLeft for $packageName to $updatedMinutes")
        } ?: Log.d("VariableSessionManager", "Session not found for $packageName")
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
