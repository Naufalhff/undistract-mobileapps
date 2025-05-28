package com.example.undistract.features.setadaily_limit.presentation

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.undistract.R
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepositoryImpl
import com.example.undistract.features.setadaily_limit.data.local.SetaDailyLimitEntity
import com.example.undistract.features.usage_limit.presentation.UsageLimitViewModel
import com.example.undistract.ui.navigation.BottomNavItem
import com.example.undistract.ui.theme.Purple40
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetDailyUsageLimitScreen(
    navController: NavHostController,
    selectAppsViewModel: SelectAppsViewModel,
    usageLimitViewModel: UsageLimitViewModel,
    isParental: Boolean
) {

    val context = LocalContext.current
    val setaDailyLimitViewModel: SetaDailyLimitViewModel = viewModel(
        factory = SetaDailyLimitViewModelFactory(
            SetaDailyLimitRepositoryImpl(
                AppDatabase.getDatabase(context).setaDailyLimitDao()
            ),
            isParental
        )
    )

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }

    // Coroutine scope for showing snackbar
    val coroutineScope = rememberCoroutineScope()

    var selectedHours by remember { mutableStateOf("0") }
    var selectedMinutes by remember { mutableStateOf("5") }
    var disruptionExpanded by remember { mutableStateOf(false) }
    var selectedDisruptionOption by remember { mutableStateOf("Head Notification") }
    val disruptionOptions = listOf("Head Notification", "Pop Up Notification", "Block Application")
    val selectedApps by remember { mutableStateOf(selectAppsViewModel.getSelectedItems()) }

    // Time options lists
    val hoursOptions = remember { (0..23).map { "$it hrs" } }
    val minutesOptions = remember { (0..59).map { "$it mins" } }

    // Update selected notification type in ViewModel
    LaunchedEffect(selectedDisruptionOption) {
        selectAppsViewModel.updateSelectedNotificationType(selectedDisruptionOption)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // Top app bar
        TopAppBar(
            title = { Text("Add limit", fontSize = 16.sp) },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "How do you want to limit those apps?",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEAD6FF))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Set a Daily Usage Limit",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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

            Spacer(modifier = Modifier.weight(1f))

            // Bottom buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = { navController.popBackStack() }
                ) {
                    Text(
                        text = "Cancel",
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }

                val saveResultState by setaDailyLimitViewModel.saveResult.collectAsState()

                Button(
                    onClick = {
                        try {
                            Log.d("SetaDailyLimit", "Save button clicked")
                            val timeLimitMinutes = (selectedHours.toInt() * 60) + selectedMinutes.toInt()
                            val selectedAppsInfo = selectAppsViewModel.getSelectedItems()

                            Log.d("SetaDailyLimit", "Selected apps: ${selectedAppsInfo.size}, time limit: $timeLimitMinutes minutes")

                            if (selectedAppsInfo.isNullOrEmpty()) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Please select at least one app")
                                }
                            } else {
                                // Create all entities at once
                                val entities = selectedAppsInfo.map { app ->
                                    val iconString = try {
                                        app.icon.toString()
                                    } catch (e: Exception) {
                                        Log.e("SetaDailyLimit", "Error getting icon, using default", e)
                                        R.drawable.app_logo.toString()
                                    }

                                    Log.d("SetaDailyLimit", "Creating entity with notification type: ${selectAppsViewModel.selectedNotificationType.value}")

                                    SetaDailyLimitEntity(
                                        appName = app.name,
                                        packageName = app.identifier,
                                        icon = iconString,
                                        timeLimitMinutes = timeLimitMinutes,
                                        isParental = isParental,
                                        notificationType = selectAppsViewModel.selectedNotificationType.value
                                    )
                                }

                                // Save all entities in a batch
                                coroutineScope.launch {
                                    try {
                                        setaDailyLimitViewModel.addMultipleDailyLimits(entities) { success ->
                                            if (success) {
                                                coroutineScope.launch {
                                                    try {
                                                        // Update the UsageLimitViewModel
                                                        usageLimitViewModel.refreshLimits()

                                                        // Show success message and navigate
                                                        snackbarHostState.showSnackbar("Limits saved successfully")

                                                        // Use a safer navigation approach
                                                        try {
                                                            if (isParental){
                                                                navController.navigate("parental_usage_limit?isParental=true"){
                                                                    launchSingleTop = true
                                                                }
                                                            } else {
                                                                navController.navigate(BottomNavItem.UsageLimit.route){
                                                                    launchSingleTop = true
                                                                }
                                                            }
                                                        } catch (e: Exception) {
                                                            Log.e("SetaDailyLimit", "Navigation error", e)
                                                            // If navigation fails, at least the data is saved
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e("SetaDailyLimit", "Post-save operation error", e)
                                                    }
                                                }
                                            } else {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("Error saving limits")
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("SetaDailyLimit", "Error in save coroutine", e)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Error: ${e.message ?: "Unknown error"}")
                                        }
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("SetaDailyLimit", "Error saving limits", e)
                            e.printStackTrace()
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Error: ${e.message ?: "Unknown error"}")
                            }
                        }
                    },
                    enabled = saveResultState != SetaDailyLimitViewModel.SaveResult.Loading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Purple40
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    if (saveResultState == SetaDailyLimitViewModel.SaveResult.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Save",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Add SnackbarHost to the UI
        Box(modifier = Modifier.fillMaxSize()) {
            // Place the SnackbarHost at the bottom of the screen
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
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