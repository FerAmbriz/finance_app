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
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddSourceDialog(
    onDismiss: () -> Unit,
    onConfirm: (IncomeSource) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("SOFIPO") }
    
    var balance1Str by remember { mutableStateOf("") }
    var rate1Str by remember { mutableStateOf("") }
    
    var hasTier2 by remember { mutableStateOf(false) }
    var balance2Str by remember { mutableStateOf("") }
    var rate2Str by remember { mutableStateOf("") }
    
    var hasTier3 by remember { mutableStateOf(false) }
    var balance3Str by remember { mutableStateOf("") }
    var rate3Str by remember { mutableStateOf("") }

    var selectedColorHex by remember { mutableStateOf(AccentColors.first()) }

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
                    text = "Añadir Nuevo Activo",
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

                // Type Dropdown
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
                            Text(text = "▼", color = DarkTextSecondary, modifier = Modifier.padding(end = 12.dp))
                        }
                    )
                    Box(modifier = Modifier.matchParentSize().clickable { dropdownExpanded = true })
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(DarkSurface)
                    ) {
                        types.forEach { t ->
                            DropdownMenuItem(text = { Text(t, color = Color.White) }, onClick = { type = t; dropdownExpanded = false })
                        }
                    }
                }

                // Tier 1
                Text("Nivel 1 (Base)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B82F6))
                OutlinedTextField(
                    value = balance1Str,
                    onValueChange = { balance1Str = it },
                    label = { Text("Monto ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rate1Str,
                    onValueChange = { rate1Str = it },
                    label = { Text("Tasa Anual (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Tier 2
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hasTier2, onCheckedChange = { hasTier2 = it })
                    Text("Añadir Nivel 2", color = Color.White, fontSize = 14.sp)
                }
                if (hasTier2) {
                    OutlinedTextField(
                        value = balance2Str,
                        onValueChange = { balance2Str = it },
                        label = { Text("Monto Nivel 2 ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = rate2Str,
                        onValueChange = { rate2Str = it },
                        label = { Text("Tasa Nivel 2 (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Tier 3
                if (hasTier2) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = hasTier3, onCheckedChange = { hasTier3 = it })
                        Text("Añadir Nivel 3", color = Color.White, fontSize = 14.sp)
                    }
                    if (hasTier3) {
                        OutlinedTextField(
                            value = balance3Str,
                            onValueChange = { balance3Str = it },
                            label = { Text("Monto Nivel 3 ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = rate3Str,
                            onValueChange = { rate3Str = it },
                            label = { Text("Tasa Nivel 3 (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(onClick = {
                        if (name.isNotBlank()) {
                            val b1 = balance1Str.toDoubleOrNull() ?: 0.0
                            val b2 = if (hasTier2) (balance2Str.toDoubleOrNull() ?: 0.0) else 0.0
                            val b3 = if (hasTier3) (balance3Str.toDoubleOrNull() ?: 0.0) else 0.0

                            onConfirm(IncomeSource(
                                name = name,
                                type = type,
                                colorHex = selectedColorHex,
                                balance = b1 + b2 + b3,
                                annualRate = (rate1Str.toDoubleOrNull() ?: 0.0) / 100.0,
                                hasLimit = hasTier2,
                                limitAmount = b1,
                                hasSecondaryRate = hasTier2,
                                secondaryRate = (rate2Str.toDoubleOrNull() ?: 0.0) / 100.0,
                                hasTertiaryRate = hasTier3,
                                limitAmount2 = b1 + b2,
                                tertiaryRate = (rate3Str.toDoubleOrNull() ?: 0.0) / 100.0
                            ))
                        }
                    }) { Text("Guardar") }
                }
            }
        }
    }
}
