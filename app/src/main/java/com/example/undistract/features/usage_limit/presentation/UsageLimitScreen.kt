package com.example.undistract.features.usage_limit.presentation

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.undistract.ui.components.UnderConstructionScreen

@Composable
fun UsageLimitScreen(context: Context, navController: NavHostController) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        UnderConstructionScreen()

        FloatingActionButton(
            onClick = {
                navController.navigate("add_restriction")
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                painter = painterResource(id = android.R.drawable.ic_input_add),
                contentDescription = "Tambah",
                tint = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .size(32.dp)
            )
        }
    }
}