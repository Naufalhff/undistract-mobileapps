package com.example.undistract.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun BackButton(
    modifier: Modifier = Modifier,
    contentDescription: String = "Back Button",
    tint: Color = MaterialTheme.colorScheme.onPrimary,
    onClick: () -> Unit
) {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.clickable { onClick() }
    )
}