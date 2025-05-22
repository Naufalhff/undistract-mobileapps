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
                    isEnabled = false // Hide Y-axis labels as per screenshot
                }

                // Disable right Y axis
                axisRight.isEnabled = false

                // Set empty data initially
                data = BarData()

                // Animation
                animateY(1000)

                // Set padding
                setExtraOffsets(8f, 24f, 8f, 8f)
            }
        },
        update = { chart ->
            // Filter data to show a limited set of hours - based on the screenshot (around 5-20)
            val filteredData = hourlyData.filter { it.hour in 5..20 }
                .sortedBy { it.hour }

            // Convert entries for bar chart
            val barEntries = filteredData.map { hourData ->
                val hour = hourData.hour
                val minutes = TimeUnit.MILLISECONDS.toMinutes(hourData.usageTimeInMillis).toFloat()
                BarEntry(hour.toFloat(), minutes)
            }

            // Create the dataset
            val barDataSet = BarDataSet(barEntries, "Usage Time").apply {
                color = Color(0xFF8A65F6).toArgb() // Purple color
                valueTextSize = 10f
                valueTextColor = AndroidColor.GRAY
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value > 0) "${value.toInt()}m" else ""
                    }
                }
                setDrawValues(true)
            }

            // Update chart data
            chart.data = BarData(barDataSet).apply {
                barWidth = 0.5f // Thinner bars like in screenshot
            }

            // Calculate Y-axis max value
            val maxUsage = barEntries.maxOfOrNull { it.y } ?: 50f
            chart.axisLeft.axisMaximum = maxUsage * 1.2f

            // Set X-axis range to show the right hours
            chart.xAxis.apply {
                axisMinimum = 4.5f
                axisMaximum = 20.5f
                labelCount = 16 // Show all hour labels
            }

            // Custom value positioning
            chart.setDrawValueAboveBar(true)

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
                    setDrawGridLines(false)
                    axisLineColor = AndroidColor.LTGRAY
                    textColor = AndroidColor.GRAY
                    textSize = 10f
                    granularity = 1f
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return if (value.toInt() in 5..20) {
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
                    isEnabled = false // Hide Y-axis labels as per screenshot
                }

                // Disable right Y axis
                axisRight.isEnabled = false

                // Set empty data initially
                data = LineData()

                // Animation
                animateX(1000)

                // Add padding
                setExtraOffsets(8f, 24f, 8f, 8f)
            }
        },
        update = { chart ->
            // Filter data to show hours 5-20 as in screenshot
            val filteredData = hourlyData.filter { it.hour in 5..20 }
                .sortedBy { it.hour }

            // Prepare data entries with converted minutes
            val entries = filteredData.map { hourData ->
                val hour = hourData.hour.toFloat()
                val minutes = TimeUnit.MILLISECONDS.toMinutes(hourData.usageTimeInMillis).toFloat()
                Entry(hour, minutes)
            }

            // Create dataset
            val dataSet = LineDataSet(entries, "Usage Time").apply {
                color = Color(0xFF8A65F6).toArgb() // Purple color
                lineWidth = 2.5f
                setDrawCircles(true)
                setDrawCircleHole(true)
                circleRadius = 4f
                circleHoleRadius = 2f
                setCircleColor(Color(0xFF8A65F6).toArgb())

                // Format values to show minutes
                valueTextSize = 10f
                valueTextColor = AndroidColor.GRAY
                setDrawValues(true)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value > 0) "${value.toInt()}m" else ""
                    }
                }

                // Add highlight marker
                setDrawHighlightIndicators(true)
                highlightLineWidth = 1f
            }

            // Update chart data
            chart.data = LineData(dataSet)

            // Calculate Y-axis max value
            val maxUsage = entries.maxOfOrNull { it.y } ?: 50f
            chart.axisLeft.axisMaximum = maxUsage * 1.2f

            // Set X-axis range
            chart.xAxis.axisMinimum = 4.5f
            chart.xAxis.axisMaximum = 20.5f

            // Refresh the chart
            chart.invalidate()
        }
    )
}