package com.example.undistract.features.profile.presentation

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.undistract.MainActivity
import com.example.undistract.features.profile.AuthActivity
import com.example.undistract.ui.components.UnderConstructionScreen
import com.example.undistract.ui.theme.ColorNew

@Composable
fun ProfileScreen(context: Context, navController: NavHostController) {
    val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    val token = prefs.getString("token", "") ?: ""
    val userId = prefs.getInt("userId", -1)
    val username = prefs.getString("name", "Unknown User")
    val email = prefs.getString("email", "") ?: ""

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Icon",
                    modifier = Modifier
                        .size(96.dp)
                        .padding(top = 32.dp),
                    tint = ColorNew.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (!token.isNullOrEmpty() && userId != -1) "Welcome, $username" else "You are not logged in",
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = if (!token.isNullOrEmpty() && userId != -1) "Email: $email" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Text(
                    text = if (!token.isNullOrEmpty() && userId != -1) "User ID: $userId" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (token.isNullOrEmpty() || userId == -1) {
                    Button(
                        onClick = {
                            val intent = Intent(context, AuthActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorNew.primary)
                    ) {
                        Text("Login", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = {
                            prefs.edit().clear().apply()
                            val intent = Intent(context, MainActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ColorNew.primary)
                    ) {
                        Text("Logout", color = Color.White)
                    }
                }
            }
        }
    }
}

