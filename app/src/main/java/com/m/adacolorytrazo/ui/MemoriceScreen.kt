package com.m.adacolorytrazo.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.m.adacolorytrazo.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class MemoryCard(
    val base: String,
    val pairId: Int,
    val faceResId: Int,
    val backResId: Int,
    val isFaceUp: Boolean = false,
    val isMatched: Boolean = false
)

private val MemoryRounded = FontFamily(
    Font(R.font.fredoka_bold, weight = FontWeight.Bold),
    Font(R.font.fredoka_semibold, weight = FontWeight.SemiBold)
)

private val MEMORY_ITEMS_BY_CATEGORY = linkedMapOf(
    "colores" to listOf(
        "color_rojo", "color_amarillo", "color_azul", "color_verde",
        "color_naranjo", "color_morado", "color_rosado", "color_celeste"
    ),
    "frutas" to listOf("manzana", "platano", "frutilla", "pera", "uva", "naranja", "sandia", "pina", "cereza", "durazno"),
    "verduras" to listOf("zanahoria", "tomate", "brocoli", "pepino", "papa"),
    "transportes" to listOf("auto", "avion", "tren", "bici", "barco", "ambulancia", "policia"),
    "animales" to listOf("gato", "perro", "vaca", "oveja", "pato", "conejo", "cerdo", "caballo")
)

private val MEMORY_COLOR_VALUES = mapOf(
    "color_rojo" to ("Rojo" to Color(0xFFE53935)),
    "color_amarillo" to ("Amarillo" to Color(0xFFFFD21F)),
    "color_azul" to ("Azul" to Color(0xFF1686D9)),
    "color_verde" to ("Verde" to Color(0xFF31A85A)),
    "color_naranjo" to ("Naranjo" to Color(0xFFFF8B24)),
    "color_morado" to ("Morado" to Color(0xFF823BB7)),
    "color_rosado" to ("Rosado" to Color(0xFFEA5D91)),
    "color_celeste" to ("Celeste" to Color(0xFF62C5DC))
)

private fun chooseMemoryItems(pairCount: Int): List<String> {
    val pools = MEMORY_ITEMS_BY_CATEGORY.mapValues { (_, items) -> items.shuffled().toMutableList() }
    val categoryOrder = listOf("colores", "frutas", "verduras", "transportes").shuffled() + "animales"
    val chosen = mutableListOf<String>()
    while (chosen.size < pairCount) {
        var added = false
        categoryOrder.forEach { category ->
            if (chosen.size < pairCount) {
                val next = pools[category]?.removeFirstOrNull()
                if (next != null) {
                    chosen += next
                    added = true
                }
            }
        }
        if (!added) break
    }
    return chosen
}

private fun memoryDisplayName(baseName: String): String =
    MEMORY_COLOR_VALUES[baseName]?.first
        ?: spokenName(category = memoryCategory(baseName), id = baseName)

private val memoryCardsSaver = listSaver<List<MemoryCard>, Any>(
    save = { cards ->
        cards.flatMap { card ->
            listOf(
                card.base,
                card.pairId,
                card.faceResId,
                card.backResId,
                card.isFaceUp,
                card.isMatched
            )
        }
    },
    restore = { saved ->
        val restored = saved.chunked(6).mapNotNull { fields ->
            val base = fields.getOrNull(0) as? String ?: return@mapNotNull null
            val pairId = (fields.getOrNull(1) as? Number)?.toInt()
                ?: return@mapNotNull null
            val faceResId = (fields.getOrNull(2) as? Number)?.toInt()
                ?: return@mapNotNull null
            val backResId = (fields.getOrNull(3) as? Number)?.toInt()
                ?: return@mapNotNull null
            val isFaceUp = fields.getOrNull(4) as? Boolean
                ?: return@mapNotNull null
            val isMatched = fields.getOrNull(5) as? Boolean
                ?: return@mapNotNull null

            MemoryCard(
                base = base,
                pairId = pairId,
                faceResId = faceResId,
                backResId = backResId,
                isFaceUp = isFaceUp,
                isMatched = isMatched
            )
        }
        // A coroutine cannot be restored. Hide a pending failed pair after rotation.
        if (restored.count { it.isFaceUp && !it.isMatched } >= 2) {
            restored.map { if (it.isMatched) it else it.copy(isFaceUp = false) }
        } else {
            restored
        }
    }
)

@Suppress("DiscouragedApi")
@Composable
fun MemoriceScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val sound = rememberSound()
    val haptics = rememberHaptics()
    val speech = rememberSpeech()
    var levelPairs by rememberSaveable { mutableIntStateOf(3) }

    fun iconRes(base: String): Int {
        if (base.startsWith("color_")) return R.drawable.ic_launcher_foreground
        val threeD = ctx.resources.getIdentifier(
            "${base}_icon_3d", "drawable", ctx.packageName
        )
        if (threeD != 0) return threeD
        val r = ctx.resources.getIdentifier("${base}_icon", "drawable", ctx.packageName)
        return if (r != 0) r else R.drawable.ic_launcher_foreground
    }

    fun newGame(pairCount: Int): List<MemoryCard> {
        val chosen = chooseMemoryItems(pairCount)
        return chosen.flatMapIndexed { idx, base ->
            val face = iconRes(base)
            listOf(
                MemoryCard(
                    base = base,
                    pairId = idx,
                    faceResId = face,
                    backResId = R.drawable.ada_memorice_cardback
                ),
                MemoryCard(
                    base = base,
                    pairId = idx,
                    faceResId = face,
                    backResId = R.drawable.ada_memorice_cardback
                )
            )
        }.shuffled()
    }

    var cards by rememberSaveable(stateSaver = memoryCardsSaver) {
        mutableStateOf(newGame(levelPairs))
    }
    var first by rememberSaveable { mutableStateOf<Int?>(null) }
    // Transient lock is intentionally not saved across rotation/backgrounding.
    var lock by remember { mutableStateOf(false) }
    var moves by rememberSaveable { mutableIntStateOf(0) }
    var generation by rememberSaveable { mutableIntStateOf(0) }
    val mismatchJob = remember { mutableStateOf<Job?>(null) }

    // Kept as semantic feedback for TalkBack; it is deliberately not rendered as a
    // reading exercise for toddlers. The board communicates state with stars, color,
    // card borders, haptics, sound, and the flip animation.
    var feedback by rememberSaveable { mutableStateOf("Encuentra parejas de colores, frutas, verduras y transportes.") }
    var challengeBurst by rememberSaveable { mutableIntStateOf(0) }
    var showChallengeFireworks by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(350)
        speech.speak("Encuentra parejas de colores, frutas, verduras y transportes")
    }

    DisposableEffect(Unit) {
        onDispose {
            mismatchJob.value?.cancel()
            mismatchJob.value = null
        }
    }

    LaunchedEffect(challengeBurst) {
        if (challengeBurst > 0) {
            showChallengeFireworks = true
            delay(2400)
            showChallengeFireworks = false
        } else {
            showChallengeFireworks = false
        }
    }

    val boardComplete = cards.isNotEmpty() && cards.all { it.isMatched }
    LaunchedEffect(boardComplete, levelPairs, generation) {
        if (!boardComplete || levelPairs >= 9) return@LaunchedEffect
        val scheduledGeneration = generation
        delay(2500)
        if (scheduledGeneration != generation || !cards.all { it.isMatched }) return@LaunchedEffect
        val nextPairCount = levelPairs + 3
        levelPairs = nextPairCount
        cards = newGame(nextPairCount)
        first = null
        moves = 0
        feedback = "Nivel ${nextPairCount / 3}: encuentra las parejas iguales."
        lock = false
        speech.speak("Nuevo nivel. Encuentra $nextPairCount parejas iguales.")
    }

    val matchedPairs = cards.count { it.isMatched } / 2
    val totalPairs = levelPairs

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val gridColumns = if (totalPairs >= 9) 4 else 3
        val gridRows = (cards.size + gridColumns - 1) / gridColumns
        val gridHeight = (maxHeight - 230.dp).coerceAtLeast(180.dp)
        val cardHeight = (((gridHeight.value - 10f * (gridRows - 1)) / gridRows).dp)
            .coerceIn(74.dp, 120.dp)
        PlayfulGameBackdrop(Modifier.fillMaxSize())

        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            MemoriceHeader(onBack = onBack)

            MemoryProgress(
                matchedPairs = matchedPairs,
                totalPairs = totalPairs,
                moves = moves,
                feedback = feedback,
                modifier = Modifier.fillMaxWidth()
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(cards) { idx, c ->
                    CuteMemoryCard(
                        baseName = c.base,
                        faceResId = c.faceResId,
                        isFaceUp = c.isFaceUp || c.isMatched,
                        isMatched = c.isMatched,
                        cardHeight = cardHeight,
                        onClick = {
                            if (lock) return@CuteMemoryCard
                            if (c.isMatched) return@CuteMemoryCard
                            if (cards[idx].isFaceUp) return@CuteMemoryCard

                            cards = cards.toMutableList().also { list ->
                                list[idx] = list[idx].copy(isFaceUp = true)
                            }

                            if (first == null) {
                                first = idx
                            } else {
                                val a = first!!
                                first = null
                                moves++

                                val ca = cards[a]
                                val cb = cards[idx]

                                if (ca.pairId == cb.pairId) {
                                    val pairName = memoryDisplayName(ca.base)
                                    feedback = "Par encontrado: $pairName"
                                    sound.playCoin()
                                    haptics.reward()
                                    val matchedCards = cards.toMutableList().also { list ->
                                        list[a] = list[a].copy(isMatched = true)
                                        list[idx] = list[idx].copy(isMatched = true)
                                    }
                                    cards = matchedCards

                                    if (matchedCards.all { it.isMatched }) {
                                        challengeBurst++
                                        if (totalPairs < 9) {
                                            val nextPairCount = totalPairs + 3
                                            feedback = "¡Nivel completado! Ahora vienen $nextPairCount parejas."
                                            speech.speak("¡Completaste el nivel! Ahora vamos con $nextPairCount parejas.")
                                            lock = true
                                        } else {
                                            feedback = "¡Completaste los tres niveles de Memorice!"
                                            speech.speak("¡Increíble! Completaste los tres niveles de Memorice.")
                                        }
                                    } else {
                                        speech.speak("¡Muy bien! Dos iguales: $pairName.")
                                    }
                                } else {
                                    feedback = "Las cartas se vuelven a ocultar"
                                    speech.speak("Casi. Vamos a buscar dos dibujos iguales.")
                                    lock = true

                                    mismatchJob.value?.cancel()
                                    val scheduledGeneration = generation
                                    val firstPairId = ca.pairId
                                    val secondPairId = cb.pairId
                                    mismatchJob.value = scope.launch {
                                        try {
                                            delay(1200)

                                            if (scheduledGeneration != generation) return@launch

                                            val currentCards = cards
                                            if (
                                                scheduledGeneration != generation ||
                                                a !in currentCards.indices ||
                                                idx !in currentCards.indices ||
                                                currentCards[a].pairId != firstPairId ||
                                                currentCards[idx].pairId != secondPairId
                                            ) {
                                                return@launch
                                            }

                                            cards = currentCards.toMutableList().also { list ->
                                                list[a] = list[a].copy(isFaceUp = false)
                                                list[idx] = list[idx].copy(isFaceUp = false)
                                            }
                                            if (scheduledGeneration == generation) {
                                                lock = false
                                            }
                                        } finally {
                                            if (scheduledGeneration == generation) {
                                                lock = false
                                                mismatchJob.value = null
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }

            MemoryLevelFooter(
                currentPairs = totalPairs,
                matchedPairs = matchedPairs,
                onRestart = {
                    generation++
                    mismatchJob.value?.cancel()
                    mismatchJob.value = null
                    levelPairs = 3
                    cards = newGame(3)
                    first = null
                    lock = false
                    moves = 0
                    challengeBurst = 0
                    showChallengeFireworks = false
                    feedback = "Encuentra parejas de colores, frutas, verduras y transportes."
                    speech.speak("Empezamos de nuevo con tres parejas")
                }
            )
        }

        MemoryFireworksOverlay(
            visible = showChallengeFireworks,
            burstKey = challengeBurst,
            label = if (totalPairs >= 9) "¡Memorice completado!" else "¡Nivel ${totalPairs / 3} superado!",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun MemoriceHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(58.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            Text(
                text = "Memorice",
                color = Color.Unspecified,
                style = TextStyle(brush = PlayfulRainbowTitleBrush),
                fontFamily = MemoryRounded,
                fontWeight = FontWeight.Bold,
                fontSize = 23.sp,
                lineHeight = 25.sp
            )
            Text(
                text = "Ada, frutas, verduras y más",
                color = Color(0xFF315F54),
                fontFamily = MemoryRounded,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                lineHeight = 14.sp
            )
        }
        PlayfulHomeButton(onClick = onBack)
    }
}

@Composable
private fun MemoryLevelFooter(
    currentPairs: Int,
    matchedPairs: Int,
    onRestart: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(70.dp),
        shape = RoundedCornerShape(25.dp),
        color = Color(0xFFFFFCF3).copy(alpha = 0.94f),
        shadowElevation = 8.dp,
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.96f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 7.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(3, 6, 9).forEach { pairMilestone ->
                val completed = pairMilestone < currentPairs ||
                    (pairMilestone == currentPairs && matchedPairs == currentPairs)
                val current = pairMilestone == currentPairs && !completed
                Surface(
                    modifier = Modifier.weight(1f).fillMaxHeight(),
                    shape = RoundedCornerShape(17.dp),
                    color = when {
                        completed -> Color(0xFFFFE08A)
                        current -> Color(0xFFD9F0DF)
                        else -> Color(0xFFEAF3EF)
                    },
                    border = BorderStroke(
                        width = if (current) 2.dp else 1.dp,
                        color = when {
                            completed -> Color(0xFFE1B74F)
                            current -> Color(0xFF4BA36E)
                            else -> Color(0xFFD4E4DC)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = when {
                                completed -> "★  Nivel ${pairMilestone / 3}"
                                current -> "En juego"
                                else -> "Nivel ${pairMilestone / 3}"
                            },
                            color = Color(0xFF315F54),
                            fontFamily = MemoryRounded,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            lineHeight = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "$pairMilestone parejas",
                            color = Color(0xFF52746A),
                            fontFamily = MemoryRounded,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 9.sp,
                            lineHeight = 11.sp,
                            maxLines = 1
                        )
                    }
                }
            }
            Surface(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .clickable(role = Role.Button, onClick = onRestart)
                    .semantics {
                        contentDescription = "Reiniciar desde el primer nivel"
                        role = Role.Button
                    },
                shape = CircleShape,
                color = Color(0xFFFFD15B),
                shadowElevation = 4.dp,
                border = BorderStroke(2.dp, Color.White)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "↻",
                        color = Color(0xFF315F54),
                        fontFamily = MemoryRounded,
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MemoryProgress(
    matchedPairs: Int,
    totalPairs: Int,
    moves: Int,
    feedback: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .semantics {
                contentDescription = "$feedback. Progreso: $matchedPairs de $totalPairs pares. Movimientos: $moves."
            },
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFFFFCF3),
        shadowElevation = 9.dp,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.94f))
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                Text(
                    text = "Nivel ${totalPairs / 3}",
                    color = Color(0xFF28534B),
                    fontFamily = MemoryRounded,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 16.sp
                )
                Text(
                    text = "$matchedPairs de $totalPairs parejas  •  $moves jugadas",
                    color = Color(0xFF52746A),
                    fontFamily = MemoryRounded,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(totalPairs) { index ->
                    ProgressStar(found = index < matchedPairs)
                }
            }
        }
    }
}

@Composable
private fun ProgressStar(found: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (found) 1f else 0.86f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "progress-star"
    )
    Surface(
        modifier = Modifier
            .size(18.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = CircleShape,
        color = if (found) Color(0xFFFFD66D) else Color(0xFFE7F2EE),
        border = BorderStroke(1.dp, if (found) Color(0xFFF2B949) else Color(0xFFBBD7CA))
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (found) {
                Text("★", color = Color(0xFF805B13), fontSize = 11.sp)
            } else {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF9EC9B5))
                )
            }
        }
    }
}

@Composable
private fun MemoryCardBack() {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(5.dp)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(Color(0xFF31B8D5), Color(0xFF238FB6))))
            .border(2.dp, Color.White.copy(alpha = 0.9f), shape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ada_memorice_cardback),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun CuteMemoryCard(
    baseName: String,
    faceResId: Int,
    isFaceUp: Boolean,
    isMatched: Boolean,
    cardHeight: Dp,
    onClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFaceUp) 180f else 0f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "flip"
    )
    val scale by animateFloatAsState(
        targetValue = if (isFaceUp || isMatched) 1.035f else 1f,
        animationSpec = tween(240, easing = FastOutSlowInEasing),
        label = "card-pop"
    )
    val idleTransition = rememberInfiniteTransition(label = "card-idle-$baseName")
    val idleBob by idleTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card-idle-bob-$baseName"
    )
    val label = memoryDisplayName(baseName)

    Surface(
        shape = RoundedCornerShape(22.dp),
        tonalElevation = if (isMatched) 4.dp else 2.dp,
        shadowElevation = if (isFaceUp || isMatched) 14.dp else 8.dp,
        border = BorderStroke(
            if (isMatched) 3.dp else 1.dp,
            if (isMatched) Color(0xFFFFD75E) else Color.White.copy(alpha = 0.9f)
        ),
        modifier = Modifier
            .height(cardHeight)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 20f * density
                scaleX = scale
                scaleY = scale
                translationY = if (!isFaceUp && !isMatched) idleBob else 0f
            }
            .clip(RoundedCornerShape(22.dp))
            .clickable(enabled = !isMatched && !isFaceUp, role = Role.Button) { onClick() }
            .semantics {
                contentDescription = when {
                    isMatched -> "Par encontrado: $label"
                    isFaceUp -> "Figura descubierta: $label"
                    else -> "Carta oculta. Toca para descubrir el dibujo."
                }
                role = Role.Button
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isMatched) Color(0xFFE8F5E9) else Color(0xFFFFFDF5)),
            contentAlignment = Alignment.Center
        ) {
            if (rotation < 90f) {
                MemoryCardBack()
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                    contentAlignment = Alignment.Center
                ) {
                    if (baseName.startsWith("color_")) {
                        MemoryColorFace(baseName = baseName, cardHeight = cardHeight)
                    } else {
                        Image(
                            painter = painterResource(id = faceResId),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryColorFace(baseName: String, cardHeight: Dp) {
    val (name, color) = MEMORY_COLOR_VALUES[baseName] ?: ("Color" to Color.Gray)
    val swatchSize = (cardHeight * 0.46f).coerceIn(30.dp, 56.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Surface(
            modifier = Modifier.size(swatchSize),
            shape = CircleShape,
            color = color,
            shadowElevation = 3.dp,
            border = BorderStroke(
                width = if (name == "Blanco") 2.dp else 1.dp,
                color = if (name == "Blanco") Color(0xFF82938F) else Color.White
            )
        ) {}
        Text(
            text = name,
            color = Color(0xFF315F54),
            fontFamily = MemoryRounded,
            fontWeight = FontWeight.Bold,
            fontSize = if (cardHeight < 86.dp) 10.sp else 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun memoryCategory(baseName: String): String = when {
    baseName.startsWith("color_") -> "colores"
    baseName in MEMORY_ITEMS_BY_CATEGORY["animales"].orEmpty() -> "animales"
    baseName in MEMORY_ITEMS_BY_CATEGORY["frutas"].orEmpty() -> "frutas"
    baseName in MEMORY_ITEMS_BY_CATEGORY["verduras"].orEmpty() -> "verduras"
    else -> "transportes"
}


@Composable
private fun MemoryFireworksOverlay(
    visible: Boolean,
    burstKey: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    val badgeScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.72f,
        animationSpec = tween(420, easing = FastOutSlowInEasing),
        label = "fireworks-badge-scale"
    )
    val badgeAlpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(260),
        label = "fireworks-badge-alpha"
    )

    LaunchedEffect(visible, burstKey) {
        if (visible) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(2400, easing = LinearOutSlowInEasing)
            )
        } else {
            progress.snapTo(0f)
        }
    }
    if (!visible) return

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
                androidx.compose.ui.geometry.Offset(w * 0.18f, h * 0.27f),
                androidx.compose.ui.geometry.Offset(w * 0.82f, h * 0.30f),
                androidx.compose.ui.geometry.Offset(w * 0.50f, h * 0.20f)
            )
            centers.forEachIndexed { burstIndex, center ->
                val startTime = burstIndex * 0.22f
                if (progress.value >= startTime) {
                    val t = ((progress.value - startTime) / 0.50f).coerceIn(0f, 1f)
                    val alpha = 1f - t
                    val radius = minDim * (0.018f + 0.17f * t)
                    val glow = colors[burstIndex]
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.76f * alpha),
                                glow.copy(alpha = 0.48f * alpha),
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
                            color = Color.White.copy(alpha = 0.9f * alpha),
                            center = androidx.compose.ui.geometry.Offset(
                                end.x - minDim * 0.002f,
                                end.y - minDim * 0.002f
                            ),
                            radius = minDim * 0.0028f
                        )
                    }
                    drawCircle(
                        color = Color.White.copy(alpha = 0.86f * alpha),
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
                        listOf(Color.White.copy(alpha = twinkle), colors[sparkle % colors.size].copy(alpha = twinkle), Color.Transparent),
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
                    alpha = badgeAlpha
                },
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFFFD15B),
            shadowElevation = 18.dp,
            border = BorderStroke(3.dp, Color.White)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(listOf(Color(0xFFFFEA86), Color(0xFFFFAE56))),
                        RoundedCornerShape(28.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 15.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "★  $label  ★",
                    color = Color(0xFF31544B),
                    fontFamily = MemoryRounded,
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
