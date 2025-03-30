package com.example.undistract

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.block_schedules.data.BlockSchedulesRepository
import com.example.undistract.features.block_schedules.presentation.BlockSchedulesViewModel
import com.example.undistract.features.block_schedules.presentation.BlockSchedulesScreen
import com.example.undistract.features.block_schedules.domain.scheduleLogWorker

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dapatkan database
        val database = AppDatabase.getDatabase(this)

        // Dapatkan DAO dari database
        val blockSchedulesDao = database.blockSchedulesDao()

        // Buat repository dan viewModel
        val repository = BlockSchedulesRepository(blockSchedulesDao)
        val viewModel = BlockSchedulesViewModel(repository)

        enableEdgeToEdge()
        setContent {
            BlockSchedulesScreen(viewModel)
        }
    }
}