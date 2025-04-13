package com.example.undistract.features.setadaily_limit.presentation

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel
import com.example.undistract.features.setadaily_limit.data.local.SetaDailyLimitEntity
import com.example.undistract.ui.theme.Purple40
import com.example.undistract.features.usage_limit.presentation.UsageLimitViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepositoryImpl
import com.example.undistract.R
import android.util.Log
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.filled.Close
import kotlinx.coroutines.delay

import com.example.undistract.features.setadaily_limit.presentation.SetaDailyLimitViewModelFactory
import com.example.undistract.ui.navigation.BottomNavItem
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.withStyle
import android.content.Context


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetDailyUsageLimitScreen(
    navController: NavHostController,
    viewModel: SelectAppsViewModel,
    usageLimitViewModel: UsageLimitViewModel
) {
    requireNotNull(navController) { "NavController must not be null" }
    requireNotNull(viewModel) { "ViewModel must not be null" }
    requireNotNull(usageLimitViewModel) { "UsageLimitViewModel must not be null" }

    val context = LocalContext.current
    val setaDailyLimitViewModel: SetaDailyLimitViewModel = viewModel(
        factory = SetaDailyLimitViewModelFactory(
            SetaDailyLimitRepositoryImpl(
                AppDatabase.getDatabase(context).setaDailyLimitDao()
            )
        )
    )

    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }

    // Coroutine scope for showing snackbar
    val coroutineScope = rememberCoroutineScope()

    var selectedHours by remember { mutableStateOf("0") }
    var selectedMinutes by remember { mutableStateOf("5") }
    var limitName by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val limitOptions = remember { listOf("Set a Daily Usage Limit", "Block Permanently", "Block on a Schedule") }
    var selectedLimitOption by remember { mutableStateOf(limitOptions[0]) }
    val selectedApps by remember { mutableStateOf(viewModel.getSelectedAppsInfo()) }
    var showAppsDialog by remember { mutableStateOf(false) }

    // Time options lists
    val hoursOptions = remember { (0..23).map { "$it hrs" } }
    val minutesOptions = remember { (0..55 step 5).map { "$it mins" } }

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
            // App selection display with improved UI, but without play button icon
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEAD6FF))
                    .padding(16.dp)
                    .clickable(enabled = selectedApps.size > 1) {
                        if (selectedApps.isEmpty()) {
                            navController.navigate("select_apps")
                        } else {
                            showAppsDialog = true
                        }
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // App grid showing actual app icons for the first 4 selected apps
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE0B0FF))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(
                                modifier = Modifier.fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Top-left app icon (first app)
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                ) {
                                    if (selectedApps.isNotEmpty()) {
                                        Image(
                                            painter = rememberAsyncImagePainter(selectedApps[0].icon),
                                            contentDescription = selectedApps[0].name,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(0xFFD580FF))
                                        )
                                    }
                                }

                                // Bottom-left app icon (second app)
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                ) {
                                    if (selectedApps.size > 1) {
                                        Image(
                                            painter = rememberAsyncImagePainter(selectedApps[1].icon),
                                            contentDescription = selectedApps[1].name,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(0xFFD580FF))
                                        )
                                    }
                                }
                            }
                            Column(
                                modifier = Modifier.fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Top-right app icon (third app)
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                ) {
                                    if (selectedApps.size > 2) {
                                        Image(
                                            painter = rememberAsyncImagePainter(selectedApps[2].icon),
                                            contentDescription = selectedApps[2].name,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(0xFFD580FF))
                                        )
                                    }
                                }

                                // Bottom-right app icon (fourth app)
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                ) {
                                    if (selectedApps.size > 3) {
                                        Image(
                                            painter = rememberAsyncImagePainter(selectedApps[3].icon),
                                            contentDescription = selectedApps[3].name,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(0xFFD580FF))
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    // Display first app with "& ... other" format when multiple apps are selected
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (selectedApps.isEmpty()) {
                            ClickableText(
                                text = AnnotatedString("No apps selected", SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)),
                                onClick = {
                                    navController.navigate("select_apps")
                                }
                            )
                        } else if (selectedApps.size == 1) {
                            // Show just the single app
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(selectedApps[0].icon),
                                    contentDescription = selectedApps[0].name,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedApps[0].name,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            // Show first app with "& ... other" text
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Image(
                                    painter = rememberAsyncImagePainter(selectedApps[0].icon),
                                    contentDescription = selectedApps[0].name,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${selectedApps[0].name} & ... other",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "How do you want to limit those apps?",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Header banner replacing the dropdown (similar to Image 2)
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

            // Scrollable time selection UI with two narrower side-by-side columns
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
                        highlightedColor = Color(0xFF4B4BE7) // Blue highlight color from image
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
                text = "Want to name your limit? (Optional)",
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Limit name input
            OutlinedTextField(
                value = limitName,
                onValueChange = { limitName = it },
                placeholder = { Text("Limit Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = Purple40,
                    unfocusedBorderColor = Color.LightGray
                ),
                singleLine = true
            )

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
                            val selectedAppsInfo = viewModel.getSelectedAppsInfo()

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

                                    SetaDailyLimitEntity(
                                        appName = app.name,
                                        packageName = app.packageName,
                                        icon = iconString,
                                        timeLimitMinutes = timeLimitMinutes
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
                                                            // Navigate after a short delay to ensure the snackbar is shown
                                                            delay(500)
                                                            navController.navigate(BottomNavItem.UsageLimit.route) {
                                                                // Use a simpler navigation with fewer options
                                                                popUpTo(navController.graph.startDestinationId)
                                                                launchSingleTop = true
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

    // Dialog to show all selected apps when clicked
    if (showAppsDialog) {
        AlertDialog(
            onDismissRequest = { showAppsDialog = false },
            title = { Text("Selected Apps") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    selectedApps.forEach { app ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(app.icon),
                                contentDescription = app.name,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = app.name,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAppsDialog = false }) {
                    Text("Close")
                }
            }
        )
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