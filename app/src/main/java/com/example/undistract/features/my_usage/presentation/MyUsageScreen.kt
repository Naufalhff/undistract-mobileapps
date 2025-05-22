package com.example.undistract.features.my_usage.presentation

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.undistract.config.AppDatabase
import com.example.undistract.features.usage_stats.UsageStatsManager
import com.example.undistract.ui.components.ChartType
import com.example.undistract.ui.components.UsageChart
import com.example.undistract.ui.theme.Purple40
import kotlinx.coroutines.launch

@Composable
fun MyUsageScreen(context: Context, navController: NavHostController) {
    val usageViewModel: MyUsageViewModel = viewModel(
        factory = MyUsageViewModelFactory(context)
    )
    
    val appUsageStats by usageViewModel.appUsageStats.collectAsState()
    val hourlyUsageData by usageViewModel.hourlyUsageData.collectAsState()
    val totalUsage by usageViewModel.totalUsage.collectAsState()
    
    // Refresh data when screen appears
    LaunchedEffect(key1 = Unit) {
        usageViewModel.refreshUsageStats()
    }
    
    // Add state to track chart type
    var chartType by remember { mutableStateOf(ChartType.BAR_CHART) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Analytics",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(
                onClick = { /* Notification or settings */ },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Notifications",
                    tint = Color.Gray
                )
            }
        }
        
        // Usage chart card with chart type buttons
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column {
                // Chart type selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp, top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { chartType = ChartType.BAR_CHART },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Bar Chart",
                            tint = if (chartType == ChartType.BAR_CHART) Color(0xFF8A65F6) else Color.Gray
                        )
                    }
                    IconButton(
                        onClick = { chartType = ChartType.LINE_CHART },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Line Chart",
                            tint = if (chartType == ChartType.LINE_CHART) Color(0xFF8A65F6) else Color.Gray
                        )
                    }
                }
                
                // Chart
                UsageChart(
                    hourlyData = if (hourlyUsageData.isEmpty()) generatePlaceholderData() else hourlyUsageData,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    chartType = chartType
                )
            }
        }

        // Mobile app total usage
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
            ) {
                Text(
                    text = "My Usage",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    text = formatDuration(totalUsage),
                    color = Color.Gray
                )
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        // App usage section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "App Usage",
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            )
        }
        
        // App usage list
        LazyColumn {
            items(appUsageStats) { appUsage ->
                AppUsageItem(appUsage = appUsage)
            }
        }
    }
}

@Composable
fun AppUsageItem(appUsage: AppUsageInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App icon
        Image(
            painter = rememberAsyncImagePainter(appUsage.appIcon),
            contentDescription = appUsage.appName,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        
        // App info
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = appUsage.appName,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            
            // Usage progress bar
            LinearProgressIndicator(
                progress = { appUsage.usagePercentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Purple40,
                trackColor = Color(0xFFE0D0FF)
            )
        }
        
        // Usage time
        Text(
            text = formatDuration(appUsage.usageTimeInMillis),
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

fun formatDuration(millis: Long): String {
    val seconds = millis / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    
    return when {
        hours > 0 -> "${hours}h ${minutes % 60}m"
        minutes > 0 -> "${minutes}m ${seconds % 60}s"
        else -> "${seconds}s"
    }
}

private fun generatePlaceholderData(): List<HourlyUsageData> {
    // Generate sample data that matches the screenshot pattern
    return (0..23).map { hour ->
        val usageTime = when (hour) {
            18 -> 23 * 60000L  // 23m
            19 -> 27 * 60000L  // 27m
            20 -> 18 * 60000L  // 18m
            21 -> 42 * 60000L  // 42m
            22 -> 35 * 60000L  // 35m
            23 -> 5 * 60000L   // 5m
            0 -> 1 * 60000L    // 1m
            else -> 0L         // No usage
        }
        
        HourlyUsageData(hour, usageTime)
    }
}