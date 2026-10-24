package com.m.adacolorytrazo.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m.adacolorytrazo.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val AchievementRounded = FontFamily(
    Font(R.font.fredoka_bold, weight = FontWeight.Bold),
    Font(R.font.fredoka_semibold, weight = FontWeight.SemiBold)
)

/** Fuegos artificiales con brillo volumétrico para celebrar un acierto o logro. */
@Composable
fun FireworksOverlay(
    modifier: Modifier = Modifier,
    visible: Boolean,
    burstKey: Int,
    durationMs: Long = 1200L,
    label: String = "¡Lo lograste!"
) {
    if (!visible) return

    val progress = remember { Animatable(0f) }
    val badgeScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "achievement-fireworks-badge-scale"
    )

    LaunchedEffect(burstKey) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = durationMs.toInt().coerceIn(600, 3000),
                easing = LinearOutSlowInEasing
            )
        )
    }

    val colors = listOf(
        Color(0xFFFFD34E), Color(0xFFFF6B8A), Color(0xFF56CFE1),
        Color(0xFF8C65E8), Color(0xFF65CA78), Color(0xFFFF9F43),
        Color(0xFFFFF7C2)
    )
    Box(modifier = modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val minDim = size.minDimension
            val centers = listOf(
                androidx.compose.ui.geometry.Offset(w * 0.17f, h * 0.27f),
                androidx.compose.ui.geometry.Offset(w * 0.83f, h * 0.30f),
                androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.19f)
            )
            centers.forEachIndexed { burstIndex, center ->
                val startTime = burstIndex * 0.23f
                if (progress.value >= startTime) {
                    val t = ((progress.value - startTime) / 0.52f).coerceIn(0f, 1f)
                    val alpha = 1f - t
                    val radius = minDim * (0.018f + 0.17f * t)
                    val glow = colors[burstIndex]
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.80f * alpha),
                                glow.copy(alpha = 0.52f * alpha),
                                Color.Transparent
                            ),
                            center = center,
                            radius = (radius * 1.45f).coerceAtLeast(1f)
                        ),
                        center = center,
                        radius = radius * 1.45f
                    )
                    repeat(24) { ray ->
                        val angle = 2.0 * PI * ray / 24.0
                        val dx = cos(angle).toFloat()
                        val dy = sin(angle).toFloat()
                        val endDistance = radius * (0.66f + 0.32f * t)
                        val rayColor = colors[(ray + burstIndex * 2) % colors.size]
                        val end = androidx.compose.ui.geometry.Offset(
                            center.x + dx * endDistance,
                            center.y + dy * endDistance
                        )
                        val start = androidx.compose.ui.geometry.Offset(
                            center.x + dx * radius * 0.30f,
                            center.y + dy * radius * 0.30f
                        )
                        drawLine(
                            color = rayColor.copy(alpha = alpha),
                            start = start,
                            end = end,
                            strokeWidth = minDim * (0.0035f + 0.0025f * alpha)
                        )
                        drawCircle(
                            color = rayColor.copy(alpha = alpha),
                            center = end,
                            radius = minDim * 0.008f * (0.7f + 0.3f * alpha)
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.92f * alpha),
                            center = androidx.compose.ui.geometry.Offset(
                                end.x - minDim * 0.002f,
                                end.y - minDim * 0.002f
                            ),
                            radius = minDim * 0.0028f
                        )
                    }
                    drawCircle(
                        color = Color.White.copy(alpha = 0.88f * alpha),
                        center = center,
                        radius = minDim * 0.014f * alpha
                    )
                }
            }

            repeat(18) { sparkle ->
                val angle = 2.0 * PI * sparkle / 18.0 + progress.value * 2.0
                val distance = minDim * (0.22f + (sparkle % 4) * 0.035f)
                val center = androidx.compose.ui.geometry.Offset(
                    w * 0.50f + cos(angle).toFloat() * distance,
                    h * 0.42f + sin(angle).toFloat() * distance
                )
                val twinkle = (0.45f + 0.45f * sin(progress.value * 10f + sparkle).toFloat()).coerceIn(0f, 1f)
                val radius = minDim * (0.0035f + 0.003f * twinkle)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = twinkle),
                            colors[sparkle % colors.size].copy(alpha = twinkle),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius * 3.5f
                    ),
                    center = center,
                    radius = radius * 3.5f
                )
                drawCircle(color = Color.White.copy(alpha = twinkle), center = center, radius = radius)
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 18.dp)
                .graphicsLayer {
                    scaleX = badgeScale
                    scaleY = badgeScale
                },
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFFFD15B),
            shadowElevation = 18.dp,
            border = androidx.compose.foundation.BorderStroke(3.dp, Color.White)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFFFFEA86), Color(0xFFFFAE56))),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "★  $label  ★",
                    color = Color(0xFF31544B),
                    fontFamily = AchievementRounded,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
