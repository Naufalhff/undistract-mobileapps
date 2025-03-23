package com.example.undistract.features.usage_limit.presentation

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun UsageLimitScreen (context: Context, navController: NavHostController) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text("Ini Screen Usage Limit")
    }
}