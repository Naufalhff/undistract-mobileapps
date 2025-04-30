package com.example.undistract.features.add_behavior.presentation

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.undistract.R
import com.example.undistract.features.get_installed_apps.domain.AppInfo
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel
import com.example.undistract.ui.components.AppSelector
import com.example.undistract.ui.components.BackButton
import com.example.undistract.ui.navigation.BottomNavItem

@Composable
fun AddRestrictionScreen(
    navController: NavHostController,
    viewModel: SelectAppsViewModel
) {
    var currentMainSection by remember { mutableStateOf("base") }
    viewModel.updateCurrentRoute("add_restriction")

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {

        // SECTION 1: BACK BUTTON
        BackButtonSection(navController)

        Spacer(modifier = Modifier.height(8.dp))

        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // SECTION 2: APP SELECTOR
            AppSelector(
                icon = Icons.Default.Star,
                title = stringResource(R.string.choose_apps_to_restrict),
                navController = navController,
                destinationRoute = "select_apps"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // SECTION 3: MAIN SECTION
            when (currentMainSection) {
                "base" -> BaseSection(onSectionChange = { currentMainSection = it })
//                "block_permanent" -> BlockPermanentScreen()
//                "block_schedule" -> BlockScheduleScreen()
//                "daily_limit" -> DailyLimitScreen()
//                "session_limit" -> SessionLimitScreen()
            }
        }
    }
}

@Composable
fun BackButtonSection(
    navController: NavHostController
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
            onClick = { navController.navigate(BottomNavItem.UsageLimit.route)}
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
    selectedApps: List<AppInfo>,
    navController: NavController,
    showAppsDialog: Boolean,
    onShowAppsDialogChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
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
                .clickable(enabled = selectedApps.size > 1) {
                    if (selectedApps.isEmpty()) {
                        navController.navigate("select_apps")
                    } else {
                        onShowAppsDialogChange(true)
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
private fun AppIconPreview(app: AppInfo?) {
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
private fun AppIcon(app: AppInfo, size: Dp) {
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
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
