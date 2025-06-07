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
                                "%02d".format(value.toInt())
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
            // Gunakan semua data jam dari 0-23
            val filteredData = hourlyData.sortedBy { it.hour }

            // Konversi entri untuk bar chart
            val barEntries = filteredData.map { hourData ->
                val hour = hourData.hour
                val minutes = TimeUnit.MILLISECONDS.toMinutes(hourData.usageTimeInMillis).toFloat()
                BarEntry(hour.toFloat(), minutes)
            }

            // Buat dataset
            val barDataSet = BarDataSet(barEntries, "Usage Time").apply {
                color = Color(0xFF8A65F6).toArgb() // Warna ungu
                valueTextSize = 10f
                valueTextColor = AndroidColor.GRAY
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value > 0) "${value.toInt()}m" else ""
                    }
                }
                setDrawValues(true)
            }

            // Update data chart
            chart.data = BarData(barDataSet).apply {
                barWidth = 0.8f // Lebar bar yang lebih lebar
            }

            // Hitung nilai maksimum sumbu Y
            val maxUsage = barEntries.maxOfOrNull { it.y } ?: 50f
            chart.axisLeft.apply {
                axisMaximum = maxUsage * 1.2f
                axisMinimum = 0f
            }

            // Atur rentang sumbu X untuk menampilkan semua jam
            chart.xAxis.apply {
                axisMinimum = -0.5f
                axisMaximum = 23.5f
                labelCount = 24 // Tampilkan label untuk setiap jam
            }

            // Sesuaikan posisi nilai
            chart.setDrawValueAboveBar(true)

            // Refresh chart
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
                            return if (value.toInt() in 0..23) {
                                "%02d".format(value.toInt())
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
            // Gunakan semua data jam dari 0-23
            val filteredData = hourlyData.sortedBy { it.hour }

            // Siapkan entri data dengan konversi menit
            val entries = filteredData.map { hourData ->
                val hour = hourData.hour.toFloat()
                val minutes = TimeUnit.MILLISECONDS.toMinutes(hourData.usageTimeInMillis).toFloat()
                Entry(hour, minutes)
            }

            // Buat dataset
            val dataSet = LineDataSet(entries, "Usage Time").apply {
                color = Color(0xFF8A65F6).toArgb() // Warna ungu
                lineWidth = 2.5f
                setDrawCircles(true)
                setDrawCircleHole(true)
                circleRadius = 4f
                circleHoleRadius = 2f
                setCircleColor(Color(0xFF8A65F6).toArgb())

                // Format nilai untuk menampilkan menit
                valueTextSize = 10f
                valueTextColor = AndroidColor.GRAY
                setDrawValues(true)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value > 0) "${value.toInt()}m" else ""
                    }
                }

                // Tambahkan penanda sorotan
                setDrawHighlightIndicators(true)
                highlightLineWidth = 1f
            }

            // Update data chart
            chart.data = LineData(dataSet)

            // Hitung nilai maksimum sumbu Y
            val maxUsage = entries.maxOfOrNull { it.y } ?: 50f
            chart.axisLeft.apply {
                axisMaximum = maxUsage * 1.2f
                axisMinimum = 0f
            }

            // Atur rentang sumbu X
            chart.xAxis.apply {
                axisMinimum = -0.5f
                axisMaximum = 23.5f
                labelCount = 24 // Tampilkan label untuk setiap jam
            }

            // Refresh chart
            chart.invalidate()
        }
    )
}