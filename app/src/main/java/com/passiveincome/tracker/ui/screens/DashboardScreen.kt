package com.passiveincome.tracker.ui.screens

import android.os.Build
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.passiveincome.tracker.ui.components.GrowingOrbStatus
import com.passiveincome.tracker.ui.components.drawOrb
import com.passiveincome.tracker.ui.theme.DarkTextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GrowingHero(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    statusText: String = "Growing...",
    height: Dp = 220.dp,
    titleFontSize: TextUnit = 36.sp,
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
        // --- 1. FONDO CON ORBES ANIMADAS ---
        val canvasModifier = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Modifier.fillMaxSize().blur(32.dp)
        } else {
            Modifier.fillMaxSize()
        }

        Canvas(modifier = canvasModifier) {
            val width = size.width
            val heightPx = size.height

            // Orbe 1 - Emerald / Verde
            drawOrb(
                center = Offset(
                    width * 0.2f + 60f * sin(time),
                    heightPx * 0.4f + 40f * cos(time * 0.8f)
                ),
                radius = (heightPx * 0.8f) + 30f * sin(time * 1.2f),
                color = Color(0xFF10B981).copy(alpha = 0.3f)
            )

            // Orbe 2 - Cían
            drawOrb(
                center = Offset(
                    width * 0.7f + 50f * cos(time * 0.7f),
                    heightPx * 0.6f + 50f * sin(time * 1.5f)
                ),
                radius = (heightPx * 1.0f) + 40f * cos(time),
                color = Color(0xFF06B6D4).copy(alpha = 0.25f)
            )

            // Orbe 3 - Indigo
            drawOrb(
                center = Offset(
                    width * 0.5f + 120f * sin(time * 0.5f),
                    heightPx * 0.3f + 30f * cos(time * 1.1f)
                ),
                radius = (heightPx * 0.7f) + 25f * sin(time),
                color = Color(0xFF6366F1).copy(alpha = 0.2f)
            )
        }

        // --- 2. CONTENIDO DEL HERO ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cápsula "Growing..." arriba
            GrowingOrbStatus(text = statusText)

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
                        letterSpacing = (-0.5).sp,
                        lineHeight = (titleFontSize.value * 1.1).sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = DarkTextSecondary.copy(alpha = 0.8f),
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.2.sp
                    )
                }
                action?.invoke()
            }
        }
    }
}
