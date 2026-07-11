package com.passiveincome.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddYieldDialog(
    source: IncomeSource,
    onDismiss: () -> Unit,
    onConfirm: (Double, Double, String) -> Unit // returns: (yieldAmount, rateApplied, description)
) {
    var rateStr by remember { mutableStateOf(String.format(Locale.US, "%.2f", source.annualRate * 100)) }
    var period by remember { mutableStateOf("Mensual") }
    val periods = listOf("Mensual", "Anual", "Diario")
    var dropdownExpanded by remember { mutableStateOf(false) }

    val rate = (rateStr.toDoubleOrNull() ?: 0.0) / 100.0
    val yieldAmount = when (period) {
        "Anual" -> source.balance * rate
        "Mensual" -> source.balance * (rate / 12.0)
        "Diario" -> source.balance * (rate / 365.0)
        else -> 0.0
    }

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
                    text = "Añadir Rendimiento",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = "Aplica rendimiento a la cuenta de ${source.name} con base en una tasa y período definibles.",
                    fontSize = 13.sp,
                    color = DarkTextSecondary
                )

                // Current balance info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Saldo Actual:", color = DarkTextSecondary, fontSize = 14.sp)
                    Text(
                        text = String.format(Locale.getDefault(), "$%,.2f", source.balance),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Rate input
                OutlinedTextField(
                    value = rateStr,
                    onValueChange = { rateStr = it },
                    label = { Text("Tasa de Rendimiento (%)") },
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

                // Period dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = period,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Período") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = BorderColor,
                            focusedLabelColor = Color(0xFF6366F1),
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
                        periods.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p, color = Color.White) },
                                onClick = {
                                    period = p
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Real-time simulated yield amount info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF6366F1).copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF6366F1).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Rendimiento a sumar:", color = DarkTextSecondary, fontSize = 14.sp)
                    Text(
                        text = String.format(Locale.getDefault(), "+$%,.2f", yieldAmount),
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
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
                            if (yieldAmount > 0.0) {
                                val desc = String.format(
                                    Locale.getDefault(),
                                    "Rendimiento %s (Tasa: %.2f%%)",
                                    period.lowercase(Locale.getDefault()),
                                    rate * 100
                                )
                                onConfirm(yieldAmount, rate, desc)
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1),
                            contentColor = Color.White
                        ),
                        enabled = yieldAmount > 0.0
                    ) {
                        Text("Aplicar", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
