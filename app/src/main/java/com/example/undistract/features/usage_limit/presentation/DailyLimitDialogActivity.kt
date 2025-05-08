package com.example.undistract.features.usage_limit.presentation

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.undistract.ui.theme.Purple40

class DailyLimitDialogActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyLimitDialogContent(
                appName = intent.getStringExtra("APP_NAME") ?: "this app",
                onDismiss = {
                    closeApp(intent.getStringExtra("PACKAGE_NAME"))
                    finish()
                }
            )
        }
    }

    private fun closeApp(packageName: String?) {
        if (packageName != null) {
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            activityManager.killBackgroundProcesses(packageName)
        }
    }
}

@Composable
fun DailyLimitDialogContent(appName: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Daily Limit Reached",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "You have reached your daily usage limit for $appName. Please take a break!",
                fontSize = 16.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Purple40)
            ) {
                Text("OK", color = Color.White)
            }
        }
    )
}