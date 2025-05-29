package com.example.undistract.features.block_permanent.presentation

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.undistract.features.block_permanent.data.BlockPermanentRepository
import com.example.undistract.features.block_permanent.data.local.BlockPermanentEntity
import com.example.undistract.features.get_app_data.domain.AppOrUrlItem
import com.example.undistract.features.select_apps.presentation.SelectAppsViewModel
import com.example.undistract.ui.navigation.BottomNavItem
import com.example.undistract.ui.theme.ColorNew

@Composable
fun BlockPermanentScreen(
    navController: NavHostController,
    selectAppsViewModel: SelectAppsViewModel,
    repository: BlockPermanentRepository,
    isParental: Boolean
) {
    val viewModel: BlockPermanentViewModel = viewModel(
        factory = BlockPermanentViewModelFactory(repository, isParental)
    )

    // Mengambil daftar identifier aplikasi yang dipilih
    val selectedPackageNames = selectAppsViewModel.getSelectedIdentifiers()

    // Mengambil daftar gabungan aplikasi dan URL yang sudah disortir
    val combinedItems by selectAppsViewModel.combinedItems.collectAsState()

    // Status untuk menyimpan nama pembatasan
    var restrictionName by remember { mutableStateOf("") }

    // Filter item yang dipilih berdasarkan identifier
    var selectedAppsWithInfo by remember { mutableStateOf<List<AppOrUrlItem>>(emptyList()) }

    // Ketika daftar selectedPackageNames atau combinedItems berubah, perbarui selectedAppsWithInfo
    LaunchedEffect(selectedPackageNames, combinedItems) {
        selectedAppsWithInfo = combinedItems.filter { item ->
            selectedPackageNames.contains(item.identifier)
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // WARNING SECTION
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 16.dp,bottom = 80.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Warning Icon",
                tint = Color.Red,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Aplikasi yang diblokir permanen tidak bisa dibuka sama sekali hingga Anda menonaktifkannya.",
                color = Color.Red,
                fontSize = 14.sp
            )
        }
    }
}