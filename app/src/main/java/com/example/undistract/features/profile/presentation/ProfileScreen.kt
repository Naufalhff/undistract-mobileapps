package com.example.undistract.features.profile.presentation

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

@Composable
fun ProfileScreen (context: Context, navController: NavHostController) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text("Ini Screen Profile")
    }
}