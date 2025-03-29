package com.example.undistract

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.undistract.ui.navigation.AppNavHost
import com.example.undistract.ui.theme.UndistractTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UndistractTheme {
                AppNavHost()
            }
        }
    }
}