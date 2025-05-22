package com.example.undistract.features.add_behavior.presentation

import android.annotation.SuppressLint
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
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
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
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.block_permanent.data.BlockPermanentRepository
import com.example.undistract.features.block_permanent.presentation.BlockPermanentScreen
import com.example.undistract.features.block_permanent.presentation.BlockPermanentViewModel
import com.example.undistract.features.block_permanent.presentation.BlockPermanentViewModelFactory
import com.example.undistract.features.block_schedules.presentation.BlockSchedulesScreen
import com.example.undistract.features.block_schedules.presentation.BlockSchedulesViewModel
import com.example.undistract.features.get_app_data.domain.AppOrUrlItem
import com.example.undistract.features.get_visited_urls.data.VisitedUrlsRepository
import com.example.undistract.features.select_apps.presentation.BlockSchedulesViewModelFactory
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModelFactory
import com.example.undistract.features.variable_session.data.VariableSessionRepository
import com.example.undistract.features.variable_session.presentation.VariableSessionScreen
import com.example.undistract.features.variable_session.presentation.VariableSessionViewModel
import com.example.undistract.features.variable_session.presentation.VariableSessionViewModelFactory
import com.example.undistract.ui.components.BackButton
import com.example.undistract.ui.navigation.BottomNavItem

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun AddRestrictionScreen(navController: NavHostController, isParental: Boolean = false) {
    val context = LocalContext.current
    val database = AppDatabase.getDatabase(context)
    var currentMainSection by remember { mutableStateOf("base") }
    var restrictionName by remember { mutableStateOf("") }
    val parentEntry = remember {
        navController.getBackStackEntry("add_restriction")
    }

    val blockPermanentDao = remember { database.blockPermanentDao() }
    val variableSessionDao = remember { database.variableSessionDao() }
    val visitedUrlsDao = remember { database.visitedUrlsDao() }

    val blockPermanentRepository = remember { BlockPermanentRepository(blockPermanentDao) }
    val variableSessionRepository = remember { VariableSessionRepository(variableSessionDao) }
    val visitedUrlsRepository = remember { VisitedUrlsRepository(visitedUrlsDao) }

    val selectAppsViewModel = runCatching {
        if (parentEntry.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)) {
            viewModel<SelectAppsViewModel>(
                viewModelStoreOwner = parentEntry,
                factory = SelectAppsViewModelFactory(context, visitedUrlsRepository = visitedUrlsRepository)
            )
        } else null
    }.getOrNull()


    val blockPermanentViewModel: BlockPermanentViewModel = viewModel(
        factory = BlockPermanentViewModelFactory(blockPermanentRepository)
    )

    val blockSchedulesViewModel: BlockSchedulesViewModel = viewModel(
        factory = BlockSchedulesViewModelFactory(context)
    )

    val variableSessionViewModel: VariableSessionViewModel = viewModel(
        factory = VariableSessionViewModelFactory(variableSessionRepository)
    )

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // SECTION 1: BACK BUTTON
        BackButtonSection(
            navController, currentMainSection, onSectionChange = { currentMainSection = it }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // SECTION 2: APP SELECTOR
            if (selectAppsViewModel != null) {
                AppSelectorSection(
                    selectAppsViewModel,
                    navController
                )
            }

            // SECTION 3: RESTRICTION NAME INPUT OR MAIN QUESTION
            if (currentMainSection != "base") {
                RestrictionNameInput(
                    restrictionName = restrictionName,
                    onRestrictionNameChange = { restrictionName = it }
                )
            } else {
                Text(
                    text = stringResource(R.string.choose_what_restriction),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))
            }

            // SECTION 4: MAIN SECTION
            when (currentMainSection) {
                "base" -> BaseSection(onSectionChange = { currentMainSection = it }, isParental = isParental)
                "block_permanent" -> selectAppsViewModel?.let {
                    BlockPermanentScreen(
                        navController = navController,
                        blockPermanentViewModel = blockPermanentViewModel,
                        selectAppsViewModel = it,
                        repository = blockPermanentRepository,
                        isParental = isParental
                    )
                }
                "block_schedule" -> selectAppsViewModel?.let {
                    BlockSchedulesScreen(
                        navController = navController,
                        selectAppViewModel = it,
                        viewModel = blockSchedulesViewModel
                    )
                }
//                "daily_limit" -> SetDailyUsageLimitScreen()
                "session_limit" -> selectAppsViewModel?.let {
                    VariableSessionScreen(
                        navController = navController,
                        viewModel = variableSessionViewModel,
                        selectAppViewModel = it
                    )
                }
            }
        }
    }
}

@Composable
fun BackButtonSection(
    navController: NavHostController,
    currentMainSection: String,
    onSectionChange: (String) -> Unit
) {
    Row (
        modifier = Modifier
            .padding(start = 16.dp)
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.background),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BackButton (
            modifier = Modifier.size(24.dp),
            onClick = {
                if (currentMainSection == "base") {
                    navController.navigate(BottomNavItem.UsageLimit.route)
                } else {
                    onSectionChange("base")
                }
            }
        )

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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEAD6FF))
                .padding(16.dp)
                .clickable {
                    navController.navigate("select_apps")
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

                // Display first app with "& ... other" format when multiple apps are selected
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    if (selectedApps.isEmpty()) {
                        ClickableText(
                            text = AnnotatedString(
                                "No apps selected",
                                SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            ),
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
                            AppIcon(app = selectedApps[0], size = 24.dp)
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
                            AppIcon(app = selectedApps[0], size = 24.dp)
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
fun BaseSection(onSectionChange: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // FIRST LINE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FlexboxItem(
                icon = Icons.Default.Star,
                label = stringResource(R.string.block_permanently),
                onClick = { onSectionChange("block_permanent") }
            )

            FlexboxItem(
                icon = Icons.Default.Star,
                label = stringResource(R.string.block_on_a_schedule),
                onClick = { onSectionChange("block_schedule") }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECOND LINE
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FlexboxItem(
                icon = Icons.Default.Star,
                label = stringResource(R.string.restrict_daily_usage),
                onClick = { onSectionChange("daily_limit") }
            )

            FlexboxItem(
                icon = Icons.Default.Star,
                label = stringResource(R.string.apply_custom_session_restriction),
                onClick = { onSectionChange("session_limit") }
            )
        }
    }
}

@Composable
fun FlexboxItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
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
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary
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

@Composable
fun RestrictionNameInput(
    restrictionName: String,
    onRestrictionNameChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(start = 21.dp, end = 21.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = stringResource(R.string.name_your_restriction),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = restrictionName,
            onValueChange = onRestrictionNameChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(text = "Restriction Name") },
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}
