package com.m.adacolorytrazo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.m.adacolorytrazo.R
import kotlinx.coroutines.delay
import kotlin.random.Random

private val ColorsRounded = FontFamily(
    Font(R.font.fredoka_bold, weight = FontWeight.Bold),
    Font(R.font.fredoka_semibold, weight = FontWeight.SemiBold)
)

private val COLOR_EXAMPLES: Map<String, String> = mapOf(
    "Rojo" to "Rojo como las frutillas, el camión de bomberos y el Viejito Pascuero.",
    "Amarillo" to "Amarillo como el sol, los pollitos y el plátano.",
    "Azul" to "Azul como el cielo, el mar y el pez cirujano azul.",
    "Verde" to "Verde como el pasto, la lechuga y la manzana verde.",
    "Naranjo" to "Naranjo como la mandarina, la zanahoria y una calabaza.",
    "Morado" to "Morado como las uvas, las flores de lavanda y la betarraga.",
    "Rosado" to "Rosado como los flamencos, Peppa Pig y la pantera rosa.",
    "Café" to "Café como el chocolate, las hojas en otoño y la tierra húmeda.",
    "Celeste" to "Celeste como el cielo clarito, una nube suave y un globo.",
    "Gris" to "Gris como las nubes en un día de lluvia, una roca y un elefantito.",
    "Negro" to "Negro como la noche, las arañas y los gorilas.",
    "Blanco" to "Blanco como la nieve, la leche y el conejo de Pascua."
)

private data class UiColor(val name: String, val color: Color)

private val COLOR_LIST = listOf(
    UiColor("Rojo", Color(0xFFE53935)),
    UiColor("Amarillo", Color(0xFFFFD21F)),
    UiColor("Azul", Color(0xFF1686D9)),
    UiColor("Verde", Color(0xFF31A85A)),
    UiColor("Naranjo", Color(0xFFFF8B24)),
    UiColor("Morado", Color(0xFF823BB7)),
    UiColor("Rosado", Color(0xFFEA5D91)),
    UiColor("Café", Color(0xFF87513B)),
    UiColor("Celeste", Color(0xFF62C5DC)),
    UiColor("Gris", Color(0xFF9CA6AA)),
    UiColor("Negro", Color(0xFF25282D)),
    UiColor("Blanco", Color(0xFFFFFFFF))
)

@Composable
fun ColorsFlow(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val sound = rememberSound()
    val haptics = rememberHaptics()
    val speech = rememberSpeech()
    var target by remember { mutableStateOf(randomColorName(exclude = null)) }
    var feedback by remember { mutableStateOf("Escucha el color y encuéntralo.") }
    var lastPicked by remember { mutableStateOf<String?>(null) }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }
    var answerAccepted by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var burst by remember { mutableIntStateOf(0) }

    LaunchedEffect(burst) {
        if (burst > 0) {
            showConfetti = true
            delay(1200)
            showConfetti = false
        }
    }
    LaunchedEffect(answerAccepted) {
        if (answerAccepted) {
            delay(1200)
            val nextTarget = randomColorName(exclude = target)
            target = nextTarget
            feedback = "Escucha el color y encuéntralo."
            lastPicked = null
            isCorrect = null
            answerAccepted = false
        }
    }

    LaunchedEffect(target) {
        delay(350)
        speech.speak("Busca el color $target")
    }

    Box(modifier = modifier.fillMaxSize()) {
        PlayfulGameBackdrop(Modifier.fillMaxSize())
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            ColorsHeader(onBack = onBack)
            ColorsListeningCard(
                feedback = feedback,
                isCorrect = isCorrect,
                onRepeat = { speech.speak("Busca el color $target") }
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                COLOR_LIST.chunked(3).forEach { rowColors ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowColors.forEach { uiColor ->
                            val cardResult = when {
                                isCorrect == true && uiColor.name == target -> true
                                isCorrect == false && uiColor.name == lastPicked -> false
                                else -> null
                            }
                            ColorCard(
                                uiColor = uiColor,
                                enabled = !answerAccepted,
                                result = cardResult,
                                onClick = {
                                    if (!answerAccepted) {
                                        lastPicked = uiColor.name
                                        if (uiColor.name == target) {
                                            feedback = "¡Muy bien! Encontraste $target."
                                            isCorrect = true
                                            answerAccepted = true
                                            sound.playCoin()
                                            haptics.reward()
                                            speech.speak("¡Muy bien! Encontraste el color $target.")
                                            burst++
                                        } else {
                                            feedback = "Ese es ${uiColor.name}. Escucha otra vez."
                                            isCorrect = false
                                            speech.speak(
                                                "${COLOR_EXAMPLES[uiColor.name] ?: "Ese es ${uiColor.name}."} Busquemos el color $target."
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        FireworksOverlay(
            modifier = Modifier.fillMaxSize(),
            visible = showConfetti,
            burstKey = burst
        )
    }
}

@Composable
private fun ColorsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(58.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(
                text = "Colores",
                color = Color.Unspecified,
                style = TextStyle(brush = PlayfulRainbowTitleBrush),
                fontFamily = ColorsRounded,
                fontWeight = FontWeight.Bold,
                fontSize = 23.sp,
                lineHeight = 25.sp
            )
            Text(
                text = "Escucha, encuentra y aprende",
                color = Color(0xFF315F54),
                fontFamily = ColorsRounded,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 14.sp
            )
        }
        PlayfulHomeButton(onClick = onBack)
    }
}

@Composable
private fun ColorsListeningCard(
    feedback: String,
    isCorrect: Boolean?,
    onRepeat: () -> Unit
) {
    val cardTint = when (isCorrect) {
        true -> Color(0xFFE1F3D4)
        false -> Color(0xFFFFE5D8)
        null -> Color(0xFFFFF5D8)
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .semantics { contentDescription = feedback },
        shape = RoundedCornerShape(25.dp),
        color = cardTint,
        shadowElevation = 7.dp,
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.94f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White, Color(0xFFFFE6A7), Color(0xFFFFCC62))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text("👂", fontSize = 25.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = when (isCorrect) {
                        true -> "¡Muy bien!"
                        false -> "Escucha de nuevo"
                        null -> "Escucha a Ada"
                    },
                    color = Color(0xFF28534B),
                    fontFamily = ColorsRounded,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    lineHeight = 20.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when (isCorrect) {
                        true -> "Encontraste el color."
                        false -> "Toca el que escuchaste."
                        null -> "Toca el color que nombra."
                    },
                    color = Color(0xFF466B61),
                    fontFamily = ColorsRounded,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onRepeat)
                    .semantics {
                        contentDescription = "Repetir el color en voz alta"
                        role = Role.Button
                    },
                shape = CircleShape,
                color = Color(0xFFFFD04E),
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        tint = Color(0xFF4C563A),
                        modifier = Modifier.size(23.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.ColorCard(
    uiColor: UiColor,
    enabled: Boolean,
    result: Boolean?,
    onClick: () -> Unit
) {
    val outline = when (result) {
        true -> Color(0xFF3B9B5B)
        false -> Color(0xFFE67C52)
        null -> Color.White.copy(alpha = 0.96f)
    }
    Surface(
        modifier = Modifier
            .weight(1f)
            .height(76.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = "Color ${uiColor.name}"
                role = Role.Button
            },
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFFDFBF2).copy(alpha = 0.94f),
        shadowElevation = if (enabled) 5.dp else 2.dp,
        border = BorderStroke(if (result != null) 3.dp else 2.dp, outline)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = uiColor.color,
                shadowElevation = if (uiColor.name == "Blanco") 2.dp else 3.dp,
                border = BorderStroke(
                    width = if (uiColor.name == "Blanco") 1.5.dp else 1.dp,
                    color = if (uiColor.name == "Blanco") Color(0xFF82938F) else Color.White
                )
            ) {}
            Spacer(Modifier.size(5.dp))
            Text(
                text = uiColor.name,
                color = Color(0xFF344F49),
                fontFamily = ColorsRounded,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun randomColorName(exclude: String?): String {
    if (COLOR_LIST.size <= 1) return COLOR_LIST.first().name
    val choices = COLOR_LIST.map { it.name }.filterNot { it == exclude }
    return choices[Random.nextInt(choices.size)]
}
