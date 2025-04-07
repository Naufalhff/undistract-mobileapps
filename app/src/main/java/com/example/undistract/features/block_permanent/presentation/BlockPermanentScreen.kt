package com.example.undistract.features.block_permanent.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.undistract.R
import com.example.undistract.features.block_permanent.data.local.BlockPermanentEntity
import com.example.undistract.features.get_installed_apps.domain.AppInfo
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel
import com.example.undistract.ui.components.AppSelector
import com.example.undistract.ui.components.BackButton
import com.example.undistract.ui.components.RestrictionNameInput
import com.example.undistract.ui.navigation.BottomNavItem

@Composable
fun BlockPermanentScreen(
    navController: NavHostController,
    selectAppsViewModel: SelectAppsViewModel,
    blockPermanentViewModel: BlockPermanentViewModel
) {
    val context = LocalContext.current

    selectAppsViewModel.updateCurrentRoute("block_permanent")

    val selectedPackageNames = selectAppsViewModel.getSelectedApps()

    val installedApps = selectAppsViewModel.installedApps.collectAsState().value

    var restrictionName by remember { mutableStateOf("") }

    var selectedAppsWithInfo by remember { mutableStateOf<List<AppInfo>>(emptyList()) }

    LaunchedEffect(selectedPackageNames, installedApps) {
        selectedAppsWithInfo = installedApps.filter { appInfo ->
            selectedPackageNames.contains(appInfo.packageName)
        }
    }

    Column (
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // BACK BUTTON
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

        Spacer(modifier = Modifier.height(8.dp))

        Column (
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // APPS SELECTOR
            AppSelector(
                icon = Icons.Default.Star,
                title = stringResource(R.string.choose_apps_to_restrict),
                navController = navController,
                destinationRoute = "select_apps"
            )

            Spacer(modifier = Modifier.height(16.dp))

            RestrictionNameInput(
                value = restrictionName,
                onValueChange = { restrictionName = it }
            )

            Spacer(modifier = Modifier.weight(1f))

            Row (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { navController.navigate(BottomNavItem.UsageLimit.route)}
                ) {
                    Text(text = "Cancel")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        try {
                            selectedAppsWithInfo.forEach { appInfo ->
                                val blockPermanentEntity = BlockPermanentEntity(
                                    packageName = appInfo.packageName,
                                    appName = restrictionName.ifEmpty { appInfo.name },
                                    isActive = true,
                                )
                                blockPermanentViewModel.insertBlockPermanent(blockPermanentEntity)
                                Log.d("BlockPermanentScreen", "Data saved: ${blockPermanentEntity.packageName}, ${blockPermanentEntity.appName}")
                            }
                            navController.navigate(BottomNavItem.UsageLimit.route)
                        } catch (e: Exception) {
                            Log.e("BlockPermanentScreen", "Error saving data: ${e.message}", e)
                        }
                    }
                ) {
                    Text(text = "Save")
                }
            }
        }
    }
}