package com.passiveincome.tracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passiveincome.tracker.data.IncomeSource
import com.passiveincome.tracker.ui.theme.DarkSurface
import com.passiveincome.tracker.ui.theme.DarkTextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProjectionScreen(sources: List<IncomeSource>) {
    var selectedDays by remember { mutableIntStateOf(30) }
    var customDaysStr by remember { mutableStateOf("") }
    var isCustomSelected by remember { mutableStateOf(false) }
    
    val timeWindows = listOf(
        30 to "1M", 
        90 to "3M", 
        365 to "1A", 
        1825 to "5A", 
        3650 to "10A"
    )
    
    val projection = remember(sources, selectedDays) {
        calculateProjection(sources, selectedDays)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "Crecimiento Proyectado",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Simulación con interés compuesto por niveles.",
                fontSize = 12.sp,
                color = DarkTextSecondary
            )
        }

        // Selectors Row
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                timeWindows.forEach { (days, label) ->
                    FilterChip(
                        selected = selectedDays == days && !isCustomSelected,
                        onClick = { 
                            selectedDays = days
                            isCustomSelected = false
                        },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6366F1),
                            selectedLabelColor = Color.White,
                            containerColor = DarkSurface,
                            labelColor = DarkTextSecondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            borderColor = if (selectedDays == days && !isCustomSelected) Color.Transparent else Color(0xFF2E3D60)
                        )
                    )
                }
                
                FilterChip(
                    selected = isCustomSelected,
                    onClick = { isCustomSelected = true },
                    label = { Text("Ps", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6366F1),
                        selectedLabelColor = Color.White,
                        containerColor = DarkSurface,
                        labelColor = DarkTextSecondary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isCustomSelected) Color.Transparent else Color(0xFF2E3D60)
                    )
                )
            }

            if (isCustomSelected) {
                OutlinedTextField(
                    value = customDaysStr,
                    onValueChange = { 
                        customDaysStr = it
                        it.toIntOrNull()?.let { days -> 
                            if (days > 0) selectedDays = days 
                        }
                    },
                    label = { Text("Días a proyectar") },
                    placeholder = { Text("Ej. 500") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = DarkSurface,
                        focusedLabelColor = Color(0xFF6366F1),
                        unfocusedLabelColor = DarkTextSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        // Summary Card
        val totalStart = sources.sumOf { it.totalBalance }
        val totalEnd = projection.lastOrNull()?.totalBalance ?: totalStart
        val profit = totalEnd - totalStart
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface, RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Total al final", fontSize = 12.sp, color = DarkTextSecondary)
                Text(
                    text = String.format(Locale.getDefault(), "$%,.2f", totalEnd),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Ganancia", fontSize = 12.sp, color = DarkTextSecondary)
                Text(
                    text = String.format(Locale.getDefault(), "+$%,.2f", profit),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }
        }

        // Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(DarkSurface.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            ProjectionChart(
                projection = projection,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

data class DayProjection(
    val day: Int,
    val totalBalance: Double,
    val sourceBalances: Map<Int, Double>
)

fun calculateProjection(sources: List<IncomeSource>, days: Int): List<DayProjection> {
    val projection = mutableListOf<DayProjection>()
    val currentBalances = sources.associate { it.id to it.balance }.toMutableMap()
    
    // Step calculation to avoid thousands of points on long projections
    val step = when {
        days <= 365 -> 1
        days <= 1825 -> 7  // Weekly for 5 years
        else -> 30        // Monthly for 10+ years
    }

    for (day in 1..days) {
        val totalSourceBalances = mutableMapOf<Int, Double>()
        for (source in sources) {
            val balance = currentBalances[source.id] ?: 0.0
            val tempSource = source.copy(balance = balance)
            
            val y1 = tempSource.balance1 * (source.rate1 / 365.0)
            val y2 = if (source.hasTier2) tempSource.balance2 * (source.rate2 / 365.0) else 0.0
            val y3 = if (source.hasTier3) tempSource.balance3 * (source.rate3 / 365.0) else 0.0
            
            val newBalance = balance + y1 + y2 + y3
            currentBalances[source.id] = newBalance
            totalSourceBalances[source.id] = newBalance
        }
        
        if (day == 1 || day == days || day % step == 0) {
            projection.add(DayProjection(day, totalSourceBalances.values.sum(), totalSourceBalances))
        }
    }
    return projection
}

@Composable
fun ProjectionChart(
    projection: List<DayProjection>,
    modifier: Modifier = Modifier
) {
    if (projection.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(
        color = DarkTextSecondary.copy(alpha = 0.8f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium
    )

    val primaryColor = Color(0xFF00BCD4) // Teal / Verde azulado

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val paddingLeft = 12.dp.toPx()
        val paddingRight = 50.dp.toPx()
        val paddingTop = 10.dp.toPx()
        val paddingBottom = 30.dp.toPx()
        
        val chartWidth = width - paddingRight - paddingLeft
        val chartHeight = height - paddingBottom - paddingTop

        val maxVal = projection.maxOf { it.totalBalance } * 1.01f
        val minVal = projection.minOf { it.totalBalance } * 0.99f
        val range = maxVal - minVal

        // Draw Y-Axis Grid Lines and Labels
        val gridLines = 5
        for (i in 0 until gridLines) {
            val ratio = i.toFloat() / (gridLines - 1)
            val y = paddingTop + chartHeight - (ratio * chartHeight)
            val value = minVal + (ratio * range)
            
            // Grid line
            drawLine(
                color = Color.White.copy(alpha = 0.08f),
                start = Offset(paddingLeft, y),
                end = Offset(paddingLeft + chartWidth, y),
                strokeWidth = 1.dp.toPx()
            )
            
            // Label
            val labelText = if (value >= 1000) {
                String.format(Locale.getDefault(), "$%,.1fk", value / 1000)
            } else {
                String.format(Locale.getDefault(), "$%,.0f", value)
            }
            
            drawText(
                textMeasurer = textMeasurer,
                text = labelText,
                topLeft = Offset(paddingLeft + chartWidth + 8.dp.toPx(), y - 7.dp.toPx()),
                style = labelStyle
            )
        }

        // Draw X-Axis Labels (Days)
        val xLabelsCount = 5
        for (i in 0 until xLabelsCount) {
            val ratio = i.toFloat() / (xLabelsCount - 1)
            val x = paddingLeft + (ratio * chartWidth)
            val dayIndex = (ratio * (projection.size - 1)).toInt()
            val dayLabel = "D${projection[dayIndex].day}"
            
            val textLayoutResult = textMeasurer.measure(dayLabel, labelStyle)
            drawText(
                textMeasurer = textMeasurer,
                text = dayLabel,
                topLeft = Offset(x - (textLayoutResult.size.width / 2), paddingTop + chartHeight + 8.dp.toPx()),
                style = labelStyle
            )
        }

        // Paths for Line and Fill
        val path = Path()
        val fillPath = Path()
        
        projection.forEachIndexed { index, dayData ->
            val x = paddingLeft + (index.toFloat() / (projection.size - 1) * chartWidth)
            val y = paddingTop + chartHeight - ((dayData.totalBalance.toFloat() - minVal.toFloat()) / range.toFloat() * chartHeight)
            val clampedY = y.coerceIn(paddingTop, paddingTop + chartHeight)

            if (index == 0) {
                path.moveTo(x, clampedY)
                fillPath.moveTo(x, paddingTop + chartHeight)
                fillPath.lineTo(x, clampedY)
            } else {
                path.lineTo(x, clampedY)
                fillPath.lineTo(x, clampedY)
            }
            
            if (index == projection.size - 1) {
                fillPath.lineTo(x, paddingTop + chartHeight)
                fillPath.close()
            }
        }

        // Draw Area Fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(primaryColor.copy(alpha = 0.25f), Color.Transparent),
                startY = paddingTop,
                endY = paddingTop + chartHeight
            ),
            style = Fill
        )
        
        // Draw Main Line
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        
        // Final point dot
        val lastX = paddingLeft + chartWidth
        val lastY = paddingTop + chartHeight - ((projection.last().totalBalance.toFloat() - minVal.toFloat()) / range.toFloat() * chartHeight)
        drawCircle(
            color = Color.White,
            radius = 5.dp.toPx(),
            center = Offset(lastX, lastY)
        )
        drawCircle(
            color = primaryColor,
            radius = 3.dp.toPx(),
            center = Offset(lastX, lastY)
        )
    }
}
