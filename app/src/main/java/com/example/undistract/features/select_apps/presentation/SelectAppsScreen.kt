package com.example.undistract.features.select_apps.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

        // SELECT ALL TOGGLE
        val isSelectAll by viewModel.isSelectAll.collectAsState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Select All",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )

            Checkbox(
                checked = isSelectAll,
                onCheckedChange = {
                    viewModel.toggleSelectAll(combinedItems.map { it.identifier })
                },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    checkmarkColor = MaterialTheme.colorScheme.background
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // LIST OF INSTALLED APPLICATION OR URL (Gabungan)
        val selectedAppsMap = viewModel.selectedApps

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

                when (item) {
                    is AppOrUrlItem.AppItem,
                    is AppOrUrlItem.UrlItem -> {
                        ListItem(
                            item = item,
                            isChecked = isChecked,
                            onCheckedChange = { checked ->
                                viewModel.toggleAppSelection(item.identifier, checked)
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