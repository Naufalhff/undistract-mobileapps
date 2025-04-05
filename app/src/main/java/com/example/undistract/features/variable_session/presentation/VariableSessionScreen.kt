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
import com.example.undistract.features.get_installed_apps.domain.AppInfo
import com.example.undistract.ui.theme.ColorNew
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.navigation.NavController
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.block_schedules.domain.BlockScheduleManager
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel
import com.example.undistract.features.variable_session.domain.VariableSessionManager
import com.example.undistract.ui.components.BackButton

@Composable
fun VariableSessionScreen(navController: NavController, viewModel: VariableSessionViewModel, selectAppViewModel: SelectAppsViewModel) {
    val context = LocalContext.current

    var showDialog by remember { mutableStateOf(false) }
    var isOn by remember { mutableStateOf("Off") }
    var coolDownHours by remember { mutableStateOf("") }
    var coolDownMinutes by remember { mutableStateOf("") }

    // Mengambil selected apps
    selectAppViewModel.updateCurrentRoute("variable_session")
    val selectedApps = selectAppViewModel.getSelectedApps()
    val database = AppDatabase.getDatabase(context)
    val variableSessionDao = database.variableSessionDao()
    val variableSessionManager = VariableSessionManager(context, variableSessionDao)
    val listApps = variableSessionManager.getAppInfoFromPackageNames(context, selectedApps)

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
                    color = Color(0xFFFAF9F9)
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
                                value = coolDownHours,
                                onValueChange = { newValue ->
                                    coolDownHours = newValue.toIntOrNull()?.toString() ?: ""
                                },
                                label = { Text("Hours") },
                                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = coolDownMinutes,
                                onValueChange = { newValue ->
                                    coolDownMinutes = newValue.toIntOrNull()?.toString() ?: ""
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
                                isOn = if (coolDownMinutes.isNotEmpty() && coolDownMinutes != "0" || coolDownHours.isNotEmpty() && coolDownHours != "0") "On" else "Off"
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

        Row (
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton (
                modifier = Modifier.size(24.dp),
                onClick = { navController.popBackStack()}
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Custom session restriction",
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            GetAppInfo(context, selectedApps)
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

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            val coroutineScope = rememberCoroutineScope()

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = ColorNew.primary
                    ),
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Text("Cancel")
                }

                Button(
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorNew.primary,
                        contentColor = Color.White
                    ),
                    onClick = {
                        coroutineScope.launch {
                            try {
                                viewModel.addVariableSession(
                                    apps = listApps,
                                    secondsLeft = 0,
                                    coolDownDuration = calculate(coolDownMinutes, coolDownHours).toLong(),
                                    coolDownEndTime = null,
                                    isOnCooldown = false,
                                    isActive = true
                                )
                                navController.popBackStack()
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
}

@Composable
fun VariableLimitDialog (navController: NavController, viewModel: VariableSessionViewModel, packageName: String){

    var showDialog by remember { mutableStateOf(false) }
    var hours by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Dialog(
            onDismissRequest = { },
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = Color(0xFFFAF9F9)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Session Limit",
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
                        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                        TextButton(onClick = {
                            viewModel.updateIsActive(
                                packageName,
                                false
                            )
                            context.startActivity(launchIntent)
                        }) {
                            Text(text = "Don't Limit")
                        }
                        TextButton(onClick = {
                            coroutineScope.launch {
                                try {
                                    viewModel.updateSecondsLeft(
                                        packageName,
                                        calculate(minutes, hours)
                                    )
                                    context.startActivity(launchIntent)
                                    Toast.makeText(context, "Save Success!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    navController.popBackStack()
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
}

fun calculate(minutes: String, hours: String): Int {
    val min = minutes.toIntOrNull() ?: 0
    val hr = hours.toIntOrNull() ?: 0
    return (min + (hr * 60)) * 60
}

@Composable
fun GetAppInfo(context: Context, packageName: List<String>) {
    val packageManager = context.packageManager

    val app = try {
        packageManager.getApplicationInfo(packageName.firstOrNull()?: "", PackageManager.GET_META_DATA)
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }

    if (app != null) {
        val appInfo = AppInfo(
            name = packageManager.getApplicationLabel(app).toString(),
            packageName = app.packageName,
            icon = app.loadIcon(packageManager)
        )

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
                Image(
                    painter = rememberAsyncImagePainter(appInfo.icon),
                    contentDescription = appInfo.name,
                    modifier = Modifier.size(36.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                val displayText = when {
                    packageName.isEmpty() -> "No apps selected"
                    packageName.size == 1 -> appInfo.name
                    else -> "${appInfo.name}, and ${packageName.size - 1} more"
                }

                Text(
                    text = displayText,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    } else {
        Text("No app selected", style = MaterialTheme.typography.bodyLarge)
    }
}