package com.example.undistract.features.block_schedules.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.undistract.R
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.block_schedules.data.local.BlockSchedulesDao
import com.example.undistract.features.get_installed_apps.domain.AppInfo
import com.example.undistract.features.block_schedules.presentation.BlockSchedulesViewModel
import com.example.undistract.features.block_schedules.domain.BlockScheduleManager
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel
import com.example.undistract.ui.components.BackButton
import com.example.undistract.ui.navigation.BottomNavItem
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.example.undistract.ui.theme.ColorNew
import kotlinx.coroutines.launch


@Composable
fun BlockSchedulesScreen(navController: NavController, viewModel: BlockSchedulesViewModel, selectAppViewModel: SelectAppsViewModel) {
    val context = LocalContext.current
    // Mengambil selected apps
    selectAppViewModel.updateCurrentRoute("block_schedules")
    val selectedApps = selectAppViewModel.getSelectedApps()
    val database = AppDatabase.getDatabase(context)
    val blockSchedulesDao = database.blockSchedulesDao()
    val blockScheduleManager = BlockScheduleManager(context, blockSchedulesDao)
    val listApps = blockScheduleManager.getAppInfoFromPackageNames(context, selectedApps)

    // State untuk hari yang dipilih
    val days = listOf("S", "M", "T", "W", "T", "F", "S")
    var selectedDays = remember { mutableStateOf(MutableList(7) { false }) }
    val allSelected by remember { derivedStateOf { selectedDays.value.all { it } } }
    val allNotSelected by remember { derivedStateOf { selectedDays.value.none { it } } }


    // State untuk jam
    var startTime by remember { mutableStateOf(LocalTime.of(0, 0)) }
    var endTime by remember { mutableStateOf(LocalTime.of(0, 0)) }
    var isAllDay by remember { mutableStateOf(false) }

    // Formatter untuk menampilkan waktu
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

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
                text = "Block on Schedules",
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text ("When do you want to block the selected apps?", style = MaterialTheme.typography.bodyMedium)
        }

        // Pemilihan Hari
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, ColorNew.primary, RoundedCornerShape(16.dp))
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEachIndexed { index, day ->
                    DaySelector(
                        day = day,
                        isSelected = selectedDays.value[index],
                        isAllDay = isAllDay,
                        onSelect = {
                            if (!isAllDay){
                                val updatedSelection = selectedDays.value.toMutableList()
                                updatedSelection[index] = !updatedSelection[index]
                                selectedDays.value = updatedSelection
                            }
                        }
                    )
                }
            }
        }

        // Pemilihan All Day
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("All day", style = MaterialTheme.typography.bodyLarge)
            if (allSelected) {
                isAllDay = true
            }
            Switch(
                checked = isAllDay,
                onCheckedChange = {
                    isAllDay = it
                    selectedDays.value = MutableList(selectedDays.value.size) { isAllDay }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White, // Warna thumb saat switch on
                    checkedTrackColor = ColorNew.primary, // Warna track saat switch on
                    uncheckedThumbColor = Color.Gray, // Warna thumb saat switch off
                    uncheckedTrackColor = Color.White // Warna track saat switch off
                )
            )
        }

        // Pemilihan Waktu
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .clickable {
                        val timePickerDialog = TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                startTime = LocalTime.of(hourOfDay, minute)
                            },
                            0,
                            0,
                            true
                        )
                        timePickerDialog.show()
                    }
            ) {
                Text("Start Time", style = MaterialTheme.typography.bodyMedium)
                Text(
                    startTime.format(timeFormatter),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowForward,
                contentDescription = "Arrow",
                modifier = Modifier.size(24.dp),
                tint = Color.Gray
            )

            // End Time
            Column(
                modifier = Modifier
                    .clickable {
                        val timePickerDialog = TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                endTime = LocalTime.of(hourOfDay, minute)
                            },
                            0,
                            0,
                            true
                        )
                        timePickerDialog.show()
                    }
            ) {
                Text("End Time", style = MaterialTheme.typography.bodyMedium)
                Text(
                    endTime.format(timeFormatter),
                    style = MaterialTheme.typography.titleMedium
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
                        if (allNotSelected){
                            Toast.makeText(context, "Please select at least one day", Toast.LENGTH_SHORT).show()
                        } else {
                            coroutineScope.launch {
                                try {
                                    val selectedDaysList = selectedDays.value.toList()
                                    viewModel.addBlockSchedules(
                                        apps = listApps,
                                        daysOfWeek = selectedDaysList.toString(),
                                        isAllDay = isAllDay,
                                        startTime = startTime.toString(),
                                        endTime = endTime.toString(),
                                        isActive = true
                                    )
                                    navController.popBackStack()
                                    Toast.makeText(context, "Save Success!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Save Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    Log.e("SAVE_ERROR", "Failed to save block schedule", e)
                                }
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
fun DaySelector(
    day: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    isAllDay: Boolean
) {
    if(!isAllDay){
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isSelected) ColorNew.primary else Color.Transparent, shape = CircleShape)
                .border(1.dp, ColorNew.primary, CircleShape)
                .clickable(onClick = onSelect),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day,
                color = if (isSelected) Color.White else ColorNew.primary,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(ColorNew.primary, shape = CircleShape)
                .border(1.dp, ColorNew.primary, CircleShape)
                .clickable(onClick = onSelect),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = day,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }

}

@Composable
fun GetAppInfo(context: Context, packageName: List<String>) {
    val packageManager = context.packageManager

    // Coba ambil info aplikasi
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