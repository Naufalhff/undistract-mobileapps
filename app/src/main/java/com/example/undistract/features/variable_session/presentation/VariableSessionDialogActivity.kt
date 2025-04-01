package com.example.undistract.features.variable_session.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.variable_session.data.VariableSessionRepository

class VariableSessionDialogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Menambahkan layout Compose
        setContent {
            // Dapatkan database
            val database = AppDatabase.getDatabase(applicationContext)
            val variableSessionDao = database.variableSessionDao()
            val repository = VariableSessionRepository(variableSessionDao)
            val viewModel = VariableSessionViewModel(repository)

            VariableLimitDialog(viewModel)
        }
    }
}
