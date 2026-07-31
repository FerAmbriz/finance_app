package com.passiveincome.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passiveincome.tracker.data.IncomeSource
import com.passiveincome.tracker.ui.theme.BorderColor
import com.passiveincome.tracker.ui.theme.DarkTextSecondary
import java.util.Locale

@Composable
fun IncomeSourceCard(
    source: IncomeSource,
    totalBalance: Double,
    onAddYieldClick: () -> Unit,
    onTransactClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val percentage = if (totalBalance > 0.0) {
        (source.totalBalance / totalBalance) * 100
    } else {
        0.0
    }

    val sourceColor = try {
        Color(android.graphics.Color.parseColor(source.colorHex))
    } catch (e: Exception) {
        Color.Gray
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = com.passiveincome.tracker.ui.theme.DarkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(sourceColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = source.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = source.type,
                            fontSize = 12.sp,
                            color = DarkTextSecondary
                        )
                        
                        // Tier Breakdown
                        Text(
                            text = String.format(Locale.getDefault(), "Tier 1: $%,.0f (%.1f%%)", source.balance1, source.rate1 * 100),
                            fontSize = 10.sp,
                            color = if (source.balance1 > 0) Color.White else DarkTextSecondary
                        )
                        if (source.hasTier2) {
                            Text(
                                text = String.format(Locale.getDefault(), "Tier 2: $%,.0f (%.1f%%)", source.balance2, source.rate2 * 100),
                                fontSize = 10.sp,
                                color = if (source.balance2 > 0) Color.White else DarkTextSecondary
                            )
                        }
                        if (source.hasTier3) {
                            Text(
                                text = String.format(Locale.getDefault(), "Tier 3: $%,.0f (%.1f%%)", source.balance3, source.rate3 * 100),
                                fontSize = 10.sp,
                                color = if (source.balance3 > 0) Color.White else DarkTextSecondary
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = DarkTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Body info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Monto Total",
                        fontSize = 11.sp,
                        color = DarkTextSecondary
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "$%,.2f", source.totalBalance),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    val dailyYield = (source.balance1 * source.rate1 + 
                                     (if (source.hasTier2) source.balance2 * source.rate2 else 0.0) +
                                     (if (source.hasTier3) source.balance3 * source.rate3 else 0.0)) / 365.0
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "$%,.2f", dailyYield),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = " /día",
                            fontSize = 11.sp,
                            color = DarkTextSecondary
                        )
                    }
                    Text(
                        text = String.format(Locale.getDefault(), "$%,.2f /mes", dailyYield * 30),
                        fontSize = 11.sp,
                        color = Color(0xFF10B981).copy(alpha = 0.9f)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "$%,.2f /año", dailyYield * 365),
                        fontSize = 11.sp,
                        color = Color(0xFF10B981).copy(alpha = 0.9f)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "Ocupa %.1f%%", percentage),
                        fontSize = 11.sp,
                        color = DarkTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onTransactClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Movimiento",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Monto", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }

                OutlinedButton(
                    onClick = onAddYieldClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = sourceColor
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Calcular Rendimiento",
                        modifier = Modifier.size(16.dp),
                        tint = sourceColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Rendimiento", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}
