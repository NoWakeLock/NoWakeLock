package com.js.nowakelock.ui.screens.dadetail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.js.nowakelock.data.model.HourData
import kotlin.math.max
import androidx.compose.ui.res.stringResource
import com.js.nowakelock.R
import androidx.compose.ui.geometry.CornerRadius

/**
 * A chart showing hourly activity timeline for a device automation item.
 * Displays total and blocked activities for each hour over a 24-hour period.
 *
 * @param data The hourly data to display
 * @param modifier Optional modifier for the component
 */
@Composable
fun TimelineChart(
    data: List<HourData>,
    modifier: Modifier = Modifier,
    barCornerRadius: Float = 4.dp.value,
    showYAxisScales: Boolean = true
) {
    // Cache text measurer and colors
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    // Key hours to display labels for (0, 4, 8, 12, 16, 20, 23)
    val keyHours = remember { listOf(0, 4, 8, 12, 16, 20, 23) }

    // Cache max value for scaling
    val maxValue = remember(data) {
        data.maxOfOrNull { max(it.total, 1) } ?: 1
    }

    val now = stringResource(R.string.now)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(top = 16.dp, bottom = 16.dp)
    ) {
        if (data.isEmpty()) return@Canvas

        // Calculate y-axis width based on max value (more digits need more space)
        val digitCount = maxValue.toString().length
        val yAxisWidth = (24 + digitCount * 6).dp.toPx() // Base width + width per digit
        val xAxisHeight = 30.dp.toPx() // Space for x-axis labels
        val rightPadding = 8.dp.toPx() // Right padding to keep bars within bounds
        
        // Calculate chart dimensions
        val chartWidth = size.width - yAxisWidth - rightPadding
        val chartHeight = size.height - xAxisHeight
        
        // Chart origin point (after y-axis space)
        val chartStartX = yAxisWidth
        
        // Y-axis scales and horizontal grid lines
        if (showYAxisScales) {
            val scalePoints = listOf(0.25f, 0.5f, 0.75f, 1.0f)
            
            scalePoints.forEach { scale ->
                val y = chartHeight - (chartHeight * scale)
                
                // Draw scale line
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.5f),
                    start = Offset(chartStartX - 5.dp.toPx(), y),
                    end = Offset(chartStartX, y),
                    strokeWidth = 1.dp.toPx()
                )
                
                // Draw horizontal grid line
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.2f),
                    start = Offset(chartStartX, y),
                    end = Offset(size.width - rightPadding, y),
                    strokeWidth = 0.5.dp.toPx()
                )
                
                // Draw scale value
                val value = (maxValue * scale).toInt()
                if (value > 0) { // Only show non-zero values
                    val labelStyle = TextStyle(fontSize = 8.sp, color = onSurfaceVariant)
                    val textLayoutResult = textMeasurer.measure(value.toString(), labelStyle)
                    
                    // Right-align the text with some padding from the scale line
                    val textX = chartStartX - textLayoutResult.size.width - 8.dp.toPx()
                    
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = Offset(
                            textX.coerceAtLeast(2.dp.toPx()), // Ensure text is visible
                            y - textLayoutResult.size.height / 2
                        )
                    )
                }
            }
        }

        // Draw the baseline
        drawLine(
            color = Color.LightGray.copy(alpha = 0.5f),
            start = Offset(chartStartX, chartHeight),
            end = Offset(size.width - rightPadding, chartHeight),
            strokeWidth = 1.dp.toPx()
        )

        // Calculate bar width based on available space and data points
        val barWidth = chartWidth / (data.size * 2) // Each hour gets a bar width + spacing

        // Find which hours in our data correspond to key hours
        val hoursInData = data.map { it.hour }
        
        // Draw bars and hour labels
        data.forEachIndexed { index, hourData ->
            val x = chartStartX + index * (barWidth * 2) + barWidth / 2

            // Draw total activity bar (light gray with rounded corners)
            if (hourData.total > 0) {
                val totalBarHeight = (hourData.total.toFloat() / maxValue) * chartHeight
                drawRoundRect(
                    color = surfaceVariant,
                    topLeft = Offset(x - barWidth / 2, chartHeight - totalBarHeight),
                    size = Size(barWidth, totalBarHeight),
                    cornerRadius = CornerRadius(barCornerRadius, barCornerRadius)
                )
            }

            // Draw blocked activity bar (primary color with rounded corners)
            if (hourData.blocked > 0) {
                val blockedBarHeight = (hourData.blocked.toFloat() / maxValue) * chartHeight
                drawRoundRect(
                    color = primaryColor,
                    topLeft = Offset(x - barWidth / 2, chartHeight - blockedBarHeight),
                    size = Size(barWidth, blockedBarHeight),
                    cornerRadius = CornerRadius(barCornerRadius, barCornerRadius)
                )
            }

            // Draw time labels - either show key hours or make sure to show the last one as "Now"
            val isKeyHour = keyHours.contains(hourData.hour)
            val isLastHour = index == data.size - 1
            
            if (isKeyHour || isLastHour) {
                val labelText = if (isLastHour) now else hourData.label
                val labelStyle = TextStyle(
                    fontSize = 10.sp,
                    color = onSurfaceVariant
                )

                drawTextCentered(
                    textMeasurer = textMeasurer,
                    text = labelText,
                    x = x,
                    y = chartHeight + 15.dp.toPx(),
                    style = labelStyle
                )
            }
        }
    }
}

/**
 * Helper function to draw centered text.
 */
private fun DrawScope.drawTextCentered(
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    text: String,
    x: Float,
    y: Float,
    style: TextStyle
): Offset {
    val textLayoutResult = textMeasurer.measure(text, style)
    val textWidth = textLayoutResult.size.width
    val textHeight = textLayoutResult.size.height

    val xPos = x - textWidth / 2
    val yPos = y - textHeight / 2

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(xPos, yPos)
    )

    return Offset(xPos + textWidth, yPos + textHeight)
}

/**
 * Preview for TimelineChart
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun TimelineChartPreview() {
    val mockData = List(24) { hour ->
        HourData(
            hour = hour,
            label = if (hour == 0) "12AM" else if (hour < 12) "${hour}AM" else if (hour == 12) "12PM" else "${hour - 12}PM",
            total = (5..20).random(),
            blocked = (0..5).random()
        )
    }

    androidx.compose.material3.Surface {
        TimelineChart(data = mockData)
    }
}

/**
 * Preview for TimelineChart with empty data
 */
@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun TimelineChartEmptyPreview() {
    androidx.compose.material3.Surface {
        TimelineChart(data = emptyList())
    }
}