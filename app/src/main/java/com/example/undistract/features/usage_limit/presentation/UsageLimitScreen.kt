package com.example.undistract.features.usage_limit.presentation

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.CalendarToday
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.undistract.R
import com.example.undistract.ui.theme.Purple40
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.compose.material.icons.filled.Refresh
import androidx.core.content.ContextCompat
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepository
import com.example.undistract.features.setadaily_limit.data.local.SetaDailyLimitEntity
import com.example.undistract.features.usage_stats.UsageStatsManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.setadaily_limit.data.SetaDailyLimitRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsageLimitScreen(context: Context, navController: NavHostController, viewModel: SelectAppsViewModel) {
    // Get the UsageLimitViewModel
    val usageLimitViewModel: UsageLimitViewModel = viewModel(
        factory = UsageLimitViewModelFactory(
            SetaDailyLimitRepositoryImpl(
                AppDatabase.getDatabase(context).setaDailyLimitDao()
            )
        )
    )

    // Initialize usage tracking
    LaunchedEffect(Unit) {
        usageLimitViewModel.initUsageTracking(context)
    }

    // Ambil data dailyLimits dari ViewModel
    val dailyLimits by usageLimitViewModel.dailyLimits.collectAsState()
    val appUsageProgress by usageLimitViewModel.appUsageProgress.collectAsState()

    // State untuk menampung aplikasi yang dibatasi
    val limitedUsageApps by produceState(initialValue = mutableListOf<AppLimitInfo>(), dailyLimits, appUsageProgress) {
        value = dailyLimits.map { limit ->
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
                        Log.e("UsageLimitScreen", "Error loading icon for ${limit.appName}", e)
                        ContextCompat.getDrawable(context, R.drawable.app_logo)
                    }
                } ?: ContextCompat.getDrawable(context, R.drawable.app_logo)!!

                // Get the progress from the ViewModel
                val progress = appUsageProgress[limit.packageName] ?: 0f

                // Calculate used time in minutes
                val usedMinutes = (progress * limit.timeLimitMinutes).toInt()
                val timeLimit = "${limit.timeLimitMinutes / 60}h ${limit.timeLimitMinutes % 60}m"
                val usageText = "$timeLimit (${usedMinutes}m used)"

                AppLimitInfo(
                    id = limit.id,
                    appName = limit.appName,
                    packageName = limit.packageName,
                    icon = iconDrawable,
                    isBlocked = limit.isActive,
                    timeLimit = usageText,
                    progress = if (limit.isActive) progress else 0f
                )
            } catch (e: Exception) {
                Log.e("UsageLimitScreen", "Error creating AppLimitInfo for ${limit.appName}", e)
                // Return a default entity if there's an error
                AppLimitInfo(
                    id = limit.id,
                    appName = limit.appName,
                    packageName = limit.packageName,
                    icon = ContextCompat.getDrawable(context, R.drawable.app_logo)!!,
                    isBlocked = true,
                    timeLimit = "Error",
                    progress = 0.0f
                )
            }
        }.toMutableList()
    }

    // Observe loading state
    val isLoading by usageLimitViewModel.isLoading.collectAsState()

    // Check if there are any usage limits set
    val hasNoLimits = limitedUsageApps.isEmpty()

    // Pastikan untuk memanggil refreshUsageStats saat screen menjadi aktif
    LaunchedEffect(Unit) {
        usageLimitViewModel.refreshUsageStats()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Usage Limits",
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
                        onClick = { usageLimitViewModel.refreshUsageStats() },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
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
            if (!hasNoLimits) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { navController.navigate("add_restriction") },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Purple40,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text("Add Limit")
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Purple40)
                }
            } else {
                if (hasNoLimits) {
                    // Empty state view
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.emptystate),
                            contentDescription = "Empty Usage Limit",
                            modifier = Modifier
                                .size(200.dp)
                                .padding(bottom = 24.dp)
                        )

                        Text(
                            text = "Ketuk tombol \"Tambahkan Batas Penggunaan\" di bawah dan temukan opsi pembatasan yang paling efektif untuk mengontrol waktu layar Anda.",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )

                        Button(
                            onClick = { navController.navigate("add_restriction") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Purple40
                            ),
                            shape = RoundedCornerShape(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                tint = Color.White,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "Tambahkan Batas Penggunaan",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    // Content view when there are items
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White)
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 90.dp)
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
                                text = "Usage Limits",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (limitedUsageApps.isNotEmpty()) {
                                TextButton(onClick = {
                                    navController.navigate("editUsageLimit")
                                }) {
                                    Text(
                                        text = "Edit",
                                        color = Purple40,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        if (limitedUsageApps.isNotEmpty()) {
                            LimitSection(
                                title = "LIMITED DAILY USAGE",
                                count = limitedUsageApps.size,
                                apps = limitedUsageApps,
                                showProgress = true,
                                onAppToggleChange = { index, isActive ->
                                    val app = limitedUsageApps[index]
                                    usageLimitViewModel.toggleAppActiveState(app.id, isActive)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LimitSection(
    title: String,
    count: Int,
    apps: List<AppLimitInfo>,
    showTimeIcon: Boolean = false,
    showProgress: Boolean = false,
    onAppToggleChange: (Int, Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$title ($count)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
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
                AppLimitItem(
                    app = app,
                    showTimeIcon = showTimeIcon,
                    showProgress = showProgress,
                    onToggleChange = { isChecked ->
                        onAppToggleChange(index, isChecked)
                    }
                )
            }
        }
    }
}

@Composable
fun AppLimitItem(
    app: AppLimitInfo,
    showTimeIcon: Boolean = false,
    showProgress: Boolean = false,
    onToggleChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                onClick = { /* TODO: Add navigation here */ },
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

        Switch(
            checked = app.isBlocked,
            onCheckedChange = { isChecked ->
                onToggleChange(isChecked)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Purple40,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}


