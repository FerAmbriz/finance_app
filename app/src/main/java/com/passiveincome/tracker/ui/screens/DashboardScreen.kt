package com.passiveincome.tracker.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passiveincome.tracker.data.IncomeSource
import com.passiveincome.tracker.ui.components.AddSourceDialog
import com.passiveincome.tracker.ui.components.AddYieldDialog
import com.passiveincome.tracker.ui.components.DonutChart
import com.passiveincome.tracker.ui.components.EditSourceDialog
import com.passiveincome.tracker.ui.components.IncomeSourceCard
import com.passiveincome.tracker.ui.components.MovementRow
import com.passiveincome.tracker.ui.components.TransactionDialog
import com.passiveincome.tracker.ui.theme.DarkBackground
import com.passiveincome.tracker.ui.theme.DarkTextSecondary
import com.passiveincome.tracker.viewmodel.IncomeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: IncomeViewModel,
    modifier: Modifier = Modifier
) {
    val sources by viewModel.allSources.collectAsState()
    val movements by viewModel.allMovements.collectAsState()

    var showAddSourceDialog by remember { mutableStateOf(false) }
    var sourceForYieldDialog by remember { mutableStateOf<IncomeSource?>(null) }
    var sourceForTransactionDialog by remember { mutableStateOf<IncomeSource?>(null) }
    var sourceForEditDialog by remember { mutableStateOf<IncomeSource?>(null) }

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Activos", "Historial")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "PasivTrack",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = { showAddSourceDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Añadir Activo",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground
                )
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = { showAddSourceDialog = true },
                    containerColor = Color(0xFF6366F1),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Añadir Activo")
                }
            }
        },
        containerColor = DarkBackground,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            // Tab Selector
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = DarkBackground,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = Color(0xFF6366F1)
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (index == 0) Icons.Default.PieChart else Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (selectedTabIndex == index) Color.White else DarkTextSecondary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) Color.White else DarkTextSecondary
                                )
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTabIndex) {
                0 -> {
                    // Summary and Source List
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    DonutChart(sources = sources)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    val totalDailyYield = sources.sumOf { (it.balance * it.annualRate) / 365.0 }
                                    Text(
                                        text = "Rendimiento Diario Estimado",
                                        fontSize = 12.sp,
                                        color = DarkTextSecondary
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), "$%,.2f", totalDailyYield),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }
                        }

                        if (sources.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 64.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Aún no tienes activos agregados",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Pulsa + para añadir una SOFIPO o cuenta bancaria.",
                                        fontSize = 12.sp,
                                        color = DarkTextSecondary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        } else {
                            val totalBalance = sources.sumOf { it.balance }
                            items(sources, key = { it.id }) { source ->
                                IncomeSourceCard(
                                    source = source,
                                    totalBalance = totalBalance,
                                    onAddYieldClick = { sourceForYieldDialog = source },
                                    onTransactClick = { sourceForTransactionDialog = source },
                                    onDeleteClick = { viewModel.deleteSource(source) },
                                    onEditClick = { sourceForEditDialog = source }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(72.dp))
                        }
                    }
                }
                1 -> {
                    // History
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        if (movements.isEmpty()) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 64.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Sin movimientos registrados",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Los depósitos, retiros y rendimientos aparecerán aquí.",
                                        fontSize = 12.sp,
                                        color = DarkTextSecondary,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        } else {
                            items(movements, key = { it.id }) { movement ->
                                MovementRow(movement = movement)
                            }
                        }
                    }
                }
            }
        }

        // Dialogs management
        if (showAddSourceDialog) {
            AddSourceDialog(
                onDismiss = { showAddSourceDialog = false },
                onConfirm = { newSource ->
                    viewModel.insertSource(newSource)
                    showAddSourceDialog = false
                }
            )
        }

        sourceForYieldDialog?.let { source ->
            AddYieldDialog(
                source = source,
                onDismiss = { sourceForYieldDialog = null },
                onConfirm = { yieldAmount, rateApplied, description ->
                    viewModel.addYield(source, yieldAmount, rateApplied, description)
                    sourceForYieldDialog = null
                }
            )
        }

        sourceForTransactionDialog?.let { source ->
            TransactionDialog(
                source = source,
                onDismiss = { sourceForTransactionDialog = null },
                onConfirm = { amount, type, description ->
                    viewModel.transact(source, amount, type, description)
                    sourceForTransactionDialog = null
                }
            )
        }

        sourceForEditDialog?.let { source ->
            EditSourceDialog(
                source = source,
                onDismiss = { sourceForEditDialog = null },
                onConfirm = { updatedSource ->
                    viewModel.updateSource(updatedSource)
                    sourceForEditDialog = null
                }
            )
        }
    }
}
