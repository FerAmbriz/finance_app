package com.passiveincome.tracker.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passiveincome.tracker.data.IncomeSource
import com.passiveincome.tracker.ui.theme.DarkTextSecondary
import java.util.Locale

@Composable
fun DonutChart(
    sources: List<IncomeSource>,
    modifier: Modifier = Modifier,
    thickness: Dp = 28.dp
) {
    val total = sources.sumOf { it.totalBalance }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(180.dp)) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2 - thickness.toPx() / 2
            val centerOffset = Offset(size.width / 2, size.height / 2)

            if (total == 0.0) {
                // Empty state ring
                drawCircle(
                    color = Color(0xFF2E3D60),
                    radius = radius,
                    center = centerOffset,
                    style = Stroke(width = thickness.toPx())
                )
            } else {
                var startAngle = -90f // Start drawing from 12 o'clock
                // Only draw gaps if there are multiple segments
                val activeSources = sources.filter { it.totalBalance > 0 }
                val spaceAngle = if (activeSources.size > 1) 4f else 0f
                val totalGaps = spaceAngle * activeSources.size
                val availableSweepAngle = 360f - totalGaps

                sources.forEach { source ->
                    if (source.totalBalance > 0) {
                        val percentage = (source.totalBalance / total).toFloat()
                        val sweepAngle = percentage * availableSweepAngle

                        val color = try {
                            Color(android.graphics.Color.parseColor(source.colorHex))
                        } catch (e: Exception) {
                            Color.Gray
                        }

                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(centerOffset.x - radius, centerOffset.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = thickness.toPx(), cap = StrokeCap.Round)
                        )

                        startAngle += sweepAngle + spaceAngle
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Total Activo",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = DarkTextSecondary
            )
            Text(
                text = String.format(Locale.getDefault(), "$%,.2f", total),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
