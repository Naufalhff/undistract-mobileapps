package com.example.undistract.features.usage_limit.domain

import android.graphics.drawable.Drawable

data class AppLimitInfo(
    val id: Int = 0,
    val appName: String,
    val packageName: String = "",
    val icon: Drawable,
    val isBlocked: Boolean = false,
    val timeLimit: String? = null,
    val progress: Float? = null
) 