package com.example.undistract.ui.components

import android.graphics.Color as AndroidColor
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.example.undistract.features.my_usage.presentation.HourlyUsageData
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

enum class ChartType {
    BAR_CHART,
    LINE_CHART
}

@Composable
fun UsageChart(
    hourlyData: List<HourlyUsageData>,
    modifier: Modifier = Modifier,
    chartType: ChartType = ChartType.BAR_CHART
) {
    when (chartType) {
        ChartType.BAR_CHART -> UsageBarChart(hourlyData, modifier)
        ChartType.LINE_CHART -> UsageLineChart(hourlyData, modifier)
    }
}

@Composable
fun UsageBarChart(
    hourlyData: List<HourlyUsageData>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            BarChart(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // Configure chart appearance
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(false)
                setPinchZoom(false)
                setDrawGridBackground(false)
                setDrawBarShadow(false)
                setDrawValueAboveBar(true)
                legend.isEnabled = false
                
                // Configure X axis
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    axisLineColor = AndroidColor.LTGRAY
                    textColor = AndroidColor.GRAY
                    textSize = 10f
                    granularity = 1f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return if (value.toInt() in 0..23) {
                                value.toInt().toString()
                            } else {
                                ""
                            }
                        }
                    }
                }

                // Configure left Y axis
                axisLeft.apply {
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    textColor = AndroidColor.GRAY
                    textSize = 10f
                    axisMinimum = 0f
                    
                    // Custom formatter for Y axis
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return if (value > 0) "${value.toInt()}m" else ""
                        }
                    }
                }

                // Disable right Y axis
                axisRight.isEnabled = false
                
                // Set empty data initially
                data = BarData()
                
                // Animation
                animateY(1000)
                
                // Set padding
                setExtraOffsets(8f, 16f, 8f, 8f)
            }
        },
        update = { chart ->
            // Filter data to show only evening hours (18-23) and early morning (0-1)
            // as shown in the screenshot
            val filteredData = hourlyData.filter { it.hour in 18..23 || it.hour in 0..1 }
                .sortedBy { it.hour }
                
            // Convert entries for bar chart
            val barEntries = filteredData.map { hourData ->
                val hour = hourData.hour
                val minutes = TimeUnit.MILLISECONDS.toMinutes(hourData.usageTimeInMillis).toFloat()
                BarEntry(hour.toFloat(), minutes)
            }
            
            // Create the dataset
            val barDataSet = BarDataSet(barEntries, "Usage Time").apply {
                color = Color(0xFF8A65F6).toArgb() // Purple color from screenshot
                valueTextSize = 10f
                valueTextColor = AndroidColor.GRAY
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value > 0) "${value.toInt()}m" else ""
                    }
                }
            }
            
            // Update chart data
            chart.data = BarData(barDataSet).apply {
                barWidth = 0.8f // Wider bars
            }
            
            // Calculate Y-axis max value
            val maxUsage = barEntries.maxOfOrNull { it.y } ?: 50f
            chart.axisLeft.axisMaximum = maxUsage * 1.2f
            
            // Set X-axis range to show evening and early morning hours
            chart.xAxis.axisMinimum = 17.5f
            chart.xAxis.axisMaximum = 1.5f
            
            // Refresh the chart
            chart.invalidate()
        }
    )
}

@Composable
fun UsageLineChart(
    hourlyData: List<HourlyUsageData>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            LineChart(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // Configure chart appearance
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(false)
                setPinchZoom(false)
                setDrawGridBackground(false)
                legend.isEnabled = false

                // Configure X axis
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(true)
                    gridColor = AndroidColor.parseColor("#E0E0E0")
                    gridLineWidth = 0.5f
                    axisLineColor = AndroidColor.LTGRAY
                    textColor = AndroidColor.GRAY
                    textSize = 10f
                    granularity = 2f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return when (value.toInt()) {
                                18 -> "18:00"
                                24 -> "Tengah Malam"
                                else -> ""
                            }
                        }
                    }
                }

                // Configure left Y axis
                axisLeft.apply {
                    setDrawGridLines(false)
                    setDrawAxisLine(false)
                    textColor = AndroidColor.GRAY
                    textSize = 10f
                    axisMinimum = 0f
                    
                    // Custom formatter for Y axis
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return if (value > 0) "${value.toInt()}m" else ""
                        }
                    }
                }

                // Disable right Y axis
                axisRight.isEnabled = false

                // Set empty data initially
                data = LineData()

                // Animation
                animateX(1000)

                // Add padding
                setExtraOffsets(8f, 16f, 8f, 16f)
            }
        },
        update = { chart ->
            // Filter data to show only evening hours (18-23) and early morning (0-1)
            val filteredData = hourlyData.filter { it.hour in 18..23 || it.hour in 0..1 }
                .sortedBy { it.hour }
                
            // Prepare data entries with converted minutes
            val entries = filteredData.map { hourData ->
                val hour = hourData.hour.toFloat()
                val minutes = TimeUnit.MILLISECONDS.toMinutes(hourData.usageTimeInMillis).toFloat()
                Entry(hour, minutes)
            }

            // Create dataset
            val dataSet = LineDataSet(entries, "Usage Time").apply {
                color = Color(0xFF8A65F6).toArgb() // Purple color from screenshot
                lineWidth = 2.5f
                setDrawCircles(true)
                setDrawCircleHole(true)
                circleRadius = 4f
                circleHoleRadius = 2f
                setCircleColor(Color(0xFF8A65F6).toArgb())
                mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth curves
                
                // Format values
                valueTextSize = 10f
                valueTextColor = AndroidColor.GRAY
                setDrawValues(false)
                
                // Add highlight marker
                setDrawHighlightIndicators(true)
                highlightLineWidth = 1f
                
                // Fill area under the line
                setDrawFilled(true)
                fillColor = Color(0x508A65F6).toArgb() // Semi-transparent purple
                fillAlpha = 80
            }

            // Update chart data
            chart.data = LineData(dataSet)

            // Calculate Y-axis max value
            val maxUsage = entries.maxOfOrNull { it.y } ?: 50f
            chart.axisLeft.axisMaximum = maxUsage * 1.2f

            // Set X-axis range
            chart.xAxis.axisMinimum = 17.5f
            chart.xAxis.axisMaximum = 1.5f

            // Refresh the chart
            chart.invalidate()
        }
    )
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