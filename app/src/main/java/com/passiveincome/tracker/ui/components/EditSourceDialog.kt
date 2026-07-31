package com.passiveincome.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.passiveincome.tracker.data.IncomeSource
import com.passiveincome.tracker.ui.theme.AccentColors
import com.passiveincome.tracker.ui.theme.BorderColor
import com.passiveincome.tracker.ui.theme.DarkSurface
import com.passiveincome.tracker.ui.theme.DarkTextSecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditSourceDialog(
    source: IncomeSource,
    onDismiss: () -> Unit,
    onConfirm: (IncomeSource) -> Unit
) {
    var name by remember { mutableStateOf(source.name) }
    var type by remember { mutableStateOf(source.type) }
    var balanceStr by remember { mutableStateOf(String.format(Locale.US, "%.2f", source.balance)) }
    var rateStr by remember { mutableStateOf(String.format(Locale.US, "%.2f", source.annualRate * 100)) }
    
    var hasLimit by remember { mutableStateOf(source.hasLimit) }
    var limitAmountStr by remember { mutableStateOf(if (source.limitAmount > 0) String.format(Locale.US, "%.2f", source.limitAmount) else "") }
    
    var hasSecondaryRate by remember { mutableStateOf(source.hasSecondaryRate) }
    var secondaryRateStr by remember { mutableStateOf(if (source.secondaryRate > 0) String.format(Locale.US, "%.2f", source.secondaryRate * 100) else "") }
    
    var hasTertiaryRate by remember { mutableStateOf(source.hasTertiaryRate) }
    var limitAmount2Str by remember { mutableStateOf(if (source.limitAmount2 > 0) String.format(Locale.US, "%.2f", source.limitAmount2) else "") }
    var tertiaryRateStr by remember { mutableStateOf(if (source.tertiaryRate > 0) String.format(Locale.US, "%.2f", source.tertiaryRate * 100) else "") }
    
    var hasHardCap by remember { mutableStateOf(source.hasHardCap) }
    var hardCapAmountStr by remember { mutableStateOf(if (source.hardCapAmount > 0) String.format(Locale.US, "%.2f", source.hardCapAmount) else "") }

    var selectedColorHex by remember { mutableStateOf(source.colorHex) }

    val types = listOf("SOFIPO", "Banco", "Cetes", "Otro")
    var dropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Editar Activo",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Color Selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Color:", color = DarkTextSecondary, fontSize = 14.sp)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AccentColors.forEach { colorHex ->
                            val color = Color(android.graphics.Color.parseColor(colorHex))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color)
                                    .border(
                                        width = if (selectedColorHex == colorHex) 2.dp else 0.dp,
                                        color = Color.White,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedColorHex = colorHex }
                            )
                        }
                    }
                }

                // Current Tier Breakdown
                val currentBalance = balanceStr.toDoubleOrNull() ?: 0.0
                val limit1 = limitAmountStr.toDoubleOrNull() ?: 0.0
                val limit2 = limitAmount2Str.toDoubleOrNull() ?: 0.0
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Distribución por Tasa", fontSize = 12.sp, color = DarkTextSecondary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Tier 1
                        val t1Amount = if (hasLimit) minOf(currentBalance, limit1) else currentBalance
                        TierInfoRow("Tier 1 (${rateStr}%)", t1Amount)
                        
                        if (hasLimit && hasSecondaryRate) {
                            // Tier 2
                            val l2 = if (hasTertiaryRate) limit2 else Double.MAX_VALUE
                            val t2Amount = minOf(maxOf(0.0, currentBalance - limit1), l2 - limit1)
                            TierInfoRow("Tier 2 (${secondaryRateStr}%)", t2Amount)
                            
                            if (hasTertiaryRate) {
                                // Tier 3
                                val t3Amount = maxOf(0.0, currentBalance - limit2)
                                TierInfoRow("Tier 3 (${tertiaryRateStr}%)", t3Amount)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = Color(0xFF3B82F6),
                        unfocusedLabelColor = DarkTextSecondary
                    )
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de Activo") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = Color(0xFF3B82F6),
                            unfocusedLabelColor = DarkTextSecondary
                        ),
                        trailingIcon = {
                            Text(
                                text = "▼",
                                modifier = Modifier.padding(end = 12.dp),
                                color = DarkTextSecondary
                            )
                        }
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { dropdownExpanded = true }
                    )

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(DarkSurface)
                    ) {
                        types.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t, color = Color.White) },
                                onClick = {
                                    type = t
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = balanceStr,
                    onValueChange = { balanceStr = it },
                    label = { Text("Monto Actual ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = Color(0xFF3B82F6),
                        unfocusedLabelColor = DarkTextSecondary
                    )
                )

                OutlinedTextField(
                    value = rateStr,
                    onValueChange = { rateStr = it },
                    label = { Text("Tasa Anual de Rendimiento (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = Color(0xFF3B82F6),
                        unfocusedLabelColor = DarkTextSecondary
                    )
                )

                // Limit Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasLimit,
                        onCheckedChange = { hasLimit = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6), uncheckedColor = DarkTextSecondary)
                    )
                    Text("Tiene Límite de Saldo", color = Color.White, fontSize = 14.sp)
                }

                if (hasLimit) {
                    OutlinedTextField(
                        value = limitAmountStr,
                        onValueChange = { limitAmountStr = it },
                        label = { Text("Monto Límite ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = Color(0xFF3B82F6),
                            unfocusedLabelColor = DarkTextSecondary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = hasSecondaryRate,
                            onCheckedChange = { hasSecondaryRate = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6), uncheckedColor = DarkTextSecondary)
                        )
                        Text("Aplicar Segunda Tasa (al excedente)", color = Color.White, fontSize = 14.sp)
                    }

                    if (hasSecondaryRate) {
                        OutlinedTextField(
                            value = secondaryRateStr,
                            onValueChange = { secondaryRateStr = it },
                            label = { Text("Segunda Tasa Anual (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF3B82F6),
                                unfocusedBorderColor = BorderColor,
                                focusedLabelColor = Color(0xFF3B82F6),
                                unfocusedLabelColor = DarkTextSecondary
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = hasTertiaryRate,
                                onCheckedChange = { hasTertiaryRate = it },
                                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6), uncheckedColor = DarkTextSecondary)
                            )
                            Text("Añadir Tercer Nivel (Tier 3)", color = Color.White, fontSize = 14.sp)
                        }

                        if (hasTertiaryRate) {
                            OutlinedTextField(
                                value = limitAmount2Str,
                                onValueChange = { limitAmount2Str = it },
                                label = { Text("Segundo Monto Límite ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = BorderColor,
                                    focusedLabelColor = Color(0xFF3B82F6),
                                    unfocusedLabelColor = DarkTextSecondary
                                )
                            )
                            OutlinedTextField(
                                value = tertiaryRateStr,
                                onValueChange = { tertiaryRateStr = it },
                                label = { Text("Tercera Tasa Anual (%)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF3B82F6),
                                    unfocusedBorderColor = BorderColor,
                                    focusedLabelColor = Color(0xFF3B82F6),
                                    unfocusedLabelColor = DarkTextSecondary
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = hasHardCap,
                        onCheckedChange = { hasHardCap = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF3B82F6), uncheckedColor = DarkTextSecondary)
                    )
                    Text("Límite Total de Depósito", color = Color.White, fontSize = 14.sp)
                }

                if (hasHardCap) {
                    OutlinedTextField(
                        value = hardCapAmountStr,
                        onValueChange = { hardCapAmountStr = it },
                        label = { Text("Capacidad Máxima ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF3B82F6),
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = Color(0xFF3B82F6),
                            unfocusedLabelColor = DarkTextSecondary
                        )
                    )
                }

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
                            val balance = balanceStr.toDoubleOrNull() ?: source.balance
                            val rate = (rateStr.toDoubleOrNull() ?: (source.annualRate * 100)) / 100.0
                            val limit = if (hasLimit) (limitAmountStr.toDoubleOrNull() ?: 0.0) else 0.0
                            val secondaryRate = if (hasLimit && hasSecondaryRate) (secondaryRateStr.toDoubleOrNull() ?: 0.0) / 100.0 else 0.0
                            val limit2 = if (hasLimit && hasSecondaryRate && hasTertiaryRate) (limitAmount2Str.toDoubleOrNull() ?: 0.0) else 0.0
                            val tertiaryRate = if (hasLimit && hasSecondaryRate && hasTertiaryRate) (tertiaryRateStr.toDoubleOrNull() ?: 0.0) / 100.0 else 0.0
                            val hardCap = if (hasHardCap) (hardCapAmountStr.toDoubleOrNull() ?: 0.0) else 0.0

                            if (name.isNotBlank()) {
                                onConfirm(
                                    source.copy(
                                        name = name,
                                        type = type,
                                        colorHex = selectedColorHex,
                                        balance = balance,
                                        annualRate = rate,
                                        hasLimit = hasLimit,
                                        limitAmount = limit,
                                        hasSecondaryRate = hasSecondaryRate,
                                        secondaryRate = secondaryRate,
                                        hasTertiaryRate = hasTertiaryRate,
                                        limitAmount2 = limit2,
                                        tertiaryRate = tertiaryRate,
                                        hasHardCap = hasHardCap,
                                        hardCapAmount = hardCap
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Actualizar", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun TierInfoRow(label: String, amount: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color.White)
        Text(
            text = String.format(Locale.getDefault(), "$%,.2f", amount),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (amount > 0) Color(0xFF10B981) else DarkTextSecondary
        )
    }
}
