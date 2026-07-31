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
import java.text.SimpleDateFormat
import java.util.*
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

    val last3MonthsData = remember(movements) {
        val cal = Calendar.getInstance()
        val results = mutableListOf<Triple<String, Double, Double>>() // Month, Incomes, Expenses
        
        for (i in 0 until 3) {
            val m = cal.get(Calendar.MONTH)
            val y = cal.get(Calendar.YEAR)
            val monthLabel = SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time)
            
            val monthMovements = movements.filter {
                val mCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
                mCal.get(Calendar.MONTH) == m && mCal.get(Calendar.YEAR) == y
            }
            
            val incomes = monthMovements.filter { it.amount > 0 && it.type != "Cierre Mensual" }.sumOf { it.amount }
            val expenses = monthMovements.filter { it.amount < 0 && it.type != "Cierre Mensual" }.sumOf { kotlin.math.abs(it.amount) }
            
            results.add(Triple(monthLabel, incomes, expenses))
            cal.add(Calendar.MONTH, -1)
        }
        results.reversed()
    }

    val currentMonthMovements = remember(movements) {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)
        
        movements.filter {
            val mCal = Calendar.getInstance().apply { timeInMillis = it.timestamp }
            mCal.get(Calendar.MONTH) == currentMonth && mCal.get(Calendar.YEAR) == currentYear
        }
    }

    val monthlyEntradas = currentMonthMovements.filter { it.amount > 0 && it.type != "Cierre Mensual" }.sumOf { it.amount }
    val monthlySalidas = currentMonthMovements.filter { it.amount < 0 && it.type != "Cierre Mensual" }.sumOf { kotlin.math.abs(it.amount) }
    val netoMes = monthlyEntradas - monthlySalidas

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            if (currentView == "Dashboard") {
                ThinkingOrbsHero(
                    title = "Balance",
                    subtitle = "Patrimonio Total",
                    statusText = String.format(Locale.getDefault(), "+$%,.2f diario", dailyYield),
                    height = 200.dp,
                    titleFontSize = 36.sp,
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
                    height = 200.dp,
                    titleFontSize = 36.sp
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
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkSurface, RoundedCornerShape(16.dp))
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    SummaryItem("R. Diario", dailyYield, Color(0xFF10B981))
                                    SummaryItem("R. Mensual", dailyYield * 30, Color(0xFF10B981))
                                    SummaryItem("R. Anual", dailyYield * 365, Color(0xFF10B981))
                                }
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
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Text("Balance Total Actual", fontSize = 14.sp, color = DarkTextSecondary)
                                        Text(
                                            text = String.format(Locale.getDefault(), "$%,.2f", totalBalance),
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Divider(color = Color.White.copy(alpha = 0.1f))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            SummaryItem("Entradas", monthlyEntradas, Color(0xFF10B981))
                                            SummaryItem("Salidas", monthlySalidas, Color(0xFFEF4444))
                                            SummaryItem("Neto Mes", netoMes, if (netoMes >= 0) Color(0xFF10B981) else Color(0xFFEF4444))
                                        }
                                    }
                                }
                            }

                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Ingresos vs Gastos (3 Meses)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        SimpleBarChart(data = last3MonthsData)
                                    }
                                }
                            }

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
fun SimpleBarChart(data: List<Triple<String, Double, Double>>) {
    val maxVal = data.flatMap { listOf(it.second, it.third) }.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { (month, income, expense) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .fillMaxHeight((income / maxVal).toFloat().coerceIn(0.01f, 1f))
                            .background(Color(0xFF10B981), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    )
                    Box(
                        modifier = Modifier
                            .width(12.dp)
                            .fillMaxHeight((expense / maxVal).toFloat().coerceIn(0.01f, 1f))
                            .background(Color(0xFFEF4444), RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(month, fontSize = 10.sp, color = DarkTextSecondary)
            }
        }
    }
}
