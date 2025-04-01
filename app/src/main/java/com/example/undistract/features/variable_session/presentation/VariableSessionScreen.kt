package com.example.undistract.features.variable_session.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import coil.compose.rememberAsyncImagePainter
import com.example.undistract.R
import com.example.undistract.features.select_apps.presentation.AppInfo
import com.example.undistract.ui.theme.ColorNew
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner


@Composable
fun VariableLimitDialog (viewModel: VariableSessionViewModel){

    var showDialog by remember { mutableStateOf(false) }
    var hours by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var isSet by remember { mutableStateOf(false) }
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    if (!isSet) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Dialog(
                onDismissRequest = { isSet = true },
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
                            TextButton(onClick = {
                                isSet = true
                                backDispatcher?.onBackPressed() // Simulasi tombol Back
                            }) {
                                Text(text = "Cancel")
                            }
                            TextButton(onClick = {
                                isSet = true
                                backDispatcher?.onBackPressed()
                                coroutineScope.launch {
                                    try {
                                        viewModel.addVariableSession(
                                            apps = listOf(Pair("Youtube", "com.google.android.youtube")),
                                            minutesLeft = calculate(minutes, hours),
                                            isActive = true
                                        )
                                        Toast.makeText(context, "Save Success!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Save Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                        Log.e("SAVE_ERROR", "Failed to save variable session", e)
                                    }
                                }

                            }) {
                                Text(text = "OK")
                            }
                        }
                    }
                }
            }
        }
    } else {
        backDispatcher?.onBackPressed()
    }
}

@Composable
fun VariableSessionScreen(viewModel: VariableSessionViewModel) {

    var showDialog by remember { mutableStateOf(false) }
    var isOn by remember { mutableStateOf("Off") }
    var hours by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ShowYouTubeApp(context)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, ColorNew.primary, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color(225,225,225))
                .padding(12.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painterResource(R.drawable.info_icon),
                contentDescription = "Information",
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "When opening a selected app, Undistract will prompt you to enter how long you want to spend within the app before being blocked.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, ColorNew.primary, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    showDialog = true
                }
                .padding(12.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.timer_icon),
                contentDescription = "Timer",
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Cool down period: $isOn",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Prevent you from starting a new session in distracting apps until the cooldown period has ended.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Column (
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val coroutineScope = rememberCoroutineScope()
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    coroutineScope.launch {
                        try {
                            viewModel.addVariableSession(
                                apps = listOf(Pair("Youtube", "com.google.android.youtube"), Pair("WhatsApp", "com.android.whatsapp")),
                                minutesLeft = calculate(minutes, hours),
                                isActive = true
                            )
                            Toast.makeText(context, "Save Success!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Save Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                            Log.e("SAVE_ERROR", "Failed to save variable session", e)
                        }
                    }
                }
            ) {
                Text("Save")
            }
        }
    }
}

fun calculate(minutes: String, hours: String): Int {
    val min = minutes.toIntOrNull() ?: 0
    val hr = hours.toIntOrNull() ?: 0
    return min + (hr * 60)
}


@Composable
fun ShowYouTubeApp(context: Context) {
    val packageManager = context.packageManager
    val youtubePackage = "com.google.android.youtube"

    // Coba ambil info aplikasi YouTube
    val youtubeApp = try {
        packageManager.getApplicationInfo(youtubePackage, PackageManager.GET_META_DATA)
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }

    if (youtubeApp != null) {
        val appInfo = AppInfo(
            name = packageManager.getApplicationLabel(youtubeApp).toString(),
            packageName = youtubeApp.packageName,
            icon = youtubeApp.loadIcon(packageManager)
        )

        // Tampilkan di UI
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Selected Apps:", style = MaterialTheme.typography.labelLarge)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            )  {
                // Menampilkan ikon aplikasi
                Image(
                    painter = rememberAsyncImagePainter(appInfo.icon),
                    contentDescription = appInfo.name,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Nama aplikasi
                Text(
                    text = appInfo.name,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        Text("YouTube tidak ditemukan!", style = MaterialTheme.typography.bodyLarge)
    }
}