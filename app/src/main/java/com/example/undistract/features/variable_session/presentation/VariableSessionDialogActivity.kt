package com.example.undistract.features.variable_session.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.variable_session.data.VariableSessionRepository

class VariableSessionDialogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()

            // Dapatkan database
            val database = AppDatabase.getDatabase(applicationContext)
            val variableSessionDao = database.variableSessionDao()
            val repository = VariableSessionRepository(variableSessionDao)
            val viewModel = VariableSessionViewModel(repository)
            val packageName = intent.getStringExtra("PACKAGE_NAME") ?: "Unknown"

            VariableLimitDialog(navController, viewModel, packageName)
        }
    }
}
