package com.example.undistract.features.add_limit.presentation

import android.annotation.SuppressLint
import android.util.Log
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.undistract.R
import com.example.undistract.di.ViewModelFactoryProvider
import com.example.undistract.features.block_permanent.presentation.BlockPermanentScreen
import com.example.undistract.features.block_permanent.presentation.BlockPermanentViewModel
import com.example.undistract.features.block_schedules.presentation.BlockSchedulesScreen
import com.example.undistract.features.block_schedules.presentation.BlockSchedulesViewModel
import com.example.undistract.features.get_app_data.domain.AppOrUrlItem
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel
import com.example.undistract.features.setadaily_limit.presentation.SetDailyUsageLimitScreen
import com.example.undistract.features.setadaily_limit.presentation.SetaDailyLimitViewModel
import com.example.undistract.features.usage_limit.presentation.UsageLimitViewModel
import com.example.undistract.features.variable_session.presentation.VariableSessionScreen
import com.example.undistract.features.variable_session.presentation.VariableSessionViewModel
import com.example.undistract.ui.components.BackButton
import com.example.undistract.ui.navigation.BottomNavItem
import com.example.undistract.ui.theme.ColorNew

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun AddRestrictionScreen(navController: NavHostController, isParental: Boolean = false) {
    val context = LocalContext.current
    val parentEntry = remember {
        navController.getBackStackEntry("add_restriction")
    }
    var currentMainSection by remember { mutableStateOf("base") }

    val selectAppsViewModel = runCatching {
        if (parentEntry.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            viewModel<SelectAppsViewModel>(
                viewModelStoreOwner = parentEntry,
                factory = ViewModelFactoryProvider.provideSelectAppsViewModelFactory(context)
            )
        } else null
    }.getOrNull()

    val addLimitViewModel: AddLimitViewModel = viewModel(
        factory = ViewModelFactoryProvider.provideAddLimitViewModelFactory()
    )

    val blockPermanentViewModel: BlockPermanentViewModel = viewModel(
        factory = ViewModelFactoryProvider.provideBlockPermanentViewModelFactory(context)
    )

    val blockSchedulesViewModel: BlockSchedulesViewModel = viewModel(
        factory = ViewModelFactoryProvider.provideBlockSchedulesViewModelFactory(
            context,
            isParental
        )
    )

    val variableSessionViewModel: VariableSessionViewModel = viewModel(
        factory = ViewModelFactoryProvider.provideVariableSessionViewModelFactory(
            context,
            isParental
        )
    )

    val usageLimitViewModel: UsageLimitViewModel = viewModel(
        factory = ViewModelFactoryProvider.provideUsageLimitViewModelFactory(context, isParental)
    )

    val setaDailyLimitViewModel: SetaDailyLimitViewModel = viewModel(
        factory = ViewModelFactoryProvider.provideSetaDailyLimitViewModelFactory(
            context,
            isParental
        )
    )

    val selectedPackageNames = selectAppsViewModel?.getSelectedIdentifiers()

    val combinedItems = selectAppsViewModel?.combinedItems?.collectAsState()?.value.orEmpty()

    var selectedAppsWithInfo by remember { mutableStateOf<List<AppOrUrlItem>>(emptyList()) }

    LaunchedEffect(selectedPackageNames, combinedItems) {
        if (selectedPackageNames != null) {
            selectedAppsWithInfo = combinedItems.filter { item ->
                selectedPackageNames.contains(item.identifier)
            }
        }
    }

    val containsUrlItem = selectedAppsWithInfo.any { it is AppOrUrlItem.UrlItem }

    val selectedAppsPairs = selectedAppsWithInfo.map { it.name to it.identifier }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // SECTION 1: BACK BUTTON
        BackButtonSection(navController,
            currentMainSection,
            onSectionChange = { currentMainSection = it })

        Spacer(modifier = Modifier.height(8.dp))

        // SECTION 2: MAIN SECTION
        if (selectAppsViewModel != null) {
            if (selectedAppsWithInfo.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 56.dp, bottom = 80.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // APP SELECTOR
                    AppSelectorSection(selectAppsViewModel, navController)
                }

                // HINT
                Text(
                    text = stringResource(R.string.select_apps_hint),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 56.dp, bottom = 80.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // APP SELECTOR
                    AppSelectorSection(selectAppsViewModel, navController)

                    // MAIN SECTION
                    Text(
                        text = stringResource(R.string.choose_what_restriction),
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (currentMainSection != "base")
                    {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEAD6FF))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val text = when (currentMainSection) {
                                    "daily_limit" -> stringResource(R.string.restrict_daily_usage)
                                    "session_limit" -> stringResource(R.string.apply_custom_session_restriction)
                                    "block_schedule" -> stringResource(R.string.block_on_a_schedule)
                                    "block_permanent" -> stringResource(R.string.block_permanently)
                                    else -> ""
                                }

                                Text(
                                    text = text,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp)
                                )

                                IconButton(
                                    onClick = {
                                        if (currentMainSection == "base") {
                                            navController.navigate(BottomNavItem.UsageLimit.route)
                                        } else {
                                            currentMainSection = "base"
                                        }
                                    },
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
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    MainSectionSwitcher(
                        currentMainSection = currentMainSection,
                        onSectionChange = { currentMainSection = it },
                        selectAppsViewModel = selectAppsViewModel,
                        isParental = isParental,
                        addLimitViewModel = addLimitViewModel
                    )
                }
            }
        }

        if (selectedAppsWithInfo.isNotEmpty()) {
            // SECTION 3: CANCEL AND SAVE BUTTON
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent, contentColor = ColorNew.primary
                ), onClick = {
                    navController.navigate(BottomNavItem.UsageLimit.route)
                }) {
                    Text(text = stringResource(R.string.action_cancel))
                }

                Spacer(modifier = Modifier.width(8.dp))

                val buttonColor =
                    if (currentMainSection != "base" && selectedAppsWithInfo.isNotEmpty()) {
                        ColorNew.primary
                    } else {
                        Color.Gray
                    }

                Button(shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = buttonColor, contentColor = Color.White
                ), onClick = {
                    when (currentMainSection) {
                        "block_permanent" -> {
                            if (selectedAppsWithInfo.isNotEmpty()) {
                                blockPermanentViewModel.saveBlockedApps(selectedApps = selectedAppsWithInfo,
                                    isParental = isParental,
                                    onSuccess = {
                                        if (isParental) {
                                            navController.navigate("parental_usage_limit?isParental=true") {
                                                launchSingleTop = true
                                            }
                                        } else {
                                            navController.navigate(BottomNavItem.UsageLimit.route) {
                                                launchSingleTop = true
                                            }
                                        }
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.toast_save_success),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onError = { e ->
                                        // Tangani error
                                        Log.e("BlockPermanent", "Failed to save", e)
                                    })
                            }
                        }

                        "block_schedule" -> {
                            if (selectedAppsWithInfo.isNotEmpty()) {
                                when {
                                    addLimitViewModel.scheduleData.value == null -> {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.toast_schedule_data_incomplete),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    addLimitViewModel.scheduleData.value?.daysOfWeek?.removePrefix("[")
                                        ?.removeSuffix("]")?.split(",")
                                        ?.map { it.trim().toBoolean() }?.all { !it } == true -> {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.toast_select_at_least_one_day),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    addLimitViewModel.scheduleData.value != null &&
                                            !addLimitViewModel.scheduleData.value!!.isAllDay &&
                                            addLimitViewModel.scheduleData.value!!.startTime == addLimitViewModel.scheduleData.value!!.endTime -> {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.toast_start_end_time_same),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    else -> {
                                        val data = addLimitViewModel.scheduleData.value!!
                                        blockSchedulesViewModel.saveBlockSchedule(apps = selectedAppsPairs,
                                            daysOfWeek = data.daysOfWeek,
                                            isAllDay = data.isAllDay,
                                            startTime = data.startTime,
                                            endTime = data.endTime,
                                            isActive = data.isActive,
                                            isParental = data.isParental,
                                            onSuccess = {
                                                if (data.isParental) {
                                                    navController.navigate("parental_usage_limit?isParental=true") {
                                                        launchSingleTop = true
                                                    }
                                                } else {
                                                    navController.navigate(BottomNavItem.UsageLimit.route) {
                                                        launchSingleTop = true
                                                    }
                                                }
                                                Toast.makeText(
                                                    context,
                                                    context.getString(R.string.toast_save_success),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            onError = { e ->
                                                Log.e("BlockPermanent", "Failed to save", e)
                                            })
                                    }
                                }
                            }
                        }

                        "session_limit" -> {
                            if (containsUrlItem) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.warning_url),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (selectedAppsWithInfo.isNotEmpty()) {
                                val data = addLimitViewModel.sessionData.value
                                data?.let { sessionData ->
                                    variableSessionViewModel.saveVariableSession(apps = selectedAppsPairs,
                                        secondsLeft = sessionData.secondsLeft,
                                        coolDownDuration = sessionData.coolDownDuration,
                                        coolDownEndTime = sessionData.coolDownEndTime,
                                        isOnCooldown = sessionData.isOnCooldown,
                                        isActive = sessionData.isActive,
                                        isParental = sessionData.isParental,
                                        onSuccess = {
                                            if (sessionData.isParental) {
                                                navController.navigate("parental_usage_limit?isParental=true") {
                                                    launchSingleTop = true
                                                }
                                            } else {
                                                navController.navigate(BottomNavItem.UsageLimit.route) {
                                                    launchSingleTop = true
                                                }
                                            }
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.toast_save_success),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = { e ->
                                            Log.e("BlockSchedule", "Failed to save", e)
                                        })
                                }
                            }
                        }

                        "daily_limit" -> {
                            val data = addLimitViewModel.dailyLimitData.value

                            try {
                                if (data != null) {
                                    if (data.timeLimitMinutes == 0) {
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.error_time_limit_zero),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    setaDailyLimitViewModel.saveDailyLimits(data = data,
                                        usageLimitViewModel = usageLimitViewModel,
                                        onSuccess = {
                                            if (isParental) {
                                                navController.navigate("parental_usage_limit?isParental=true") {
                                                    launchSingleTop = true
                                                }
                                            } else {
                                                navController.navigate(BottomNavItem.UsageLimit.route) {
                                                    launchSingleTop = true
                                                }
                                            }
                                            Toast.makeText(
                                                context,
                                                context.getString(R.string.toast_save_success),
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onError = { _ ->
                                            Log.e("SetDailyLimit", "Failed to save")
                                        })
                                }
                            } catch (e: Exception) {
                                Log.e("AddLimitScreen", "Exception saving daily limits", e)
                            }
                        }

                    }
                }

                ) {
                    Text(text = stringResource(R.string.action_save))
                }
            }
        }
    }
}

@Composable
fun BackButtonSection(
    navController: NavHostController, currentMainSection: String, onSectionChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(start = 16.dp)
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.background),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackButton(modifier = Modifier.size(24.dp), onClick = {
            if (currentMainSection == "base") {
                navController.navigate(BottomNavItem.UsageLimit.route)
            } else {
                onSectionChange("base")
            }
        })

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = stringResource(R.string.add_restriction),
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AppSelectorSection(
    selectAppsViewModel: SelectAppsViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Mengambil daftar identifier aplikasi yang dipilih
    val selectedPackageNames = selectAppsViewModel.getSelectedIdentifiers()

    // Mengambil daftar gabungan aplikasi dan URL yang sudah disortir
    val combinedItems by selectAppsViewModel.combinedItems.collectAsState()

    // Filter item yang dipilih berdasarkan identifier
    var selectedApps by remember { mutableStateOf<List<AppOrUrlItem>>(emptyList()) }

    // Ketika daftar selectedPackageNames atau combinedItems berubah, perbarui selectedAppsWithInfo
    LaunchedEffect(selectedPackageNames, combinedItems) {
        selectedApps = combinedItems.filter { item ->
            selectedPackageNames.contains(item.identifier)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEAD6FF))
            .padding(16.dp)
            .clickable {
                navController.navigate("select_apps")
            }) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedApps.isNotEmpty()) {
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
                                AppIconPreview(app = selectedApps.getOrNull(0))

                                // Bottom-left app icon (second app)
                                AppIconPreview(app = selectedApps.getOrNull(1))
                            }
                            Column(
                                modifier = Modifier.fillMaxHeight(),
                                verticalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Top-right app icon (third app)
                                AppIconPreview(app = selectedApps.getOrNull(2))

                                // Bottom-right app icon (fourth app)
                                AppIconPreview(app = selectedApps.getOrNull(3))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                }

                // Display first app with "& ... other" format when multiple apps are selected
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (selectedApps.isEmpty()) {
                        Text(text = stringResource(R.string.select_apps),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable {
                                navController.navigate("select_apps")
                            })
                    } else if (selectedApps.size == 1) {
                        // Show just the single app
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            AppIcon(app = selectedApps[0], size = 24.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedApps[0].name, fontWeight = FontWeight.Medium
                            )
                        }
                    } else {
                        // Show first app with "& ... other" text
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            AppIcon(app = selectedApps[0], size = 24.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(
                                    R.string.selected_multiple_apps,
                                    selectedApps[0].name
                                ),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppIconPreview(app: AppOrUrlItem?) {
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(RoundedCornerShape(2.dp))
    ) {
        if (app != null) {
            Image(
                painter = rememberAsyncImagePainter(app.icon),
                contentDescription = app.name,
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

@Composable
private fun AppIcon(app: AppOrUrlItem, size: Dp) {
    Image(
        painter = rememberAsyncImagePainter(app.icon),
        contentDescription = app.name,
        modifier = Modifier.size(size)
    )
}

@Composable
fun MainSectionSwitcher(
    currentMainSection: String,
    onSectionChange: (String) -> Unit,
    selectAppsViewModel: SelectAppsViewModel?,
    isParental: Boolean,
    addLimitViewModel: AddLimitViewModel
) {
    AnimatedContent(
        targetState = currentMainSection,
        transitionSpec = {
            if (targetState > initialState) {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { fullWidth -> fullWidth }
                ) + fadeIn(animationSpec = tween(300)) togetherWith
                        slideOutHorizontally(
                            animationSpec = tween(300),
                            targetOffsetX = { fullWidth -> -fullWidth }
                        ) + fadeOut(animationSpec = tween(300))
            } else {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { fullWidth -> -fullWidth }
                ) + fadeIn(animationSpec = tween(300)) togetherWith
                        slideOutHorizontally(
                            animationSpec = tween(300),
                            targetOffsetX = { fullWidth -> fullWidth }
                        ) + fadeOut(animationSpec = tween(300))
            }.using(
                SizeTransform(clip = false)
            )
        }
    ) { targetSection ->
        when (targetSection) {
            "base" -> BaseSection(onSectionChange = onSectionChange)
            "block_permanent" -> selectAppsViewModel?.let {
                BlockPermanentScreen()
            }

            "block_schedule" -> selectAppsViewModel?.let {
                BlockSchedulesScreen(
                    selectAppViewModel = it,
                    isParental = isParental,
                    sharedViewModel = addLimitViewModel
                )
            }

            "daily_limit" -> selectAppsViewModel?.let {
                SetDailyUsageLimitScreen(
                    selectAppsViewModel = it,
                    isParental = isParental,
                    sharedViewModel = addLimitViewModel
                )
            }

            "session_limit" -> selectAppsViewModel?.let {
                VariableSessionScreen(
                    selectAppViewModel = it,
                    isParental = isParental,
                    sharedViewModel = addLimitViewModel
                )
            }
        }
    }
}

@Composable
fun BaseSection(onSectionChange: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // FIRST LINE
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FlexboxItem(iconResId = R.drawable.block_permanent_icon,
                label = stringResource(R.string.block_permanently),
                onClick = { onSectionChange("block_permanent") })

            FlexboxItem(iconResId = R.drawable.block_schedule_icon,
                label = stringResource(R.string.block_on_a_schedule),
                onClick = { onSectionChange("block_schedule") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECOND LINE
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FlexboxItem(iconResId = R.drawable.daily_usage_icon,
                label = stringResource(R.string.restrict_daily_usage),
                onClick = { onSectionChange("daily_limit") })

            FlexboxItem(iconResId = R.drawable.custom_restriction_icon,
                label = stringResource(R.string.apply_custom_session_restriction),
                onClick = { onSectionChange("session_limit") })
        }
    }
}

@Composable
fun FlexboxItem(
    @DrawableRes iconResId: Int, label: String, isSelected: Boolean = false, onClick: () -> Unit
) {
    val painter = painterResource(id = iconResId)

    Box(
        modifier = Modifier
            .size(145.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 1.dp,
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
            )
            .clickable { onClick() }, contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painter,
                contentDescription = label,
                modifier = Modifier.size(48.dp),
                colorFilter = ColorFilter.tint(ColorNew.primary)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = label,
                fontSize = 12.sp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}
