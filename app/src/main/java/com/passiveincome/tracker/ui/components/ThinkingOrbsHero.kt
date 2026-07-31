package com.passiveincome.tracker.ui.components

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ThinkingOrbsHero(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    statusText: String? = null,
    height: androidx.compose.ui.unit.Dp = 200.dp,
    titleFontSize: androidx.compose.ui.unit.TextUnit = 40.sp,
    action: (@Composable () -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2f).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        contentAlignment = Alignment.CenterStart
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val heightPx = size.height

            // Orb 1 - Blue
            drawOrb(
                center = Offset(
                    width * 0.2f + 60f * sin(time),
                    heightPx * 0.4f + 40f * cos(time * 0.8f)
                ),
                radius = (heightPx * 0.8f) + 30f * sin(time * 1.2f),
                color = Color(0xFF3B82F6).copy(alpha = 0.2f)
            )

            // Orb 2 - Emerald
            drawOrb(
                center = Offset(
                    width * 0.7f + 50f * cos(time * 0.7f),
                    heightPx * 0.6f + 50f * sin(time * 1.5f)
                ),
                radius = (heightPx * 1.0f) + 40f * cos(time),
                color = Color(0xFF10B981).copy(alpha = 0.15f)
            )

            // Orb 3 - Cyan
            drawOrb(
                center = Offset(
                    width * 0.5f + 120f * sin(time * 0.5f),
                    heightPx * 0.3f + 30f * cos(time * 1.1f)
                ),
                radius = (heightPx * 0.7f) + 25f * sin(time),
                color = Color(0xFF06B6D4).copy(alpha = 0.15f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (statusText != null) {
                GrowingOrbStatus(text = statusText)
            } else {
                Spacer(modifier = Modifier.height(1.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = if (titleFontSize.value > 30f) (-1.5).sp else (-0.5).sp,
                        lineHeight = if (titleFontSize.value > 30f) (titleFontSize.value * 1.1).sp else titleFontSize
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        fontSize = if (titleFontSize.value > 30f) 14.sp else 12.sp,
                        color = com.passiveincome.tracker.ui.theme.DarkTextSecondary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.2.sp
                    )
                }
                action?.invoke()
            }
        }
    }
}

@Composable
fun GrowingOrbStatus(
    text: String = "Growing...",
    modifier: Modifier = Modifier
) {
    // Cápsula contenedora (Pill shape)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(Color(0xFF121212).copy(alpha = 0.85f)) // Fondo oscuro semitransparente
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(50.dp)
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Mini contenedor con el efecto de orbes
        MiniOrbCanvas(modifier = Modifier.size(28.dp))

        // Texto "Growing..."
        Text(
            text = text,
            color = Color(0xFFE2E8F0),
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.3.sp
        )
    }
}

@Composable
private fun MiniOrbCanvas(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "mini_orbs")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2f).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    val canvasModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        modifier.blur(6.dp)
    } else {
        modifier
    }

    Canvas(modifier = canvasModifier) {
        val width = size.width
        val heightPx = size.height
        val center = Offset(width / 2f, heightPx / 2f)

        // Orbe 1 - Esmeralda / Verde (Ideal para "Growing")
        drawOrb(
            center = Offset(
                center.x + 8f * sin(time),
                center.y + 6f * cos(time * 0.9f)
            ),
            radius = (width * 0.45f) + 4f * sin(time * 1.3f),
            color = Color(0xFF10B981).copy(alpha = 0.6f)
        )

        // Orbe 2 - Cían
        drawOrb(
            center = Offset(
                center.x + 6f * cos(time * 1.2f),
                center.y + 7f * sin(time * 0.7f)
            ),
            radius = (width * 0.4f) + 3f * cos(time),
            color = Color(0xFF06B6D4).copy(alpha = 0.5f)
        )

        // Orbe 3 - Púrpura / Azul
        drawOrb(
            center = Offset(
                center.x + 5f * sin(time * 0.6f),
                center.y + 5f * cos(time * 1.1f)
            ),
            radius = (width * 0.35f) + 3f * sin(time * 1.5f),
            color = Color(0xFF3B82F6).copy(alpha = 0.5f)
        )
    }
}

internal fun androidx.compose.ui.graphics.drawscope.DrawScope.drawOrb(
    center: Offset,
    radius: Float,
    color: Color
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color, Color.Transparent),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}
