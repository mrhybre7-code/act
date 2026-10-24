package com.m.adacolorytrazo.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalDensity
import com.m.adacolorytrazo.R

private val PlayfulInk = Color(0xFF24495A)
private val PlayfulTeal = Color(0xFF246C70)
private val PlayfulMint = Color(0xFF53B884)
private val PlayfulGold = Color(0xFFFFD66D)
val PlayfulRainbowTitleBrush = Brush.linearGradient(
    listOf(Color(0xFFD93580), Color(0xFFE57A19), Color(0xFF188FC0), Color(0xFF7651C8))
)
val PlayfulAdaRounded = FontFamily(
    Font(R.font.fredoka_bold, weight = FontWeight.Bold),
    Font(R.font.fredoka_semibold, weight = FontWeight.SemiBold)
)

val PlayfulCardTints = listOf(
    Color(0xFFFFE2AA),
    Color(0xFFD8F1D4),
    Color(0xFFD5ECFF),
    Color(0xFFFFDCE5),
    Color(0xFFE9DFFF)
)

/** Fondo común del mundo educativo: parque 3D colorido con espacio para la interacción. */
@Composable
fun PlayfulGameBackdrop(modifier: Modifier = Modifier) {
    Box(modifier = modifier) {
        Image(
            painter = painterResource(R.drawable.ada_menu_educational_park),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0x1ECAEFFF),
                            Color.Transparent,
                            Color(0x1A17391F)
                        )
                    )
                )
        )
    }
}

@Composable
fun AdventureTopBar(
    title: String,
    subtitle: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    titleFontFamily: FontFamily? = PlayfulAdaRounded
) {
    Row(
        modifier = modifier.fillMaxWidth().height(58.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            Text(
                text = title,
                color = Color.Unspecified,
                style = TextStyle(brush = PlayfulRainbowTitleBrush),
                fontFamily = titleFontFamily,
                fontSize = 21.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = PlayfulTeal,
                fontFamily = titleFontFamily,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.size(8.dp))
        PlayfulHomeButton(onClick = onBack)
    }
}

@Composable
fun PlayfulHomeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String = "Volver al menú"
) {
    Surface(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { contentDescription = description },
        shape = CircleShape,
        color = PlayfulGold,
        contentColor = PlayfulTeal,
        shadowElevation = 8.dp,
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.96f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFFFFE98A), Color(0xFFFFB94F))),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.home_cottage_3d),
                contentDescription = null,
                modifier = Modifier.size(42.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun PlayfulAudioMark() {
    Canvas(Modifier.size(29.dp)) {
        val w = size.width
        val h = size.height
        val speaker = Path().apply {
            moveTo(w * 0.12f, h * 0.39f)
            lineTo(w * 0.34f, h * 0.39f)
            lineTo(w * 0.64f, h * 0.17f)
            lineTo(w * 0.64f, h * 0.83f)
            lineTo(w * 0.34f, h * 0.61f)
            lineTo(w * 0.12f, h * 0.61f)
            close()
        }
        drawPath(speaker, Color(0xFF287B83))
        drawLine(
            color = Color(0xFF8CE0D0),
            start = Offset(w * 0.20f, h * 0.44f),
            end = Offset(w * 0.31f, h * 0.44f),
            strokeWidth = w * 0.045f
        )
        drawArc(
            color = Color(0xFFE96C5D),
            startAngle = -54f,
            sweepAngle = 108f,
            useCenter = false,
            topLeft = Offset(w * 0.50f, h * 0.31f),
            size = Size(w * 0.34f, h * 0.38f),
            style = Stroke(width = w * 0.075f)
        )
        drawArc(
            color = Color(0xFFFFB84E),
            startAngle = -54f,
            sweepAngle = 108f,
            useCenter = false,
            topLeft = Offset(w * 0.54f, h * 0.16f),
            size = Size(w * 0.43f, h * 0.68f),
            style = Stroke(width = w * 0.065f)
        )
        drawCircle(
            color = Color.White,
            radius = w * 0.038f,
            center = Offset(w * 0.81f, h * 0.30f)
        )
    }
}

@Composable
fun PlayfulAudioButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String = "Repetir la pregunta"
) {
    val shape = CircleShape
    Surface(
        modifier = modifier
            .size(50.dp)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = description
                role = Role.Button
            },
        shape = shape,
        color = Color(0xFF54BC70),
        shadowElevation = 8.dp,
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.96f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF97E982), Color(0xFF45B96C))),
                    shape
                ),
            contentAlignment = Alignment.Center
        ) {
            PlayfulAudioMark()
        }
    }
}

@Composable
fun AdventureTargetCard(
    eyebrow: String,
    targetName: String,
    illustrationRes: Int,
    instruction: String,
    onRepeat: () -> Unit,
    modifier: Modifier = Modifier,
    showInstruction: Boolean = true,
    showTargetName: Boolean = true,
    centerPrompt: String? = "👂",
    centerPromptFontFamily: FontFamily? = null
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(130.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFFFFFCF3),
        shadowElevation = 12.dp,
        tonalElevation = 3.dp,
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.96f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(106.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White, Color(0xFFFFE8B5), Color(0xFFFFD98B))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                val transition = rememberInfiniteTransition(label = "target-float")
                val bob by transition.animateFloat(
                    initialValue = -3f,
                    targetValue = 3f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1450, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "target-bob"
                )
                val density = LocalDensity.current
                Image(
                    painter = painterResource(illustrationRes),
                    contentDescription = targetName,
                    modifier = Modifier
                        .size(101.dp)
                        .graphicsLayer { translationY = bob * density.density },
                    contentScale = ContentScale.Fit
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (showTargetName) {
                    Text(
                        text = eyebrow.uppercase(),
                        color = Color(0xFF72827B),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = targetName,
                        color = Color.Unspecified,
                        style = TextStyle(brush = PlayfulRainbowTitleBrush),
                        fontSize = if (targetName.length > 10) 20.sp else 24.sp,
                        lineHeight = 27.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (showInstruction) {
                        Text(
                            text = instruction,
                            color = PlayfulTeal,
                            fontSize = 12.sp,
                            lineHeight = 15.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        centerPrompt?.let { prompt ->
                            Text(
                                text = prompt,
                                fontSize = if (prompt.length < 3) 38.sp else 13.sp,
                                fontFamily = centerPromptFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = PlayfulTeal,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFE3F6E9))
                                    .padding(horizontal = 9.dp, vertical = 7.dp)
                                    .semantics { contentDescription = prompt }
                            )
                        }
                    }
                }
            }
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onRepeat)
                    .semantics { contentDescription = "Escuchar la consigna" },
                shape = CircleShape,
                color = Color(0xFFFFD46A),
                contentColor = Color(0xFF624B1A),
                shadowElevation = 5.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(listOf(Color(0xFFFFF4C8), Color(0xFFFFC24F))),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    PlayfulAudioMark()
                }
            }
        }
    }
}

@Composable
fun AdventureSectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        modifier = modifier.padding(start = 5.dp, top = 1.dp),
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.sp,
        color = PlayfulTeal
    )
}

@Composable
fun AdventureOptionTile(
    title: String,
    illustrationRes: Int,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    description: String = title,
    showTitle: Boolean = true,
    titleFontFamily: FontFamily? = null,
    illustrationSize: androidx.compose.ui.unit.Dp = 88.dp
) {
    val shape = RoundedCornerShape(25.dp)
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(128.dp)
            .clip(shape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = description
                role = Role.Button
            },
        shape = shape,
        color = Color.White,
        contentColor = PlayfulInk,
        shadowElevation = if (enabled) 9.dp else 3.dp,
        tonalElevation = 2.dp,
        border = BorderStroke(
            width = if (enabled) 2.dp else 1.dp,
            color = if (enabled) Color.White else Color(0xFFE5E7E5)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val transition = rememberInfiniteTransition(label = "option-float-$title")
            val bob by transition.animateFloat(
                initialValue = -2.5f,
                targetValue = 2.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1550, delayMillis = (title.length % 5) * 95, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "option-bob-$title"
            )
            val density = LocalDensity.current
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(tint.copy(alpha = 0.96f), tint.copy(alpha = 0.58f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(illustrationSize - 10.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.55f))
                )
                Image(
                    painter = painterResource(illustrationRes),
                    contentDescription = null,
                    modifier = Modifier
                        .size(illustrationSize)
                        .graphicsLayer { translationY = bob * density.density },
                    contentScale = ContentScale.Fit
                )
            }
            if (showTitle) {
                Text(
                    text = title,
                    modifier = Modifier.padding(top = 4.dp),
                    color = PlayfulInk,
                    fontFamily = titleFontFamily,
                    fontSize = 13.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AdventureStatusCard(
    message: String,
    isCorrect: Boolean?,
    modifier: Modifier = Modifier,
    visualOnly: Boolean = false
) {
    val tone = when (isCorrect) {
        true -> Color(0xFFE4F4D7)
        false -> Color(0xFFFFE6DD)
        null -> Color(0xFFFFFCF3)
    }
    Surface(
        modifier = modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(18.dp),
        color = tone,
        contentColor = PlayfulInk,
        shadowElevation = 4.dp,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.86f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 13.dp)
                .semantics { contentDescription = message },
            contentAlignment = Alignment.Center
        ) {
            if (visualOnly) {
                Text(
                    text = when (isCorrect) {
                        true -> "⭐✨⭐"
                        false -> "👀🔄"
                        null -> "🌼"
                    },
                    fontSize = 24.sp
                )
            } else {
                Text(
                    text = message,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PlayfulActionPill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accent: Color = PlayfulGold,
    fontFamily: FontFamily? = PlayfulAdaRounded
) {
    Surface(
        modifier = modifier
            .height(42.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        shape = CircleShape,
        color = accent,
        contentColor = PlayfulInk,
        shadowElevation = if (enabled) 5.dp else 0.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.88f))
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text, fontFamily = fontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}
