package com.example.undistract.features.block_permanent.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel

@Composable
fun BlockPermanentScreen(
    navController: NavHostController,
    viewModel: SelectAppsViewModel
) {
    // Set rute saat ini
    viewModel.updateCurrentRoute("block_permanent")

    // Mendapatkan daftar aplikasi yang dipilih dari ViewModel
    val selectedApps = viewModel.getSelectedApps()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Ini Screen Blok Permanen")

        Spacer(modifier = Modifier.height(8.dp))

        // Menampilkan daftar aplikasi yang dipilih
        if (selectedApps.isEmpty()) {
            Text("Tidak ada aplikasi yang dipilih.")
        } else {
            Text("Aplikasi yang Dipilih:")
            selectedApps.forEach { packageName ->
                Text("- $packageName")
            }
        }
    }
}