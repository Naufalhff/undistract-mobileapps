package com.example.undistract.features.usage_limit.presentation

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.undistract.R
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepositoryImpl
import com.example.undistract.features.usage_limit.domain.AppLimitInfo
import com.example.undistract.ui.theme.Purple40
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import android.util.Log
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.undistract.features.block_permanent.data.BlockPermanentRepository
import com.example.undistract.features.block_schedules.data.BlockSchedulesRepository
import com.example.undistract.features.variable_session.data.VariableSessionRepository
import androidx.compose.runtime.snapshots.SnapshotStateList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditUsageLimitScreen(context: Context, navController: NavHostController, viewModel: SelectAppsViewModel) {
    val sharedViewModel: SharedViewModel = viewModel()
    val appLimitInfo by sharedViewModel.appLimitInfo.collectAsState()

    val usageLimitViewModel: UsageLimitViewModel = viewModel(
        factory = UsageLimitViewModelFactory(
            repository = SetaDailyLimitRepositoryImpl(
                AppDatabase.getDatabase(context).setaDailyLimitDao()
            ),
            blockSchedulesRepository = BlockSchedulesRepository(
                AppDatabase.getDatabase(context).blockSchedulesDao()
            ),
            variableSessionRepository = VariableSessionRepository(
                AppDatabase.getDatabase(context).variableSessionDao()
            ),
            blockPermanentRepository = BlockPermanentRepository(
                AppDatabase.getDatabase(context).blockPermanentDao()
            )
        )
    )

    // Access data from UsageLimitViewModel
    val dailyLimits by usageLimitViewModel.dailyLimits.collectAsState()
    val appUsageProgress by usageLimitViewModel.appUsageProgress.collectAsState()
    // Access blocked apps data
    val blockedApps by usageLimitViewModel.blockedApps.collectAsState(emptyList())
    // Access variable sessions data
    val variableSessions by usageLimitViewModel.variableSessions.collectAsState()
    // Access permanently blocked apps data
    val blockPermanentApps by usageLimitViewModel.blockPermanentApps.collectAsState()

    // States for app lists that can be selected for deletion
    var limitedUsageApps = remember { mutableStateListOf<AppLimitInfo>() }
    var blockedScheduleApps = remember { mutableStateListOf<AppLimitInfo>() }
    var variableSessionApps = remember { mutableStateListOf<AppLimitInfo>() }
    var permanentlyBlockedApps = remember { mutableStateListOf<AppLimitInfo>() }

    // State to force recomposition when selections change
    var selectionState by remember { mutableStateOf(0) }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Function to update selection state and trigger recomposition
    fun updateSelectionState() {
        selectionState++
    }

    // Update limitedUsageApps when dailyLimits changes
    LaunchedEffect(dailyLimits, appUsageProgress) {
        val newLimitedUsageApps = mutableListOf<AppLimitInfo>()
        dailyLimits.forEach { limit ->
            try {
                // Get app icon
                val packageManager = context.packageManager
                val iconDrawable = try {
                    packageManager.getApplicationIcon(limit.packageName)
                } catch (e: PackageManager.NameNotFoundException) {
                    try {
                        val iconResId = limit.icon.toIntOrNull() ?: R.drawable.app_logo
                        ContextCompat.getDrawable(context, iconResId)
                    } catch (e: Exception) {
                        Log.e("EditUsageLimitScreen", "Error loading icon for ${limit.appName}", e)
                        ContextCompat.getDrawable(context, R.drawable.app_logo)
                    }
                } ?: ContextCompat.getDrawable(context, R.drawable.app_logo)!!

                // Calculate progress and time formatting
                val progress = if (limit.isActive) {
                    appUsageProgress[limit.packageName] ?: 0f
                } else {
                    0f
                }

                val usedMinutes = if (limit.isActive) {
                    (progress * limit.timeLimitMinutes).toInt()
                } else {
                    0
                }

                val timeLimit = "${limit.timeLimitMinutes / 60}h ${limit.timeLimitMinutes % 60}m"
                val usageText = "$timeLimit (${usedMinutes}m used)"

                newLimitedUsageApps.add(
                    AppLimitInfo(
                        id = limit.id,
                        appName = limit.appName,
                        packageName = limit.packageName,
                        icon = iconDrawable,
                        timeLimit = usageText,
                        progress = progress,
                        isBlocked = limitedUsageApps.find { it.id == limit.id }?.isBlocked ?: false // Preserve selection state
                    )
                )
            } catch (e: Exception) {
                Log.e("EditUsageLimitScreen", "Error creating AppLimitInfo for ${limit.appName}", e)
            }
        }
        limitedUsageApps.clear()
        limitedUsageApps.addAll(newLimitedUsageApps)
        updateSelectionState()
    }

    // Update blockedScheduleApps when blockedApps changes
    LaunchedEffect(blockedApps) {
        val newBlockedScheduleApps = mutableListOf<AppLimitInfo>()
        blockedApps.forEach { app ->
            try {
                val iconDrawable = try {
                    context.packageManager.getApplicationIcon(app.packageName)
                } catch (e: Exception) {
                    ContextCompat.getDrawable(context, R.drawable.app_logo)
                } ?: ContextCompat.getDrawable(context, R.drawable.app_logo)!!

                // Process daysOfWeek string
                val daysMap = mapOf(
                    'M' to "Mon", 'T' to "Tue", 'W' to "Wed", 'R' to "Thu",
                    'F' to "Fri", 'S' to "Sat", 'U' to "Sun"
                )
                val activeDays = app.daysOfWeek.mapNotNull { day -> daysMap[day] }.joinToString(", ")
                val scheduleText = "Schedule: $activeDays ${app.startTime} - ${app.endTime}"

                newBlockedScheduleApps.add(
                    AppLimitInfo(
                        id = app.id,
                        appName = app.appName,
                        packageName = app.packageName,
                        icon = iconDrawable,
                        timeLimit = scheduleText,
                        isBlocked = blockedScheduleApps.find { it.id == app.id }?.isBlocked ?: false // Preserve selection state
                    )
                )
            } catch (e: Exception) {
                Log.e("EditUsageLimitScreen", "Error creating AppLimitInfo for blocked app ${app.appName}", e)
            }
        }
        blockedScheduleApps.clear()
        blockedScheduleApps.addAll(newBlockedScheduleApps)
        updateSelectionState()
    }

    // Update variableSessionApps when variableSessions changes
    LaunchedEffect(variableSessions) {
        val newVariableSessionApps = mutableListOf<AppLimitInfo>()
        variableSessions.forEach { session ->
            try {
                val iconDrawable = try {
                    context.packageManager.getApplicationIcon(session.packageName)
                } catch (e: Exception) {
                    ContextCompat.getDrawable(context, R.drawable.app_logo)
                } ?: ContextCompat.getDrawable(context, R.drawable.app_logo)!!

                val timeText = "${session.secondsLeft / 60}m ${session.secondsLeft % 60}s"

                newVariableSessionApps.add(
                    AppLimitInfo(
                        appName = session.appName,
                        packageName = session.packageName,
                        icon = iconDrawable,
                        timeLimit = timeText,
                        progress = 1f, // Full progress for variable sessions

                    )
                )
            } catch (e: Exception) {
                Log.e("EditUsageLimitScreen", "Error creating AppLimitInfo for variable session ${session.appName}", e)
            }
        }
        variableSessionApps.clear()
        variableSessionApps.addAll(newVariableSessionApps)
        updateSelectionState()
    }

    // Update permanentlyBlockedApps when blockPermanentApps changes
    LaunchedEffect(blockPermanentApps) {
        val newPermanentlyBlockedApps = mutableListOf<AppLimitInfo>()
        blockPermanentApps.forEach { app ->
            try {
                val iconDrawable = try {
                    context.packageManager.getApplicationIcon(app.packageName)
                } catch (e: Exception) {
                    ContextCompat.getDrawable(context, R.drawable.app_logo)
                } ?: ContextCompat.getDrawable(context, R.drawable.app_logo)!!

                newPermanentlyBlockedApps.add(
                    AppLimitInfo(
                        id = app.id,
                        appName = app.appName,
                        packageName = app.packageName,
                        icon = iconDrawable,
                        timeLimit = "Permanently Blocked",
                        isBlocked = permanentlyBlockedApps.find { it.id == app.id }?.isBlocked ?: false // Preserve selection state
                    )
                )
            } catch (e: Exception) {
                Log.e("EditUsageLimitScreen", "Error creating AppLimitInfo for permanently blocked app ${app.appName}", e)
            }
        }
        permanentlyBlockedApps.clear()
        permanentlyBlockedApps.addAll(newPermanentlyBlockedApps)
        updateSelectionState()
    }

    // Initialize usage tracking
    LaunchedEffect(Unit) {
        usageLimitViewModel.initUsageTracking(context)
        usageLimitViewModel.refreshUsageStats()
    }

    // Check if we have any restrictions defined
    val noRestrictions = limitedUsageApps.isEmpty() &&
            blockedScheduleApps.isEmpty() &&
            variableSessionApps.isEmpty() &&
            permanentlyBlockedApps.isEmpty()

    // Determine if any apps are selected for deletion - force recomposition with selectionState
    val anyAppsSelected by remember(selectionState) {
        derivedStateOf {
            limitedUsageApps.any { it.isBlocked } ||
                    blockedScheduleApps.any { it.isBlocked } ||
                    variableSessionApps.any { it.isBlocked } ||
                    permanentlyBlockedApps.any { it.isBlocked }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Edit Usage Limits",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 9.dp)
                    )
                },
                modifier = Modifier.height(56.dp),
                windowInsets = WindowInsets(0.dp),
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 4.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "App Logo",
                            modifier = Modifier.size(50.dp))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Gray
                    )
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        // Delete selected items from all categories
                        coroutineScope.launch {
                            var deleteCount = 0
                            try {
                                // Delete selected daily limits
//                                limitedUsageApps.filter { it.isBlocked }.forEach { app ->
//                                    usageLimitViewModel.deleteDailyLimitById(app.id)
//                                    deleteCount++
//                                }
//
//                                // Delete selected block schedules
//                                blockedScheduleApps.filter { it.isBlocked }.forEach { app ->
//                                    usageLimitViewModel.deleteBlockScheduleById(app.id)
//                                    deleteCount++
//                                }
//
//                                // Delete selected variable sessions
//                                variableSessionApps.filter { it.isBlocked }.forEach { app ->
//                                    usageLimitViewModel.deleteVariableSessionById(app.id.toString())
//                                    deleteCount++
//                                }
//
//                                // Delete selected permanent blocks
//                                permanentlyBlockedApps.filter { it.isBlocked }.forEach { app ->
//                                    usageLimitViewModel.deleteBlockPermanentById(app.id)
//                                    deleteCount++
//                                }

                                if (deleteCount > 0) {
                                    snackbarHostState.showSnackbar("Removed $deleteCount restrictions")
                                    // Refresh all data
                                    usageLimitViewModel.refreshLimits()
                                    usageLimitViewModel.refreshBlockedApps()
                                    usageLimitViewModel.refreshVariableSessions()
                                    usageLimitViewModel.refreshBlockPermanentApps()
                                } else {
                                    snackbarHostState.showSnackbar("No items selected for deletion")
                                }
                            } catch (e: Exception) {
                                Log.e("EditUsageLimitScreen", "Error deleting restrictions", e)
                                snackbarHostState.showSnackbar("Failed to remove: ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (anyAppsSelected) Color.Red else Color.Gray,
                        contentColor = Color.White
                    ),
                    enabled = anyAppsSelected
                ) {
                    Text("Delete")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Usage Limits",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                IconButton(onClick = {
                    usageLimitViewModel.refreshUsageStats()
                    usageLimitViewModel.refreshLimits()
                    usageLimitViewModel.refreshBlockedApps()
                    usageLimitViewModel.refreshVariableSessions()
                    usageLimitViewModel.refreshBlockPermanentApps()
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Purple40
                    )
                }
            }

            if (noRestrictions) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.emptystate),
                        contentDescription = "No limits set",
                        modifier = Modifier
                            .size(200.dp)
                            .padding(bottom = 24.dp)
                    )

                    Text(
                        text = "No usage limits have been set yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { navController.navigate("add_restriction") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple40
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Add Limit")
                    }
                }
            } else {
                // Display the four categories of restrictions

                // 1. Daily Usage Limits
                if (limitedUsageApps.isNotEmpty()) {
                    EditLimitSection(
                        title = "LIMITED DAILY USAGE",
                        count = limitedUsageApps.size,
                        apps = limitedUsageApps,
                        showProgress = true,
                        onAppSelectionChange = { index, isSelected ->
                            limitedUsageApps[index] = limitedUsageApps[index].copy(isBlocked = isSelected)
                            updateSelectionState()
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 2. Blocked Apps (by schedule)
                if (blockedScheduleApps.isNotEmpty()) {
                    EditLimitSection(
                        title = "BLOCKED APPS",
                        count = blockedScheduleApps.size,
                        apps = blockedScheduleApps,
                        showTimeIcon = true,
                        onAppSelectionChange = { index, isSelected ->
                            blockedScheduleApps[index] = blockedScheduleApps[index].copy(isBlocked = isSelected)
                            updateSelectionState()
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 3. Variable Sessions
                if (variableSessionApps.isNotEmpty()) {
                    EditLimitSection(
                        title = "VARIABLE SESSIONS",
                        count = variableSessionApps.size,
                        apps = variableSessionApps,
                        showProgress = true,
                        onAppSelectionChange = { index, isSelected ->
                            variableSessionApps[index] = variableSessionApps[index].copy(isBlocked = isSelected)
                            updateSelectionState()
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 4. Permanently Blocked Apps
                if (permanentlyBlockedApps.isNotEmpty()) {
                    EditLimitSection(
                        title = "BLOCKED PERMANENTLY",
                        count = permanentlyBlockedApps.size,
                        apps = permanentlyBlockedApps,
                        onAppSelectionChange = { index, isSelected ->
                            permanentlyBlockedApps[index] = permanentlyBlockedApps[index].copy(isBlocked = isSelected)
                            updateSelectionState()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EditLimitSection(
    title: String,
    count: Int,
    apps: List<AppLimitInfo>,
    showTimeIcon: Boolean = false,
    showProgress: Boolean = false,
    onAppSelectionChange: (Int, Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }
    // Use derivedStateOf to properly track selection state
    var allSelected by remember(apps) {
        mutableStateOf(apps.isNotEmpty() && apps.all { it.isBlocked })
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = allSelected,
                    onCheckedChange = { isChecked ->
                        allSelected = isChecked
                        apps.forEachIndexed { index, _ ->
                            onAppSelectionChange(index, isChecked)
                        }
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Purple40,
                        uncheckedColor = Color.Gray
                    )
                )

                Text(
                    text = "$title ($count)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(onClick = { expanded = !expanded }) {
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier.padding(4.dp)
                )
            }
        }

        if (expanded) {
            apps.forEachIndexed { index, app ->
                EditAppLimitItem(
                    app = app,
                    showTimeIcon = showTimeIcon,
                    showProgress = showProgress,
                    onSelectionChange = { isSelected ->
                        onAppSelectionChange(index, isSelected)
                        // Update allSelected based on all items being selected
                        allSelected = apps.all { it.isBlocked }
                    }
                )
            }
        }
    }
}

@Composable
fun EditAppLimitItem(
    app: AppLimitInfo,
    showTimeIcon: Boolean = false,
    showProgress: Boolean = false,
    onSelectionChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = app.isBlocked,
            onCheckedChange = { isChecked ->
                onSelectionChange(isChecked)
            },
            colors = CheckboxDefaults.colors(
                checkedColor = Purple40,
                uncheckedColor = Color.Gray,
                checkmarkColor = Color.White
            )
        )

        Image(
            painter = rememberAsyncImagePainter(app.icon),
            contentDescription = app.appName,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = app.appName,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            if (app.timeLimit != null) {
                Text(
                    text = app.timeLimit,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                if (showProgress && app.progress != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = app.progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Purple40,
                        trackColor = Color(0xFFE0D0FF)
                    )
                }
            }
        }

        if (showTimeIcon) {
            IconButton(
                onClick = { /* TODO */ },
                modifier = Modifier
                    .size(28.dp)
                    .padding(end = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Set Time",
                    tint = Purple40,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun uriToDrawable(context: Context, uriString: String): Drawable? {
    return try {
        val uri = Uri.parse(uriString)
        ContextCompat.getDrawable(context, uri.toString().toInt()) // If URI is a resource ID
    } catch (e: Exception) {
        null // Handle error if conversion fails
    }
}