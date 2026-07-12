package com.passiveincome.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CallMade
import androidx.compose.material.icons.filled.CallReceived
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passiveincome.tracker.data.Movement
import com.passiveincome.tracker.ui.theme.DarkTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MovementRow(
    movement: Movement,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(movement.timestamp))

    val (icon, iconColor, bgColor) = when (movement.type) {
        "Depósito" -> Triple(
            Icons.Default.CallReceived,
            Color(0xFF10B981), // Emerald Green
            Color(0xFF10B981).copy(alpha = 0.12f)
        )
        "Retiro" -> Triple(
            Icons.Default.CallMade,
            Color(0xFFEF4444), // Red Rose
            Color(0xFFEF4444).copy(alpha = 0.12f)
        )
        "Cierre Mensual" -> Triple(
            Icons.Default.CalendarToday,
            Color(0xFF00BCD4), // Teal
            Color(0xFF00BCD4).copy(alpha = 0.12f)
        )
        else -> Triple( // Rendimiento
            Icons.Default.TrendingUp,
            Color(0xFF8B5CF6), // Purple
            Color(0xFF8B5CF6).copy(alpha = 0.12f)
        )
    }

    val amountPrefix = if (movement.amount > 0) "+" else ""

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = movement.type,
                    tint = iconColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = movement.sourceName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = movement.description,
                    fontSize = 12.sp,
                    color = DarkTextSecondary
                )
                Text(
                    text = formattedDate,
                    fontSize = 10.sp,
                    color = DarkTextSecondary.copy(alpha = 0.6f)
                )
            }
        }

        val displayAmount = if (movement.type == "Cierre Mensual") {
            String.format(Locale.getDefault(), "$%,.2f", movement.amount)
        } else {
            val amountPrefix = if (movement.amount > 0) "+" else ""
            String.format(Locale.getDefault(), "%s$%,.2f", amountPrefix, movement.amount)
        }
        
        val amountColor = if (movement.type == "Cierre Mensual") {
            Color.White
        } else if (movement.amount > 0) {
            Color(0xFF10B981)
        } else {
            Color(0xFFEF4444)
        }

        Text(
            text = displayAmount,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = amountColor
        )
    }
}
