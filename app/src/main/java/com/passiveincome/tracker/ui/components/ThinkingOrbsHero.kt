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

        // Orbe 3 - Púrpura / Índigo
        drawOrb(
            center = Offset(
                center.x + 5f * sin(time * 0.6f),
                center.y + 5f * cos(time * 1.1f)
            ),
            radius = (width * 0.35f) + 3f * sin(time * 1.5f),
            color = Color(0xFF6366F1).copy(alpha = 0.5f)
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
            colorStops = arrayOf(
                0.0f to color,
                0.4f to color.copy(alpha = color.alpha * 0.5f),
                1.0f to Color.Transparent
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )
}