package com.example.undistract.features.profile.presentation

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun ProfileScreen (context: Context, navController: NavHostController) {
    Column (
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Text("Ini Screen Profile")
        Button(
            onClick = {
                val intent = Intent(context, LoginActivity::class.java)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Go to Login")
        }
    }
}