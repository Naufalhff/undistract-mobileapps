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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.undistract.R
import com.example.undistract.features.get_app_data.domain.AppOrUrlItem
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel
import com.example.undistract.features.variable_session.data.VariableSessionRepository
import com.example.undistract.features.variable_session.domain.VariableSessionManager
import com.example.undistract.ui.components.BackButton
import com.example.undistract.ui.navigation.BottomNavItem
import com.example.undistract.ui.theme.ColorNew
import kotlinx.coroutines.launch

@Composable
fun VariableSessionScreen(
    navController: NavController,
    repository: VariableSessionRepository,
    selectAppViewModel: SelectAppsViewModel,
    isParental: Boolean
) {
    val context = LocalContext.current
    val viewModel: VariableSessionViewModel = viewModel(
        factory = VariableSessionViewModelFactory(repository, isParental)
    )

    var showDialog by remember { mutableStateOf(false) }
    var isOn by remember { mutableStateOf("Off") }
    var coolDownHours by remember { mutableStateOf("") }
    var coolDownMinutes by remember { mutableStateOf("") }

    // Mengambil selected apps
    val selectedApps = selectAppViewModel.getSelectedIdentifiers()
    var listApps by remember { mutableStateOf<List<AppOrUrlItem>>(emptyList()) }

    val combinedItems by selectAppViewModel.combinedItems.collectAsState()

    LaunchedEffect(selectedApps, combinedItems) {
        listApps = combinedItems.filter { item ->
            selectedApps.contains(item.identifier)
        }
    }

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
                                if (selectedApps.isEmpty()) {
                                    Toast.makeText(context, "Please select at least one app", Toast.LENGTH_SHORT).show()
                                } else {
                                    val appsToSave = listApps.map { app -> app.name to app.identifier }
                                    viewModel.addVariableSession(
                                        apps = appsToSave,
                                        secondsLeft = 0,
                                        coolDownDuration = calculate(coolDownMinutes, coolDownHours).toLong(),
                                        coolDownEndTime = null,
                                        isOnCooldown = false,
                                        isActive = true,
                                        isParental = isParental
                                    )
                                    if (isParental){
                                        navController.navigate("parental_usage_limit?isParental=true"){
                                            launchSingleTop = true
                                        }
                                    } else {
                                        navController.navigate(BottomNavItem.UsageLimit.route){
                                            launchSingleTop = true
                                        }
                                    }
                                    Toast.makeText(context, "Save Success!", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Save Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                Log.e("SAVE_ERROR", "Failed to save variable session", e)
                                navController.navigate("parental_usage_limit?isParental=$isParental")
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
                                    val minutesValue = minutes.trim()
                                    val hoursValue = hours.trim()

                                    val isMinutesEmpty = minutesValue.isEmpty() || minutesValue == "0"
                                    val isHoursEmpty = hoursValue.isEmpty() || hoursValue == "0"

                                    if (isMinutesEmpty && isHoursEmpty) {
                                        Toast.makeText(context, "Please insert minutes and/or hours to limit the session", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.updateSecondsLeft(packageName, calculate(minutesValue, hoursValue))
                                        context.startActivity(launchIntent)
                                        Toast.makeText(context, "Save Success!", Toast.LENGTH_SHORT).show()
                                    }

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
