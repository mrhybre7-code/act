package com.m.adacolorytrazo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

private val ProduceRounded = FontFamily(
    Font(R.font.fredoka_bold, weight = FontWeight.Bold),
    Font(R.font.fredoka_semibold, weight = FontWeight.SemiBold)
)

private val PRODUCE_DRAWABLES = mapOf(
    "manzana" to R.drawable.manzana_icon_3d,
    "platano" to R.drawable.platano_icon_3d,
    "frutilla" to R.drawable.frutilla_icon_3d,
    "pera" to R.drawable.pera_icon_3d,
    "uva" to R.drawable.uva_icon_3d,
    "naranja" to R.drawable.naranja_icon_3d,
    "sandia" to R.drawable.sandia_icon_3d,
    "pina" to R.drawable.pina_icon_3d,
    "cereza" to R.drawable.cereza_icon_3d,
    "durazno" to R.drawable.durazno_icon_3d,
    "zanahoria" to R.drawable.zanahoria_icon_3d,
    "tomate" to R.drawable.tomate_icon_3d,
    "brocoli" to R.drawable.brocoli_icon_3d,
    "pepino" to R.drawable.pepino_icon_3d,
    "papa" to R.drawable.papa_icon_3d
)

private val PRODUCE_CARD_TINTS = mapOf(
    "manzana" to Color(0xFFFF7778),
    "platano" to Color(0xFFFFD447),
    "frutilla" to Color(0xFFFF82A4),
    "pera" to Color(0xFF9DDC64),
    "uva" to Color(0xFFA982E8),
    "naranja" to Color(0xFFFF9A3E),
    "sandia" to Color(0xFF68D58D),
    "pina" to Color(0xFFFFC84A),
    "cereza" to Color(0xFFFF657D),
    "durazno" to Color(0xFFFFA77A),
    "zanahoria" to Color(0xFFFF8B42),
    "tomate" to Color(0xFFFF6D64),
    "brocoli" to Color(0xFF58C875),
    "pepino" to Color(0xFF49C6A3),
    "papa" to Color(0xFFD9A76C)
)

@Composable
fun RecognizeProduceScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val sound = rememberSound()
    val haptics = rememberHaptics()
    val speech = rememberSpeech()
    val fruits = remember {
        listOf("manzana", "platano", "frutilla", "pera", "uva", "naranja", "sandia", "pina", "cereza", "durazno")
    }
    val vegetables = remember { listOf("zanahoria", "tomate", "brocoli", "pepino", "papa") }
    val allProduce = remember { fruits + vegetables }

    var target by remember { mutableStateOf(allProduce.random()) }
    var feedback by remember { mutableStateOf("Encuentra la misma imagen que Ada nombra.") }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }
    var answerAccepted by remember { mutableStateOf(false) }
    var showConfetti by remember { mutableStateOf(false) }
    var burst by remember { mutableStateOf(0) }

    val targetCategory = if (target in fruits) "fruta" else "verdura"
    val targetGroup = if (target in fruits) "frutas" else "verduras"
    val spokenTargetName = spokenName(category = targetGroup, id = target)
    val targetDrawable = PRODUCE_DRAWABLES[target] ?: R.drawable.ic_launcher_foreground

    LaunchedEffect(target) {
        delay(350)
        speech.speak("Busca $spokenTargetName")
    }

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
            val nextTarget = allProduce.filterNot { it == target }.random()
            target = nextTarget
            feedback = "Encuentra la misma imagen que Ada nombra."
            isCorrect = null
            answerAccepted = false
        }
    }

    val options = remember(target) {
        allProduce.filterNot { it == target }.shuffled().take(3).plus(target).shuffled()
    }

    Box(modifier = modifier.fillMaxSize()) {
        PlayfulGameBackdrop(Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            ProduceHeader(onBack = onBack)
            ProduceTargetCard(
                targetDrawable = targetDrawable,
                category = targetCategory,
                feedback = feedback,
                isCorrect = isCorrect,
                onRepeat = { speech.speak("Busca $spokenTargetName") }
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items = options, key = { it }) { produce ->
                    val group = if (produce in fruits) "frutas" else "verduras"
                    val optionCategory = if (produce in fruits) "fruta" else "verdura"
                    val correct = produce == target
                    AdventureOptionTile(
                        title = prettyName(category = group, id = produce),
                        illustrationRes = PRODUCE_DRAWABLES[produce] ?: R.drawable.ic_launcher_foreground,
                        tint = PRODUCE_CARD_TINTS.getValue(produce),
                        enabled = !answerAccepted,
                        description = "${spokenName(category = group, id = produce)}, $optionCategory",
                        showTitle = true,
                        titleFontFamily = ProduceRounded,
                        onClick = {
                            if (!answerAccepted && produce == target) {
                                val correctMessage = "¡Muy bien! $spokenTargetName es una $targetCategory."
                                feedback = correctMessage
                                isCorrect = true
                                answerAccepted = true
                                sound.playCoin()
                                haptics.reward()
                                speech.speak(correctMessage)
                                burst++
                            } else if (!answerAccepted) {
                                val wrongName = spokenName(category = group, id = produce)
                                val errorMessage = "Esa es otra imagen. Escucha de nuevo y busca $spokenTargetName."
                                feedback = errorMessage
                                isCorrect = false
                                speech.speak("Ese es $wrongName, una $optionCategory. $errorMessage")
                            }
                        }
                    )
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
private fun ProduceHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(58.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(
                text = "Frutas y verduras",
                color = Color.Unspecified,
                style = TextStyle(brush = PlayfulRainbowTitleBrush),
                fontFamily = ProduceRounded,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp,
                lineHeight = 23.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Reconoce sus formas y colores",
                color = Color(0xFF315F54),
                fontFamily = ProduceRounded,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 14.sp
            )
        }
        PlayfulHomeButton(onClick = onBack)
    }
}

@Composable
private fun ProduceTargetCard(
    targetDrawable: Int,
    category: String,
    feedback: String,
    isCorrect: Boolean?,
    onRepeat: () -> Unit
) {
    val color = when (isCorrect) {
        true -> Color(0xFFE5F2D9)
        false -> Color(0xFFFFE6D8)
        null -> Color(0xFFFFFCF3)
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(118.dp)
            .semantics { contentDescription = feedback },
        shape = RoundedCornerShape(27.dp),
        color = color,
        shadowElevation = 8.dp,
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.96f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 11.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(94.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(Color.White, Color(0xFFFFE5B4), Color(0xFFFFD58A)))),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(targetDrawable),
                    contentDescription = "Imagen para reconocer: $category",
                    modifier = Modifier.size(87.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = when (isCorrect) {
                        true -> "¡Muy bien!"
                        false -> "Intenta de nuevo"
                        null -> "¿Cuál es igual?"
                    },
                    color = Color(0xFF28534B),
                    fontFamily = ProduceRounded,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    lineHeight = 22.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when (isCorrect) {
                        true -> "Es una $category."
                        false -> "Escucha el nombre y busca."
                        null -> "Escucha y elige su nombre."
                    },
                    color = Color(0xFF466B61),
                    fontFamily = ProduceRounded,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                modifier = Modifier
                    .size(43.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onRepeat)
                    .semantics {
                        contentDescription = "Repetir el nombre de la fruta o verdura"
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
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
