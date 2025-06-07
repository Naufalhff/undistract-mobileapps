package com.example.undistract.features.setadaily_limit.presentation

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.undistract.features.add_limit.data.local.DailyLimitData
import com.example.undistract.features.add_limit.presentation.AddLimitViewModel
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel
import com.example.undistract.ui.theme.Purple40

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun SetDailyUsageLimitScreen(
    selectAppsViewModel: SelectAppsViewModel,
    sharedViewModel: AddLimitViewModel,
    isParental: Boolean,
) {
    var selectedHours by remember { mutableStateOf("0") }
    var selectedMinutes by remember { mutableStateOf("5") }
    var disruptionExpanded by remember { mutableStateOf(false) }
    var selectedDisruptionOption by remember { mutableStateOf("Head Notification") }
    val disruptionOptions = listOf("Head Notification", "Pop Up Notification", "Block Application")

    // Time options lists
    val hoursOptions = remember { (0..23).map { "$it hrs" } }
    val minutesOptions = remember { (0..59).map { "$it mins" } }

    // Update selected notification type in ViewModel
    LaunchedEffect(selectedDisruptionOption) {
        Log.d("SetDailyLimit", "Selected notification type changed to: $selectedDisruptionOption")
        selectAppsViewModel.updateSelectedNotificationType(selectedDisruptionOption)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "What's your daily usage limit for the total usage time across the selected item",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable time selection
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // Hours selector card - narrower width
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.LightGray),
                    modifier = Modifier
                        .width(100.dp) // Narrower width
                ) {
                    CompactTimePicker(
                        options = hoursOptions,
                        selectedValue = "$selectedHours hrs",
                        onValueSelected = { value ->
                            selectedHours = value.split(" ")[0]
                        },
                        highlightedColor = Color(0xFF4B4BE7)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Minutes selector card - narrower width
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.LightGray),
                    modifier = Modifier
                        .width(100.dp) // Narrower width
                ) {
                    CompactTimePicker(
                        options = minutesOptions,
                        selectedValue = "$selectedMinutes mins",
                        onValueSelected = { value ->
                            selectedMinutes = value.split(" ")[0]
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Disruption Options",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Disruption Options dropdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(
                        width = 1.dp,
                        color = Color.LightGray,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .background(Color.White)
                    .clickable { disruptionExpanded = true }
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedDisruptionOption,
                        fontSize = 14.sp
                    )

                    Icon(
                        imageVector = if (disruptionExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        modifier = Modifier.size(24.dp)
                    )
                }

                DropdownMenu(
                    expanded = disruptionExpanded,
                    onDismissRequest = { disruptionExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    disruptionOptions.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                selectedDisruptionOption = option
                                disruptionExpanded = false
                                selectAppsViewModel.updateSelectedNotificationType(option)
                            }
                        )
                    }
                }
            }

            LaunchedEffect(
                selectedHours,
                selectedMinutes,
                selectAppsViewModel.getSelectedItems(),
                isParental,
                selectAppsViewModel.selectedNotificationType.value
            ) {
                val selectedAppsInfo = selectAppsViewModel.getSelectedItems()

                val dailyLimitData = DailyLimitData(
                    selectedHours = selectedHours.toIntOrNull() ?: 0,
                    selectedMinutes = selectedMinutes.toIntOrNull() ?: 0,
                    selectedApps = selectedAppsInfo,
                    isParental = isParental,
                    notificationType = selectAppsViewModel.selectedNotificationType.value
                )

                sharedViewModel.updateDailyLimitData(dailyLimitData)

                Log.d(
                    "SetDailyLimit",
                    "Updated AddLimitViewModel - Apps: ${selectedAppsInfo.size}, Time: ${dailyLimitData.timeLimitMinutes}min, Valid: ${dailyLimitData.isValid()}"
                )
            }
        }
    }
}

@Composable
fun CompactTimePicker(
    options: List<String>,
    selectedValue: String,
    onValueSelected: (String) -> Unit,
    highlightedColor: Color = Purple40
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .height(140.dp)
            .verticalScroll(scrollState)
    ) {
        options.forEach { option ->
            val isSelected = option == selectedValue

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onValueSelected(option) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    fontSize = 14.sp,
                    color = if (isSelected) highlightedColor else Color.Black,
                    fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }

            if (option != options.last()) {
                Divider(
                    color = Color.LightGray,
                    thickness = 0.5.dp
                )
            }
        }
    }
}