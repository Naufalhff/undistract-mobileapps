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
import com.example.undistract.ui.theme.Purple40
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import android.util.Log
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import android.os.Parcelable
import com.example.undistract.features.block_schedules.data.BlockSchedulesRepository
import com.example.undistract.features.usage_limit.domain.AppLimitInfo

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
            )
        )
    )

    // Akses dailyLimits dari UsageLimitViewModel
    val dailyLimits by usageLimitViewModel.dailyLimits.collectAsState()
    val appUsageProgress by usageLimitViewModel.appUsageProgress.collectAsState()

    // State untuk menampung aplikasi yang dibatasi
    val limitedUsageApps = remember { mutableStateListOf<AppLimitInfo>() }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Update limitedUsageApps ketika dailyLimits berubah
    LaunchedEffect(dailyLimits, appUsageProgress) {
        limitedUsageApps.clear()
        dailyLimits.forEach { limit ->
            try {
                // Try to get the app icon from the package manager first
                val packageManager = context.packageManager
                val iconDrawable = try {
                    // First try to get the actual app icon from the package manager
                    packageManager.getApplicationIcon(limit.packageName)
                } catch (e: PackageManager.NameNotFoundException) {
                    // If that fails, try to use the stored icon string
                    try {
                        // Try to convert the icon string to a resource ID
                        val iconResId = limit.icon.toIntOrNull() ?: R.drawable.app_logo
                        ContextCompat.getDrawable(context, iconResId)
                    } catch (e: Exception) {
                        Log.e("EditUsageLimitScreen", "Error loading icon for ${limit.appName}", e)
                        ContextCompat.getDrawable(context, R.drawable.app_logo)
                    }
                } ?: ContextCompat.getDrawable(context, R.drawable.app_logo)!!

                // Get the progress from the ViewModel - same as UsageLimitScreen
                val progress = if (limit.isActive) {
                    appUsageProgress[limit.packageName] ?: 0f
                } else {
                    0f // Set progress to 0 for inactive apps
                }

                // Calculate used time in minutes - same as UsageLimitScreen
                val usedMinutes = if (limit.isActive) {
                    (progress * limit.timeLimitMinutes).toInt()
                } else {
                    0
                }
                
                // Format time limit - same as UsageLimitScreen
                val timeLimit = "${limit.timeLimitMinutes / 60}h ${limit.timeLimitMinutes % 60}m"
                val usageText = "$timeLimit (${usedMinutes}m used)"

                limitedUsageApps.add(
                    AppLimitInfo(
                        id = limit.id,
                        appName = limit.appName,
                        packageName = limit.packageName,
                        icon = iconDrawable,
                        timeLimit = usageText,
                        progress = progress,
                        isBlocked = false // Ensure isBlocked matches isActive
                    )
                )
            } catch (e: Exception) {
                Log.e("EditUsageLimitScreen", "Error creating AppLimitInfo for ${limit.appName}", e)
            }
        }
    }

    // Initialize usage tracking
    LaunchedEffect(Unit) {
        usageLimitViewModel.initUsageTracking(context)
        usageLimitViewModel.refreshUsageStats()
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
                        // Delete selected items
                        coroutineScope.launch {
                            val selectedApps = limitedUsageApps.filter { it.isBlocked }
                            if (selectedApps.isNotEmpty()) {
                                try {
                                    selectedApps.forEach { app ->
                                        usageLimitViewModel.deleteDailyLimitById(app.id)
                                    }
                                    snackbarHostState.showSnackbar("Removed ${selectedApps.size} limits")
                                    usageLimitViewModel.refreshLimits()
                                } catch (e: Exception) {
                                    Log.e("EditUsageLimitScreen", "Error deleting limits", e)
                                    snackbarHostState.showSnackbar("Failed to remove: ${e.message}")
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    )
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

                IconButton(onClick = { usageLimitViewModel.refreshUsageStats() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Purple40
                    )
                }
            }



            if (limitedUsageApps.isNotEmpty()) {
                EditLimitSection(
                    title = "LIMITED DAILY USAGE",
                    count = limitedUsageApps.size,
                    apps = limitedUsageApps,
                    showProgress = true,
                    onAppSelectionChange = { index, isSelected ->
                        limitedUsageApps[index] = limitedUsageApps[index].copy(isBlocked = isSelected)
                    }
                )
            } else {
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
    var allSelected by remember { mutableStateOf(false) }

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
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
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

            if (showProgress && app.timeLimit != null) {
                Text(
                    text = app.timeLimit,
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                app.progress?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = it,
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
        ContextCompat.getDrawable(context, uri.toString().toInt()) // Jika URI adalah resource ID
    } catch (e: Exception) {
        null // Handle error jika konversi gagal
    }
}