package com.passiveincome.tracker.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passiveincome.tracker.data.IncomeSource
import com.passiveincome.tracker.ui.theme.DarkTextSecondary
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun String.toColorOrDefault(defaultColor: Color = Color.Gray): Color {
    return try {
        val hex = if (this.startsWith("#")) this else "#$this"
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        defaultColor
    }
}

@Composable
fun DonutChart(
    sources: List<IncomeSource>,
    modifier: Modifier = Modifier,
    thickness: Dp = 28.dp,
    gapWidth: Dp = 4.dp // Ancho físico uniforme de la separación
) {
    val total = remember(sources) { sources.sumOf { it.totalBalance } }
    var selectedSourceIndex by remember { mutableStateOf<Int?>(null) }
    val haptic = LocalHapticFeedback.current

    // Animación de entrada
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(sources) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val activeSources = remember(sources) { sources.filter { it.totalBalance > 0 } }
    val numSections = activeSources.size

    val sourceColors = remember(activeSources) {
        activeSources.map { it.colorHex.toColorOrDefault() }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .size(220.dp)
                .pointerInput(activeSources, total) {
                    detectTapGestures { offset ->
                        if (total == 0.0) return@detectTapGestures

                        val radius = size.width / 2f
                        val center = Offset(size.width / 2f, size.height / 2f)

                        val dx = offset.x - center.x
                        val dy = offset.y - center.y
                        val distance = sqrt(dx * dx + dy * dy)

                        // Verificación de toque dentro del grosor de la dona
                        if (distance <= radius && distance >= radius - thickness.toPx() * 1.5f) {
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f

                            val adjustedAngle = (angle + 90f) % 360f

                            var currentAngle = 0f
                            var foundIndex = -1

                            activeSources.forEach { source ->
                                val sweep = (source.totalBalance / total).toFloat() * 360f
                                if (adjustedAngle in currentAngle..(currentAngle + sweep)) {
                                    foundIndex = sources.indexOf(source)
                                }
                                currentAngle += sweep
                            }

                            if (selectedSourceIndex != foundIndex) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            selectedSourceIndex = if (selectedSourceIndex == foundIndex) null else foundIndex
                        } else {
                            if (selectedSourceIndex != null) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            selectedSourceIndex = null
                        }
                    }
                }
        ) {
            val canvasSize = size.minDimension
            val baseThicknessPx = thickness.toPx()
            val centerOffset = Offset(size.width / 2, size.height / 2)
            val outerRadius = canvasSize / 2f
            val innerRadius = outerRadius - baseThicknessPx
            val middleRadius = (outerRadius + innerRadius) / 2f

            // Pista sutil de fondo
            drawCircle(
                color = Color.White.copy(alpha = 0.04f),
                radius = middleRadius,
                center = centerOffset,
                style = Stroke(width = baseThicknessPx)
            )

            if (total > 0.0) {
                var currentStartAngle = -90f
                val progress = animationProgress.value

                // 1. DIBUJAR LOS ARCOS CONTINUOS (Sin gaps angulares que deformen la geometría)
                activeSources.forEachIndexed { i, source ->
                    val percentage = (source.totalBalance / total).toFloat()
                    val sweepAngle = percentage * 360f * progress
                    val originalIndex = sources.indexOf(source)
                    val isSelected = selectedSourceIndex == originalIndex
                    val color = sourceColors[i]

                    val animatedThickness = if (isSelected) baseThicknessPx + 6.dp.toPx() else baseThicknessPx

                    // Resplandor elegante al seleccionar
                    if (isSelected && sweepAngle > 0f) {
                        drawArc(
                            color = color.copy(alpha = 0.3f),
                            startAngle = currentStartAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(centerOffset.x - middleRadius, centerOffset.y - middleRadius),
                            size = Size(middleRadius * 2, middleRadius * 2),
                            style = Stroke(
                                width = animatedThickness + 8.dp.toPx(),
                                cap = StrokeCap.Butt
                            )
                        )
                    }

                    // Arco Principal
                    if (sweepAngle > 0f) {
                        drawArc(
                            color = color,
                            startAngle = currentStartAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(centerOffset.x - middleRadius, centerOffset.y - middleRadius),
                            size = Size(middleRadius * 2, middleRadius * 2),
                            style = Stroke(
                                width = animatedThickness,
                                cap = StrokeCap.Butt
                            )
                        )
                    }

                    currentStartAngle += sweepAngle
                }

                // 2. DIBUJAR CORTES RADIALES UNIFORMES
                // Si hay más de un segmento, trazamos líneas divisoras con el ancho exacto deseado.
                // Esto garantiza simetría perfecta entre el borde interno y externo.
                if (numSections > 1 && progress > 0f) {
                    var dividerAngle = -90f
                    val gapPx = gapWidth.toPx()
                    val dividerLength = outerRadius + 12.dp.toPx() // Sobresale ligeramente para limpiar bien los bordes

                    activeSources.forEach { source ->
                        val percentage = (source.totalBalance / total).toFloat()
                        val sweepAngle = percentage * 360f * progress

                        val rad = Math.toRadians(dividerAngle.toDouble())
                        val endX = centerOffset.x + (dividerLength * cos(rad)).toFloat()
                        val endY = centerOffset.y + (dividerLength * sin(rad)).toFloat()

                        // Se dibuja la línea de corte limpia
                        drawLine(
                            color = Color.Black, // Cambia según el color de fondo del contenedor si no usas canal alfa transparente
                            start = centerOffset,
                            end = Offset(endX, endY),
                            strokeWidth = gapPx,
                            cap = StrokeCap.Butt
                        )

                        dividerAngle += sweepAngle
                    }
                }
            }
        }

        // Centro Visual
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val primaryTextColor = MaterialTheme.colorScheme.onSurface

            if (selectedSourceIndex != null && selectedSourceIndex!! in sources.indices) {
                val source = sources[selectedSourceIndex!!]
                val selectedColor = remember(source.colorHex) { source.colorHex.toColorOrDefault(primaryTextColor) }

                Text(
                    text = source.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarkTextSecondary
                )
                Text(
                    text = String.format(Locale.getDefault(), "$%,.2f", source.totalBalance),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = selectedColor
                )
                Text(
                    text = String.format(Locale.getDefault(), "%.1f%%", if (total > 0) (source.totalBalance / total) * 100 else 0.0),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor.copy(alpha = 0.8f)
                )
            } else {
                Text(
                    text = if (total > sources.sumOf { it.balance }) "TOTAL PROYECTADO" else "TOTAL ACTIVO",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp,
                    color = DarkTextSecondary
                )
                Text(
                    text = String.format(Locale.getDefault(), "$%,.2f", total),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor
                )
            }
        }
    }
}