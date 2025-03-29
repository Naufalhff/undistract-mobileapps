package com.example.undistract.features.select_apps.presentation

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter

@Composable
fun SelectAppsScreen(context: Context, navController: NavHostController) {
    val installedApps = remember { getInstalledApps(context) }
    val selectedApps = remember { mutableStateMapOf<String, Boolean>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Pilih Aplikasi:", style = MaterialTheme.typography.headlineSmall)

        LazyColumn(
            modifier = Modifier.weight(1f) // Pastikan daftar bisa discroll tanpa error
        ) {
            items(installedApps) { app ->
                val isChecked = selectedApps[app.packageName] ?: false
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    // Menampilkan ikon aplikasi
                    Image(
                        painter = rememberAsyncImagePainter(app.icon),
                        contentDescription = app.name,
                        modifier = Modifier.size(40.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Nama aplikasi
                    Text(
                        text = app.name,
                        modifier = Modifier.weight(1f) // Supaya teks tidak bertabrakan dengan checkbox
                    )

                    // Checkbox untuk memilih aplikasi
                    Checkbox(
                        checked = isChecked,
                        onCheckedChange = { selectedApps[app.packageName] = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp)) // Jeda sebelum tombol

        Button(
            onClick = {
                val selected = selectedApps.filter { it.value }.keys
                println("Aplikasi terpilih: $selected")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Simpan Pilihan")
        }
    }
}

// Mengambil daftar aplikasi yang bukan sistem
fun getInstalledApps(context: Context): List<AppInfo> {
    val packageManager = context.packageManager
    return packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { app ->
            // Menyaring aplikasi sistem dengan memeriksa flag FLAG_SYSTEM
            (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0
        }
        .map { app ->
            AppInfo(
                name = packageManager.getApplicationLabel(app).toString(),
                packageName = app.packageName,
                icon = app.loadIcon(packageManager) // Mengambil ikon aplikasi
            )
        }
        .sortedBy { it.name }
}

// Data class untuk menyimpan informasi aplikasi
data class AppInfo(val name: String, val packageName: String, val icon: Drawable)
