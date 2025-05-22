package com.example.undistract.features.select_apps.presentation

import android.annotation.SuppressLint
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.undistract.features.get_app_data.domain.AppOrUrlItem
import com.example.undistract.ui.components.BackButton

@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun SelectAppsScreen(
    navController: NavHostController,
) {
    // Mendapatkan ViewModel dengan parentEntry untuk navigasi
    val parentEntry = remember {
        navController.getBackStackEntry("add_restriction")
    }
    val viewModel: SelectAppsViewModel = viewModel(parentEntry)

    // Mendapatkan daftar item yang digabungkan (Aplikasi dan URL)
    val combinedItems by viewModel.combinedItems.collectAsState()

    // Mendapatkan status pilihan aplikasi
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
                onClick = { navController.popBackStack() }
            )

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "Select Apps",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // LIST OF INSTALLED APPLICATION OR URL (Gabungan)
        LazyColumn(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = combinedItems,
                key = { item -> item.identifier }
            ) { item ->
                val isChecked = selectedAppsMap[item.identifier] ?: false

                // Menangani item berdasarkan jenisnya (aplikasi atau URL)
                when (item) {
                    is AppOrUrlItem.AppItem -> {
                        ListItem(
                            item = item,
                            isChecked = isChecked,
                            onCheckedChange = { isChecked ->
                                viewModel.toggleAppSelection(item.identifier, isChecked)
                            }
                        )
                    }
                    is AppOrUrlItem.UrlItem -> {
                        ListItem(
                            item = item,
                            isChecked = isChecked,
                            onCheckedChange = { isChecked ->
                                viewModel.toggleAppSelection(item.identifier, isChecked)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ListItem(
    item: AppOrUrlItem,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
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
                painter = rememberAsyncImagePainter(item.icon),
                contentDescription = item.name,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = item.name,
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