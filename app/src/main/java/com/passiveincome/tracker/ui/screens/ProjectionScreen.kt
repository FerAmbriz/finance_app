package com.passiveincome.tracker.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import com.passiveincome.tracker.ui.components.DonutChart
import com.passiveincome.tracker.ui.components.ThinkingOrbsHero
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProjectionScreen(sources: List<IncomeSource>) {
    var selectedDays by remember { mutableIntStateOf(365) }
    var customDaysStr by remember { mutableStateOf("") }
    var isCustomSelected by remember { mutableStateOf(false) }
    
    var monthlyContributionStr by remember { mutableStateOf("") }
    var targetMonthlyIncomeStr by remember { mutableStateOf("") }
    var annualInflationStr by remember { mutableStateOf("4.0") } // Default 4%
    
    val monthlyContribution = monthlyContributionStr.toDoubleOrNull() ?: 0.0
    val targetIncome = targetMonthlyIncomeStr.toDoubleOrNull() ?: 0.0
    val annualInflation = annualInflationStr.toDoubleOrNull() ?: 0.0
    
    val timeWindows = listOf(
        30 to "1M", 
        90 to "3M", 
        365 to "1A", 
        1825 to "5A", 
        3650 to "10A"
    )
    
    val projection = remember(sources, selectedDays, monthlyContribution, annualInflation) {
        calculateProjection(sources, selectedDays, monthlyContribution, annualInflation)
    }

    var selectedDayProjection by remember { mutableStateOf<DayProjection?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Selectors and Inputs Column
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

            // Advanced Simulation Inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = monthlyContributionStr,
                    onValueChange = { monthlyContributionStr = it },
                    label = { Text("Aporte Mensual", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("$", color = DarkTextSecondary, fontSize = 12.sp) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = DarkSurface,
                        focusedLabelColor = Color(0xFF6366F1),
                        unfocusedLabelColor = DarkTextSecondary
                    )
                )
                OutlinedTextField(
                    value = targetMonthlyIncomeStr,
                    onValueChange = { targetMonthlyIncomeStr = it },
                    label = { Text("Meta Libertad", fontSize = 11.sp) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("$", color = DarkTextSecondary, fontSize = 12.sp) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = DarkSurface,
                        focusedLabelColor = Color(0xFF10B981),
                        unfocusedLabelColor = DarkTextSecondary
                    )
                )
            }
            
            OutlinedTextField(
                value = annualInflationStr,
                onValueChange = { annualInflationStr = it },
                label = { Text("Inflación Anual Estimada (%)", fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                suffix = { Text("%", color = DarkTextSecondary, fontSize = 12.sp) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFFF59E0B),
                    unfocusedBorderColor = DarkSurface,
                    focusedLabelColor = Color(0xFFF59E0B),
                    unfocusedLabelColor = DarkTextSecondary
                )
            )
        }

        // Summary Card
        val totalStart = sources.sumOf { it.totalBalance }
        val totalContributed = monthlyContribution * (selectedDays / 30.0)
        val totalEnd = projection.lastOrNull()?.totalBalance ?: totalStart
        val profit = totalEnd - (totalStart + totalContributed)
        
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
                Text("Ganancia (Interés)", fontSize = 12.sp, color = DarkTextSecondary)
                Text(
                    text = String.format(Locale.getDefault(), "+$%,.2f", profit),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }
        }

        // Financial Freedom Card
        val dayReached = remember(sources, targetIncome, monthlyContribution) {
            if (targetIncome > 0) findFreedomDay(sources, targetIncome, monthlyContribution) else null
        }

        if (dayReached != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFF59E0B).copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Celebration, contentDescription = null, tint = Color(0xFFF59E0B))
                    }
                    Column {
                        Text(
                            "Meta de Libertad Financiera",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        val years = dayReached / 365
                        val remainingMonths = (dayReached % 365) / 30
                        val reachText = when {
                            dayReached >= 10950 -> "más de 30 años (¡sigue aportando!)"
                            years > 0 && remainingMonths > 0 -> "$years años y $remainingMonths meses"
                            years > 0 -> "$years años"
                            remainingMonths > 0 -> "$remainingMonths meses"
                            else -> "$dayReached días"
                        }
                        Text(
                            "Alcanzarás tu meta en aprox. $reachText",
                            fontSize = 12.sp,
                            color = DarkTextSecondary
                        )
                    }
                }
            }
        } else if (targetIncome > 0) {
            // Feedback if target is unreachable or too high for a simple calculation
            Text(
                "Ingresa una meta mayor a tus ganancias actuales para calcular el tiempo.",
                fontSize = 11.sp,
                color = DarkTextSecondary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        // Chart
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Text(
                "Evolución del Patrimonio (Ajustado)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Box(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                ProjectionChart(
                    projection = projection,
                    onPointSelected = { selectedDayProjection = it },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // End of Projection or Selected Point Breakdown
        if (projection.isNotEmpty()) {
            val displayData = selectedDayProjection ?: projection.last()
            val isFinal = selectedDayProjection == null
            
            Text(
                if (isFinal) "Composición Final Proyectada" else "Composición en Día ${displayData.day}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            val projectedSources = sources.map { source ->
                source.copy(balance = displayData.sourceBalances[source.id] ?: 0.0)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Donut Chart on the left
                Box(modifier = Modifier.weight(1f)) {
                    DonutChart(
                        sources = projectedSources,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }

                // Summary Card on the right
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryItem("R. Diario", displayData.dailyYield, Color(0xFF10B981))
                        SummaryItem("R. Mensual", displayData.dailyYield * 30, Color(0xFF10B981))
                        SummaryItem("R. Anual", displayData.dailyYield * 365, Color(0xFF10B981))
                    }
                }
            }
        }

        // Monthly Milestones Table
        if (projection.isNotEmpty()) {
            Text(
                "Hitos Mensuales",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp)
            )
            
            val monthlyMilestones = projection.filterIndexed { index, data -> 
                data.day % 30 == 0 || index == 0 || index == projection.size - 1
            }.distinctBy { it.day / 30 }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Tiempo", fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.weight(1f))
                    Text("Total (Real)", fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.weight(1.5f))
                    Text("Rend. Diario", fontSize = 12.sp, color = DarkTextSecondary, modifier = Modifier.weight(1.5f))
                }
                
                monthlyMilestones.take(12).forEach { milestone ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val timeText = when {
                            milestone.day == 1 -> "Inicio"
                            milestone.day % 365 == 0 -> "${milestone.day / 365}A"
                            else -> "${milestone.day / 30}M"
                        }
                        Text(timeText, fontSize = 13.sp, color = Color.White, modifier = Modifier.weight(1f))
                        Text(
                            String.format(Locale.getDefault(), "$%,.0f", milestone.totalBalance),
                            fontSize = 13.sp,
                            color = Color.White,
                            modifier = Modifier.weight(1.5f)
                        )
                        Text(
                            String.format(Locale.getDefault(), "$%,.2f", milestone.dailyYield),
                            fontSize = 13.sp,
                            color = Color(0xFF10B981),
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }
                if (monthlyMilestones.size > 12) {
                    Text(
                        "... y ${monthlyMilestones.size - 12} hitos más",
                        fontSize = 11.sp,
                        color = DarkTextSecondary,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SummaryItem(label: String, value: Double, color: Color) {
    Column {
        Text(label, fontSize = 11.sp, color = DarkTextSecondary)
        Text(
            text = String.format(Locale.getDefault(), "$%,.2f", value),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

data class DayProjection(
    val day: Int,
    val totalBalance: Double,
    val sourceBalances: Map<Int, Double>,
    val dailyYield: Double
)

fun calculateProjection(
    sources: List<IncomeSource>, 
    days: Int, 
    monthlyContribution: Double = 0.0,
    annualInflation: Double = 0.0
): List<DayProjection> {
    val projection = mutableListOf<DayProjection>()
    val currentBalances = sources.associate { it.id to it.balance }.toMutableMap()
    
    val dailyInflationRate = annualInflation / 100.0 / 365.0
    
    // Step calculation to avoid thousands of points on long projections
    val step = when {
        days <= 365 -> 1
        days <= 1825 -> 7  // Weekly for 5 years
        else -> 30        // Monthly for 10+ years
    }

    for (day in 1..days) {
        // Add monthly contribution distributed proportionally
        if (day > 1 && day % 30 == 0) {
            val totalCurrent = currentBalances.values.sum()
            if (totalCurrent > 0) {
                sources.forEach { source ->
                    val proportion = (currentBalances[source.id] ?: 0.0) / totalCurrent
                    currentBalances[source.id] = (currentBalances[source.id] ?: 0.0) + (monthlyContribution * proportion)
                }
            } else if (sources.isNotEmpty()) {
                currentBalances[sources[0].id] = (currentBalances[sources[0].id] ?: 0.0) + monthlyContribution
            }
        }

        val totalSourceBalances = mutableMapOf<Int, Double>()
        var dailyYieldTotal = 0.0
        
        // Calculate nominal yields and update balances
        for (source in sources) {
            val balance = currentBalances[source.id] ?: 0.0
            val tempSource = source.copy(balance = balance)
            
            val y1 = tempSource.balance1 * (source.rate1 / 365.0)
            val y2 = if (source.hasTier2) tempSource.balance2 * (source.rate2 / 365.0) else 0.0
            val y3 = if (source.hasTier3) tempSource.balance3 * (source.rate3 / 365.0) else 0.0
            
            val dayYield = y1 + y2 + y3
            dailyYieldTotal += dayYield
            
            val newBalance = balance + dayYield
            currentBalances[source.id] = newBalance
        }

        // Apply inflation adjustment (Real Value)
        val inflationFactor = Math.pow(1.0 + dailyInflationRate, day.toDouble())
        val adjustedBalances = currentBalances.mapValues { it.value / inflationFactor }
        val adjustedDailyYieldTotal = dailyYieldTotal / inflationFactor
        
        if (day == 1 || day == days || day % step == 0) {
            projection.add(DayProjection(
                day = day, 
                totalBalance = adjustedBalances.values.sum(), 
                sourceBalances = adjustedBalances, 
                dailyYield = adjustedDailyYieldTotal
            ))
        }
    }
    return projection
}

/**
 * Calculates approximately how many days it will take to reach a monthly yield target.
 * Simulates up to 30 years (10,950 days).
 */
fun findFreedomDay(
    sources: List<IncomeSource>,
    targetMonthlyIncome: Double,
    monthlyContribution: Double
): Int? {
    if (sources.isEmpty() || targetMonthlyIncome <= 0) return null
    
    val currentBalances = sources.associate { it.id to it.balance }.toMutableMap()
    val maxDays = 10950 // 30 years limit
    
    // Check if already reached
    val initialDailyYield = sources.sumOf { source ->
        val y1 = source.balance1 * (source.rate1 / 365.0)
        val y2 = if (source.hasTier2) source.balance2 * (source.rate2 / 365.0) else 0.0
        val y3 = if (source.hasTier3) source.balance3 * (source.rate3 / 365.0) else 0.0
        y1 + y2 + y3
    }
    if (initialDailyYield * 30 >= targetMonthlyIncome) return 0

    for (day in 1..maxDays) {
        // Add monthly contribution
        if (day % 30 == 0) {
            val totalCurrent = currentBalances.values.sum()
            if (totalCurrent > 0) {
                sources.forEach { source ->
                    val proportion = (currentBalances[source.id] ?: 0.0) / totalCurrent
                    currentBalances[source.id] = (currentBalances[source.id] ?: 0.0) + (monthlyContribution * proportion)
                }
            } else {
                currentBalances[sources[0].id] = (currentBalances[sources[0].id] ?: 0.0) + monthlyContribution
            }
        }

        var dailyYieldTotal = 0.0
        for (source in sources) {
            val balance = currentBalances[source.id] ?: 0.0
            val tempSource = source.copy(balance = balance)
            
            val y1 = tempSource.balance1 * (source.rate1 / 365.0)
            val y2 = if (source.hasTier2) tempSource.balance2 * (source.rate2 / 365.0) else 0.0
            val y3 = if (source.hasTier3) tempSource.balance3 * (source.rate3 / 365.0) else 0.0
            
            val dayYield = y1 + y2 + y3
            dailyYieldTotal += dayYield
            currentBalances[source.id] = balance + dayYield
        }

        if (dailyYieldTotal * 30 >= targetMonthlyIncome) {
            return day
        }
    }
    
    return maxDays // Return max if not reached within 30 years
}

@Composable
fun ProjectionChart(
    projection: List<DayProjection>,
    onPointSelected: (DayProjection?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (projection.isEmpty()) return

    val textMeasurer = rememberTextMeasurer()
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    
    LaunchedEffect(selectedPointIndex) {
        onPointSelected(selectedPointIndex?.let { projection[it] })
    }
    
    val labelStyle = TextStyle(
        color = DarkTextSecondary.copy(alpha = 0.8f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium
    )

    val primaryColor = Color(0xFF00BCD4) // Teal / Verde azulado

    Canvas(
        modifier = modifier
            .pointerInput(projection) {
                detectTapGestures { offset ->
                    val paddingLeft = 12.dp.toPx()
                    val paddingRight = 50.dp.toPx()
                    val chartWidth = size.width - paddingRight - paddingLeft
                    
                    val x = offset.x - paddingLeft
                    if (x in 0f..chartWidth) {
                        val index = (x / chartWidth * (projection.size - 1)).toInt().coerceIn(0, projection.size - 1)
                        selectedPointIndex = index
                    } else {
                        selectedPointIndex = null
                    }
                }
            }
            .pointerInput(projection) {
                detectDragGestures(
                    onDragEnd = { selectedPointIndex = null },
                    onDragCancel = { selectedPointIndex = null }
                ) { change, _ ->
                    val paddingLeft = 12.dp.toPx()
                    val paddingRight = 50.dp.toPx()
                    val chartWidth = size.width - paddingRight - paddingLeft
                    
                    val x = change.position.x - paddingLeft
                    if (x in 0f..chartWidth) {
                        val index = (x / chartWidth * (projection.size - 1)).toInt().coerceIn(0, projection.size - 1)
                        selectedPointIndex = index
                    }
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val paddingLeft = 12.dp.toPx()
        val paddingRight = 50.dp.toPx()
        val paddingTop = 10.dp.toPx()
        val paddingBottom = 30.dp.toPx()
        
        val chartWidth = width - paddingRight - paddingLeft
        val chartHeight = height - paddingBottom - paddingTop

        val maxVal = projection.maxOf { it.totalBalance } * 1.05f
        val minVal = projection.minOf { it.totalBalance } * 0.95f
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
        
        // Tooltip interaction
        selectedPointIndex?.let { index ->
            val dayData = projection[index]
            val x = paddingLeft + (index.toFloat() / (projection.size - 1) * chartWidth)
            val y = paddingTop + chartHeight - ((dayData.totalBalance.toFloat() - minVal.toFloat()) / range.toFloat() * chartHeight)
            
            // Vertical selection line
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(x, paddingTop),
                end = Offset(x, paddingTop + chartHeight),
                strokeWidth = 1.dp.toPx()
            )
            
            // Highlight point
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = Offset(x, y)
            )
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx(),
                center = Offset(x, y)
            )
            
            // Tooltip text
            val tooltipText = String.format(Locale.getDefault(), "Día %d: $%,.2f", dayData.day, dayData.totalBalance)
            val tooltipStyle = labelStyle.copy(color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            val textResult = textMeasurer.measure(tooltipText, tooltipStyle)
            
            val tooltipX = (x + 8.dp.toPx()).coerceAtMost(width - textResult.size.width - 8.dp.toPx())
            val tooltipY = (y - textResult.size.height - 8.dp.toPx()).coerceAtLeast(paddingTop)
            
            drawRect(
                color = Color(0xFF1E293B).copy(alpha = 0.9f),
                topLeft = Offset(tooltipX - 4.dp.toPx(), tooltipY - 4.dp.toPx()),
                size = Size(textResult.size.width + 8.dp.toPx(), textResult.size.height + 8.dp.toPx())
            )
            
            drawText(
                textMeasurer = textMeasurer,
                text = tooltipText,
                topLeft = Offset(tooltipX, tooltipY),
                style = tooltipStyle
            )
        }
        
        // Final point dot (only if not selected)
        if (selectedPointIndex == null) {
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
}
