package com.example.undistract.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.undistract.R

sealed class BottomNavItem(val route: String, val title: String, val icon: Int) {
    object MyUsage : BottomNavItem("myUsage", "My Usage", R.drawable.my_usage_icon)
    object UsageLimit : BottomNavItem("usageLimit", "Usage Limit", R.drawable.usage_limit_icon)
    object ParentalControl : BottomNavItem("parentalControl", "Parental", R.drawable.parental_control_icon)
    object Profile : BottomNavItem("profile", "Profile", R.drawable.profile_icon)
}
