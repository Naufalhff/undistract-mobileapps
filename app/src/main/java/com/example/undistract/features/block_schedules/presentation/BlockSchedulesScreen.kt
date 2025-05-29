package com.example.undistract.features.block_schedules.presentation

import android.app.TimePickerDialog
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.add_limit.data.local.ScheduleData
import com.example.undistract.features.add_limit.presentation.AddLimitViewModel
import com.example.undistract.features.block_schedules.data.BlockSchedulesRepository
import com.example.undistract.features.block_schedules.data.BlockSchedulesViewModelFactory
import com.example.undistract.features.block_schedules.domain.BlockScheduleManager
import com.example.undistract.features.get_app_data.domain.AppOrUrlItem
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel
import com.example.undistract.ui.components.BackButton
import com.example.undistract.ui.theme.ColorNew
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@Composable
fun BlockSchedulesScreen(
    navController: NavController,
    isParental: Boolean,
    sharedViewModel: AddLimitViewModel,
    repository: BlockSchedulesRepository,
    selectAppViewModel: SelectAppsViewModel
) {
    val context = LocalContext.current
    val viewModel: BlockSchedulesViewModel = viewModel(
        factory = BlockSchedulesViewModelFactory(repository, isParental)
    )
    // Mengambil selected apps
    val selectedApps = selectAppViewModel.getSelectedIdentifiers()
    val database = AppDatabase.getDatabase(context)
    val blockSchedulesDao = database.blockSchedulesDao()
    val blockScheduleManager = BlockScheduleManager(context, blockSchedulesDao)

    // State untuk hari yang dipilih
    val days = listOf("S", "M", "T", "W", "T", "F", "S")
    var selectedDays = remember { mutableStateOf(MutableList(7) { false }) }
    val allSelected by remember { derivedStateOf { selectedDays.value.all { it } } }
    val allNotSelected by remember { derivedStateOf { selectedDays.value.none { it } } }


    // State untuk jam
    var startTime by remember { mutableStateOf(LocalTime.of(0, 0)) }
    var endTime by remember { mutableStateOf(LocalTime.of(0, 0)) }
    var isAllDay by remember { mutableStateOf(false) }
    var listApps by remember { mutableStateOf<List<AppOrUrlItem>>(emptyList()) }

    val combinedItems by selectAppViewModel.combinedItems.collectAsState()

    LaunchedEffect(selectedApps, combinedItems) {
        listApps = combinedItems.filter { item ->
            selectedApps.contains(item.identifier)
        }
    }

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

        LaunchedEffect(selectedDays, isAllDay, startTime, endTime, isParental) {
            sharedViewModel.updateScheduleData(
                ScheduleData(
                    daysOfWeek = selectedDays.value.toList().toString(),
                    isAllDay = isAllDay,
                    startTime = startTime.toString(),
                    endTime = endTime.toString(),
                    isActive = true,
                    isParental = isParental
                )
            )
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