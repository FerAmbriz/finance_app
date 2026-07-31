package com.passiveincome.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.passiveincome.tracker.data.IncomeSource
import com.passiveincome.tracker.ui.theme.BorderColor
import com.passiveincome.tracker.ui.theme.DarkSurface
import com.passiveincome.tracker.ui.theme.DarkTextSecondary

@Composable
fun TransactionDialog(
    source: IncomeSource,
    allSources: List<IncomeSource> = emptyList(),
    onDismiss: () -> Unit,
    onConfirm: (Double, String, String) -> Unit,
    onTransfer: (IncomeSource, Double, String) -> Unit = { _, _, _ -> }
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Deposit, 1 = Withdrawal, 2 = Transfer
    val tabs = listOf("Depósito", "Retiro", "Transferencia")

    var amountStr by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    var destinationSource by remember { mutableStateOf<IncomeSource?>(null) }
    var destinationExpanded by remember { mutableStateOf(false) }

    val amount = amountStr.toDoubleOrNull() ?: 0.0
    val isTransfer = selectedTab == 2
    
    val isEnabled = amount > 0.0 && (
        selectedTab == 0 || 
        (selectedTab == 1 && source.totalBalance >= amount) ||
        (isTransfer && source.totalBalance >= amount && destinationSource != null)
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Añadir Movimiento",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = if (isTransfer) 
                        "Transfiere saldo de ${source.name} a otro activo." 
                        else "Registra un depósito o un retiro para ${source.name}.",
                    fontSize = 13.sp,
                    color = DarkTextSecondary
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF6366F1)
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) Color.White else DarkTextSecondary
                                )
                            }
                        )
                    }
                }

                if (isTransfer) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = destinationSource?.name ?: "Seleccionar destino",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Activo Destino") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = if (destinationSource == null) DarkTextSecondary else Color.White,
                                focusedBorderColor = Color(0xFF6366F1),
                                unfocusedBorderColor = BorderColor,
                                focusedLabelColor = Color(0xFF6366F1),
                                unfocusedLabelColor = DarkTextSecondary
                            ),
                            trailingIcon = {
                                Text(text = "▼", color = DarkTextSecondary, modifier = Modifier.padding(end = 12.dp))
                            }
                        )
                        Box(modifier = Modifier.matchParentSize().clickable { destinationExpanded = true })
                        DropdownMenu(
                            expanded = destinationExpanded,
                            onDismissRequest = { destinationExpanded = false },
                            modifier = Modifier.background(DarkSurface)
                        ) {
                            allSources.filter { it.id != source.id }.forEach { s ->
                                DropdownMenuItem(
                                    text = { Text(s.name, color = Color.White) }, 
                                    onClick = { 
                                        destinationSource = s
                                        destinationExpanded = false 
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Monto ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = Color(0xFF6366F1),
                        unfocusedLabelColor = DarkTextSecondary
                    )
                )

                if (selectedTab != 0 && amount > source.totalBalance) {
                    Text(
                        text = "Saldo insuficiente en origen.",
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = Color(0xFF6366F1),
                        unfocusedLabelColor = DarkTextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                    ) {
                        Text("Cancelar", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (isEnabled) {
                                if (isTransfer) {
                                    onTransfer(destinationSource!!, amount, description.ifBlank { "Transferencia entre cuentas" })
                                } else {
                                    val type = tabs[selectedTab]
                                    val defaultDesc = if (type == "Depósito") "Depósito manual" else "Retiro manual"
                                    val finalDesc = description.ifBlank { defaultDesc }
                                    val finalAmount = if (type == "Depósito") amount else -amount
                                    onConfirm(finalAmount, type, finalDesc)
                                }
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White
                        ),
                        enabled = isEnabled
                    ) {
                        Text("Confirmar", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
