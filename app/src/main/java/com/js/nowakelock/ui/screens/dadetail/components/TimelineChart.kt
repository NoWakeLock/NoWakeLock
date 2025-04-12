package com.js.nowakelock.ui.screens.dadetail.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(top = 20.dp, bottom = 30.dp)
    ) {
        if (data.isEmpty()) return@Canvas
        
        val chartWidth = size.width
        val chartHeight = size.height - 30.dp.toPx() // Reserve space for labels
        
        // Find maximum value for scaling
        val maxValue = data.maxOf { max(it.total, 1) } // Ensure at least 1 for division
        
        // Calculate bar width based on available space and data points
        val barWidth = chartWidth / (data.size * 2) // Each hour gets a bar width + spacing
        val barSpacing = barWidth / 2
        
        // Draw the baseline
        drawLine(
            color = Color.LightGray.copy(alpha = 0.5f),
            start = Offset(0f, chartHeight),
            end = Offset(chartWidth, chartHeight),
            strokeWidth = 1.dp.toPx()
        )
        
        // Draw bars and hour labels
        data.forEachIndexed { index, hourData ->
            val x = index * (barWidth * 2) + barWidth / 2
            
            // Draw total activity bar (light gray)
            if (hourData.total > 0) {
                val totalBarHeight = (hourData.total.toFloat() / maxValue) * chartHeight
                drawRect(
                    color = surfaceVariant,
                    topLeft = Offset(x - barWidth / 2, chartHeight - totalBarHeight),
                    size = Size(barWidth, totalBarHeight)
                )
            }
            
            // Draw blocked activity bar (primary color)
            if (hourData.blocked > 0) {
                val blockedBarHeight = (hourData.blocked.toFloat() / maxValue) * chartHeight
                drawRect(
                    color = primaryColor,
                    topLeft = Offset(x - barWidth / 2, chartHeight - blockedBarHeight),
                    size = Size(barWidth, blockedBarHeight)
                )
            }
            
            // Draw hour label
            val labelStyle = TextStyle(
                fontSize = 10.sp,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            val labelOffset = drawTextCentered(
                textMeasurer = textMeasurer,
                text = hourData.label,
                x = x,
                y = chartHeight + 15.dp.toPx(),
                style = labelStyle
            )
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
//        textMeasurer = textMeasurer,
        textLayoutResult = textLayoutResult,
        topLeft = Offset(xPos, yPos)
    )
    
    return Offset(xPos + textWidth, yPos + textHeight)
}