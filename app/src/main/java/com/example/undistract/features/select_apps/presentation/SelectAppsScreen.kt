package com.example.undistract.features.select_apps.presentation

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.undistract.features.get_installed_apps.domain.AppInfo
import com.example.undistract.ui.components.BackButton

@Composable
fun SelectAppsScreen(
    context: Context,
    navController: NavHostController,
    viewModel: SelectAppsViewModel
) {
    viewModel.updateCurrentRoute("select_apps")

    val installedApps by viewModel.installedApps.collectAsState()
    val selectedAppsMap = viewModel.selectedApps

    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
    ) {
        // BACK BUTTON
        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxWidth()
                .padding(start = 16.dp)
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(
                modifier = Modifier.size(24.dp),
                onClick = { navController.navigate("add_restriction") }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Select Apps",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // LIST OF INSTALLED APPLICATION
        LazyColumn(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = installedApps,
                key = { app -> app.packageName }
            ) { app ->
                val isChecked = selectedAppsMap[app.packageName] ?: false
                AppListItem(
                    app = app,
                    isChecked = isChecked,
                    onCheckedChange = { isChecked ->
                        viewModel.toggleAppSelection(app.packageName, isChecked)
                    }
                )
            }
        }
    }
}

@Composable
private fun AppListItem(
    app: AppInfo,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val context = LocalContext.current

    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(start = 16.dp, end = 16.dp),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = rememberAsyncImagePainter(app.icon),
                contentDescription = app.name,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = app.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )

            Spacer(modifier = Modifier.width(12.dp))

            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    checkmarkColor = MaterialTheme.colorScheme.background
                )
            )
        }
    }
}