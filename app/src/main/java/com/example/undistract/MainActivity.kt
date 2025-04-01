package com.example.undistract

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.block_schedules.data.BlockSchedulesRepository
import com.example.undistract.features.block_schedules.presentation.BlockSchedulesViewModel
import com.example.undistract.features.variable_session.data.VariableSessionRepository
import com.example.undistract.features.variable_session.presentation.VariableSessionScreen
import com.example.undistract.features.variable_session.presentation.VariableSessionViewModel
import androidx.lifecycle.lifecycleScope
import com.example.undistract.features.get_installed_apps.data.InstalledAppsRepository
import com.example.undistract.features.get_installed_apps.domain.AppInfo
import com.example.undistract.features.get_installed_apps.domain.GetInstalledAppsUseCase
import com.example.undistract.ui.navigation.AppNavHost
import com.example.undistract.ui.theme.UndistractTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var installedApps: List<AppInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dapatkan database
        val database = AppDatabase.getDatabase(this)

        // Dapatkan DAO dari database
        val blockSchedulesDao = database.blockSchedulesDao()
        val variableSessionDao = database.variableSessionDao()

        // Buat repository dan viewModel
        val repository = VariableSessionRepository(variableSessionDao)
        val viewModel = VariableSessionViewModel(repository)

        enableEdgeToEdge()

        val repository = InstalledAppsRepository(this)
        val getInstalledAppsUseCase = GetInstalledAppsUseCase(repository)

        lifecycleScope.launch {
            installedApps = getInstalledAppsUseCase(this@MainActivity)

            setContent {
                UndistractTheme {
                    AppNavHost(context = this@MainActivity, installedApps = installedApps)
                }
            }
        }
    }
}