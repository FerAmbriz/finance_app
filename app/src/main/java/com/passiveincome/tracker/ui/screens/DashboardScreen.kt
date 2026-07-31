package com.passiveincome.tracker.ui.screens

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.passiveincome.tracker.data.IncomeSource
import com.passiveincome.tracker.ui.components.*
import com.passiveincome.tracker.ui.theme.DarkBackground
import com.passiveincome.tracker.ui.theme.DarkSurface
import com.passiveincome.tracker.ui.theme.DarkTextSecondary
import com.passiveincome.tracker.viewmodel.IncomeViewModel
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: IncomeViewModel = viewModel()) {
    val sources by viewModel.allSources.collectAsState()
    val movements by viewModel.allMovements.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var sourceToAddYield by remember { mutableStateOf<IncomeSource?>(null) }
    var sourceToTransact by remember { mutableStateOf<IncomeSource?>(null) }
    var sourceToEdit by remember { mutableStateOf<IncomeSource?>(null) }
    
    var currentView by remember { mutableStateOf("Dashboard") }

    val totalBalance = sources.sumOf { it.totalBalance }
    val dailyYield = sources.sumOf { source ->
        val y1 = source.balance1 * (source.rate1 / 365.0)
        val y2 = if (source.hasTier2) source.balance2 * (source.rate2 / 365.0) else 0.0
        val y3 = if (source.hasTier3) source.balance3 * (source.rate3 / 365.0) else 0.0
        y1 + y2 + y3
    }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            if (currentView == "Dashboard") {
                GrowingHero(
                    title = String.format(Locale.getDefault(), "$%,.2f", totalBalance),
                    subtitle = "Patrimonio Total",
                    statusText = String.format(Locale.getDefault(), "+$%,.2f diario", dailyYield),
                    action = {
                        IconButton(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.1f), MaterialTheme.shapes.medium)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Source", tint = Color.White)
                        }
                    }
                )
            } else {
                val heroTitle = if (currentView == "Projections") "Proyecciones" else "Actividad"
                val heroSubtitle = if (currentView == "Projections") "Crecimiento estimado a futuro" else "Historial de movimientos"
                
                ThinkingOrbsHero(
                    title = heroTitle,
                    subtitle = heroSubtitle,
                    height = 160.dp,
                    titleFontSize = 32.sp
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                when (currentView) {
                    "Dashboard" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                DonutChart(
                                    sources = sources,
                                    modifier = Modifier.fillMaxWidth().height(220.dp).padding(vertical = 8.dp)
                                )
                            }
                            
                            item {
                                Text(
                                    "Tus Activos",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                                )
                            }

                            items(sources, key = { it.id }) { source ->
                                IncomeSourceCard(
                                    source = source,
                                    totalBalance = totalBalance,
                                    onAddYieldClick = { sourceToAddYield = source },
                                    onTransactClick = { sourceToTransact = source },
                                    onDeleteClick = { viewModel.deleteSource(source) },
                                    onEditClick = { sourceToEdit = source }
                                )
                            }
                            
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                    "Projections" -> {
                        ProjectionScreen(sources = sources)
                    }
                    "History" -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(movements) { movement ->
                                MovementRow(movement = movement)
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).padding(24.dp),
            color = DarkSurface.copy(alpha = 0.95f),
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 8.dp,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val navItems = listOf(
                    Triple("Dashboard", Icons.Default.Add, "Inicio"),
                    Triple("History", Icons.Default.History, "Historial"),
                    Triple("Projections", Icons.Default.Analytics, "Proyectar")
                )
                
                navItems.forEach { (view, icon, label) ->
                    val selected = currentView == view
                    FilterChip(
                        selected = selected,
                        onClick = { currentView = view },
                        label = { Text(label) },
                        leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF6366F1),
                            selectedLabelColor = Color.White,
                            containerColor = Color.Transparent,
                            labelColor = DarkTextSecondary
                        ),
                        border = null
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddSourceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { 
                viewModel.insertSource(it)
                showAddDialog = false
            }
        )
    }

    sourceToAddYield?.let { source ->
        AddYieldDialog(
            source = source,
            onDismiss = { sourceToAddYield = null },
            onConfirm = { amount, rate, desc ->
                viewModel.addYield(source, amount, rate, desc)
                sourceToAddYield = null
            }
        )
    }

    sourceToTransact?.let { source ->
        TransactionDialog(
            source = source,
            allSources = sources,
            onDismiss = { sourceToTransact = null },
            onConfirm = { amount, type, desc ->
                viewModel.transact(source, amount, type, desc)
                sourceToTransact = null
            },
            onTransfer = { to, amount, desc ->
                viewModel.transfer(source, to, amount, desc)
                sourceToTransact = null
            }
        )
    }

    sourceToEdit?.let { source ->
        EditSourceDialog(
            source = source,
            onDismiss = { sourceToEdit = null },
            onConfirm = { updated ->
                viewModel.updateSource(updated)
                sourceToEdit = null
            }
        )
    }
}

@Composable
fun GrowingHero(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    statusText: String = "Growing...",
    height: Dp = 220.dp,
    titleFontSize: TextUnit = 36.sp,
    action: (@Composable () -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2f).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.CenterStart
    ) {
        val canvasModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.fillMaxSize().blur(32.dp)
        } else {
            Modifier.fillMaxSize()
        }

        Canvas(modifier = canvasModifier) {
            val width = size.width
            val heightPx = size.height

            drawOrb(
                center = Offset(
                    width * 0.2f + 60f * sin(time),
                    heightPx * 0.4f + 40f * cos(time * 0.8f)
                ),
                radius = (heightPx * 0.8f) + 30f * sin(time * 1.2f),
                color = Color(0xFF10B981).copy(alpha = 0.3f)
            )

            drawOrb(
                center = Offset(
                    width * 0.7f + 50f * cos(time * 0.7f),
                    heightPx * 0.6f + 50f * sin(time * 1.5f)
                ),
                radius = (heightPx * 1.0f) + 40f * cos(time),
                color = Color(0xFF06B6D4).copy(alpha = 0.25f)
            )

            drawOrb(
                center = Offset(
                    width * 0.5f + 120f * sin(time * 0.5f),
                    heightPx * 0.3f + 30f * cos(time * 1.1f)
                ),
                radius = (heightPx * 0.7f) + 25f * sin(time),
                color = Color(0xFF6366F1).copy(alpha = 0.2f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            GrowingOrbStatus(text = statusText)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-0.5).sp,
                        lineHeight = (titleFontSize.value * 1.1).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = DarkTextSecondary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.2.sp
                    )
                }
                action?.invoke()
            }
        }
    }
}
