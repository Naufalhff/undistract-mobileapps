package com.example.undistract.features.block_permanent.presentation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.undistract.features.block_permanent.data.local.BlockPermanentEntity
import com.example.undistract.features.get_installed_apps.domain.AppInfo
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel
import com.example.undistract.ui.navigation.BottomNavItem
import com.example.undistract.ui.theme.ColorNew

@Composable
fun BlockPermanentScreen(
    navController: NavHostController,
    selectAppsViewModel: SelectAppsViewModel,
    blockPermanentViewModel: BlockPermanentViewModel
) {
    val selectedPackageNames = selectAppsViewModel.getSelectedApps()

    val installedApps = selectAppsViewModel.installedApps.collectAsState().value

    var restrictionName by remember { mutableStateOf("") }

    var selectedAppsWithInfo by remember { mutableStateOf<List<AppInfo>>(emptyList()) }

    LaunchedEffect(selectedPackageNames, installedApps) {
        selectedAppsWithInfo = installedApps.filter { appInfo ->
            selectedPackageNames.contains(appInfo.packageName)
        }
    }

    Box (
        modifier = Modifier.fillMaxSize()
    ) {
        Row (
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = ColorNew.primary
                ),
                onClick = { navController.navigate(BottomNavItem.UsageLimit.route)}
            ) {
                Text(text = "Cancel")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorNew.primary,
                    contentColor = Color.White
                ),
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