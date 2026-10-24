package com.m.adacolorytrazo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m.adacolorytrazo.ui.ConfettiOverlay
import com.m.adacolorytrazo.ui.PlayfulCardTints
import com.m.adacolorytrazo.ui.PlayfulAudioButton
import com.m.adacolorytrazo.ui.rememberHaptics
import com.m.adacolorytrazo.ui.rememberSound
import com.m.adacolorytrazo.ui.rememberSpeech
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

private data class WeatherChoice(
    val weather: Weather,
    val label: String
)

private data class ClothingChoice(
    val item: Item,
    val label: String,
    val emoji: String,
    val illustrationRes: Int? = null
)

private val WEATHER_CHOICES = listOf(
    WeatherChoice(Weather.SOLEADO, "Sol"),
    WeatherChoice(Weather.LLUVIA, "Lluvia")
)

private val CLOTHING_CHOICES = listOf(
    ClothingChoice(Item.POLERA, "Polera", "👕", R.drawable.weather_item_polera),
    ClothingChoice(Item.CHAQUETA, "Chaqueta", "🧥", R.drawable.weather_item_chaqueta),
    ClothingChoice(Item.PARAGUAS, "Paraguas", "☔", R.drawable.weather_item_paraguas),
    ClothingChoice(Item.SHORT, "Short", "🩳", R.drawable.weather_item_short),
    ClothingChoice(Item.BOTAS, "Botas", "🥾", R.drawable.weather_item_botas),
    ClothingChoice(Item.CHALAS, "Chalas", "🩴", R.drawable.weather_item_chalas)
)

@Composable
fun GameScreen(
    onBack: () -> Unit = {}
) {
    val game: GameViewModel = viewModel(key = "weather-game")
    val state = game.state
    val haptics = rememberHaptics()
    val sound = rememberSound()
    val speech = rememberSpeech()
    var targetBounds by remember { mutableStateOf<Rect?>(null) }

    val weather = state.weather
    val choices = remember(weather) {
        weather?.let { current ->
            clothingChoicesFor(current).mapNotNull { item ->
                CLOTHING_CHOICES.firstOrNull { it.item == item }
            }
        }.orEmpty()
    }
    val isComplete = weather?.let { outfitComplete(it, state.selected) } == true
    var celebrationKey by remember { mutableIntStateOf(0) }
    var celebratedWeather by remember { mutableStateOf<Weather?>(null) }

    LaunchedEffect(Unit) {
        delay(350)
        if (state.weather == null) {
            speech.speak("Elige un clima para Ada.")
        }
    }

    LaunchedEffect(state.weather, state.selected) {
        if (!isComplete) {
            celebratedWeather = null
        } else {
            val completedWeather = checkNotNull(weather)
            if (celebratedWeather != completedWeather) {
                celebratedWeather = completedWeather
                celebrationKey++
                haptics.reward()
                sound.playCoin()
            }
        }
    }

    val equipItem: (Item) -> Unit = { item ->
        val accepted = game.dropItem(item) { message -> speech.speak(message) }
        if (accepted && game.state.weather?.let {
                !outfitComplete(it, game.state.selected)
            } == true
        ) {
            haptics.reward()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.weather_garden_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                GameBackButton(onClick = onBack)
                WeatherChooser(
                    selected = weather,
                    modifier = Modifier.weight(1f),
                    onSelected = { selectedWeather ->
                        targetBounds = null
                        haptics.reward()
                        game.setWeather(selectedWeather) { message -> speech.speak(message) }
                    }
                )
                PlayfulAudioButton(
                    onClick = {
                        speech.speak(weather?.let { ruleFor(it).question } ?: "Elige un clima para Ada.")
                    },
                    description = "Repetir la pregunta del clima"
                )
            }

            AdaOutfitStage(
                weather = weather,
                selected = state.selected,
                isComplete = isComplete,
                onBoundsChanged = { targetBounds = it }
            )

            if (weather != null) {
                ClothingChooser(
                    choices = choices,
                    selected = state.selected,
                    targetBounds = targetBounds,
                    onTap = { item ->
                        equipItem(item)
                    },
                    onDrop = { item ->
                        equipItem(item)
                    }
                )
            }

            Spacer(Modifier.height(8.dp))
        }

        ConfettiOverlay(
            modifier = Modifier.fillMaxSize(),
            visible = isComplete,
            burstKey = celebrationKey
        )
    }
}

@Composable
private fun WeatherChooser(
    selected: Weather?,
    modifier: Modifier = Modifier,
    onSelected: (Weather) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        WEATHER_CHOICES.forEach { choice ->
            WeatherChoiceTile(
                choice = choice,
                selected = selected == choice.weather,
                modifier = Modifier.weight(1f),
                onClick = { onSelected(choice.weather) }
            )
        }
    }
}

@Composable
private fun WeatherChoiceTile(
    choice: WeatherChoice,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(23.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = choice.label
                role = Role.Button
            },
        shape = RoundedCornerShape(23.dp),
        color = Color.Transparent,
        shadowElevation = if (selected) 8.dp else 3.dp,
        border = BorderStroke(
            width = if (selected) 3.dp else 1.dp,
            color = if (selected) {
                if (choice.weather == Weather.SOLEADO) Color(0xFFFFA72B) else Color(0xFF329BC1)
            } else Color.White.copy(alpha = 0.95f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(weatherButtonBrush(choice.weather, selected), RoundedCornerShape(23.dp)),
            contentAlignment = Alignment.Center
        ) {
            WeatherIllustration(choice.weather, Modifier.size(38.dp))
            if (selected) {
                Text(
                    text = "✓",
                    color = Color(0xFF247455),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                )
            }
        }
    }
}

private fun weatherButtonBrush(weather: Weather, selected: Boolean): Brush = when (weather) {
    Weather.SOLEADO -> Brush.verticalGradient(
        listOf(
            if (selected) Color(0xFFFFF6B0) else Color(0xFFFFFBE1),
            if (selected) Color(0xFFFFD44F) else Color(0xFFFFDF78),
            Color(0xFFFFAD3A)
        )
    )
    Weather.LLUVIA -> Brush.verticalGradient(
        listOf(
            if (selected) Color(0xFFF4FDFF) else Color.White,
            if (selected) Color(0xFF9BDEEF) else Color(0xFFBFEAF5),
            Color(0xFF62B9DA)
        )
    )
}

@Composable
private fun WeatherIllustration(weather: Weather, modifier: Modifier = Modifier) {
    val illustrationRes = when (weather) {
        Weather.SOLEADO -> R.drawable.weather_sun_3d
        Weather.LLUVIA -> R.drawable.weather_rain_3d
    }
    Image(
        painter = painterResource(illustrationRes),
        contentDescription = if (weather == Weather.SOLEADO) "Sol" else "Lluvia",
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun GameBackButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(50.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = "Volver"
                role = Role.Button
            },
        shape = CircleShape,
        color = Color.Transparent,
        shadowElevation = 7.dp,
        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.96f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF83D6F3), Color(0xFF298EC6))),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            WeatherBackArrow(Modifier.size(29.dp))
        }
    }
}

@Composable
private fun WeatherBackArrow(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val shadowOffset = Offset(w * 0.03f, h * 0.05f)
        val backStem = Offset(w * 0.80f, h * 0.50f)
        val arrowJoint = Offset(w * 0.28f, h * 0.50f)
        val upperTip = Offset(w * 0.53f, h * 0.25f)
        val lowerTip = Offset(w * 0.53f, h * 0.75f)
        drawLine(Color(0xFF276A9C), backStem + shadowOffset, arrowJoint + shadowOffset, w * 0.16f, StrokeCap.Round)
        drawLine(Color(0xFF276A9C), arrowJoint + shadowOffset, upperTip + shadowOffset, w * 0.16f, StrokeCap.Round)
        drawLine(Color(0xFF276A9C), arrowJoint + shadowOffset, lowerTip + shadowOffset, w * 0.16f, StrokeCap.Round)
        drawLine(Color.White, backStem, arrowJoint, w * 0.16f, StrokeCap.Round)
        drawLine(Color.White, arrowJoint, upperTip, w * 0.16f, StrokeCap.Round)
        drawLine(Color.White, arrowJoint, lowerTip, w * 0.16f, StrokeCap.Round)
        drawCircle(Color(0xFFFFE47A), w * 0.055f, Offset(w * 0.80f, h * 0.24f))
    }
}

@Composable
private fun AdaOutfitStage(
    weather: Weather?,
    selected: Set<Item>,
    isComplete: Boolean,
    onBoundsChanged: (Rect) -> Unit
) {
    val effective = effectiveOutfit(selected)
    val visualItems = effective.values.filter { it == Item.PARAGUAS }
    val shortEquipped = weather == Weather.SOLEADO && Item.SHORT in selected
    val sandalsEquipped = weather == Weather.SOLEADO && Item.CHALAS in selected
    val rainJacketEquipped = weather == Weather.LLUVIA && Item.CHAQUETA in selected
    val rainBootsEquipped = weather == Weather.LLUVIA && Item.BOTAS in selected
    val avatarResource = when {
        rainJacketEquipped && rainBootsEquipped -> R.drawable.ada_rain_jacket_boots
        rainJacketEquipped -> R.drawable.ada_rain_jacket
        rainBootsEquipped -> R.drawable.ada_rain_boots
        shortEquipped && sandalsEquipped -> R.drawable.ada_calor_short_chalas
        shortEquipped -> R.drawable.ada_calor_short
        sandalsEquipped -> R.drawable.ada_calor_chalas
        else -> R.drawable.ada_menu_wave
    }
    val replacementDescription = when {
        effective[ClothingSlot.BOTTOM] == Item.SHORT -> "shorts reemplazan el pantalón"
        else -> "pantalón base"
    }
    val footwearDescription = when {
        effective[ClothingSlot.FOOTWEAR] == Item.CHALAS -> "chalas reemplazan las zapatillas"
        effective[ClothingSlot.FOOTWEAR] == Item.BOTAS -> "botas reemplazan las zapatillas"
        else -> "zapatillas base"
    }

    val idleTransition = rememberInfiniteTransition(label = "ada_idle")
    val idleBob by idleTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ada_bob"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(330.dp)
            .onGloballyPositioned { coordinates ->
                val position = coordinates.positionInRoot()
                val avatarWidth = minOf(
                    coordinates.size.width.toFloat(),
                    coordinates.size.height * (2f / 3f)
                )
                val horizontalInset = (coordinates.size.width - avatarWidth) / 2f
                onBoundsChanged(
                    Rect(
                        Offset(position.x + horizontalInset, position.y),
                        Size(avatarWidth, coordinates.size.height.toFloat())
                    )
                )
            }
            .semantics {
                contentDescription = "Ada, con $replacementDescription y $footwearDescription"
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(270.dp)
                .background(
                    Brush.radialGradient(
                        listOf(Color.White.copy(alpha = 0.36f), Color.Transparent)
                    ),
                    CircleShape
                )
        )
        Image(
            painter = painterResource(avatarResource),
            contentDescription = "Ada, arrastra las prendas hasta aquí",
            modifier = Modifier
                .fillMaxSize()
                .offset(y = idleBob.dp),
            contentScale = ContentScale.Fit
        )

        visualItems.forEach { item ->
            val layer = outfitLayer(item) ?: return@forEach
            Image(
                painter = painterResource(layer.drawableRes),
                contentDescription = "Ada lleva ${spokenItemName(item)}",
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = layer.x, y = layer.y + idleBob.dp)
                    .size(width = layer.width, height = layer.height),
                contentScale = ContentScale.Fit
            )
        }

        AnimatedVisibility(
            visible = isComplete,
            modifier = Modifier.align(Alignment.TopEnd).padding(10.dp),
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut()
        ) {
            Text(
                text = "🌟",
                fontSize = 42.sp,
                modifier = Modifier.semantics {
                    contentDescription = "Ada está lista"
                }
            )
        }
    }
}

private data class OutfitLayer(
    val drawableRes: Int,
    val width: Dp,
    val height: Dp,
    val x: Dp = 0.dp,
    val y: Dp = 0.dp
)

private fun outfitLayer(item: Item): OutfitLayer? = when (item) {
    Item.PARAGUAS -> OutfitLayer(
        drawableRes = R.drawable.weather_item_paraguas,
        width = 90.dp,
        height = 108.dp,
        x = 102.dp,
        y = 35.dp
    )
    Item.POLERA, Item.CHAQUETA, Item.BOTAS, Item.SHORT, Item.CHALAS -> null
    else -> null
}

@Composable
private fun ClothingChooser(
    choices: List<ClothingChoice>,
    selected: Set<Item>,
    targetBounds: Rect?,
    onTap: (Item) -> Unit,
    onDrop: (Item) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        choices.chunked(3).forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEachIndexed { index, choice ->
                    ClothingChoiceTile(
                        choice = choice,
                        selected = choice.item in selected,
                        tint = PlayfulCardTints[(rowIndex * 3 + index + 1) % PlayfulCardTints.size],
                        targetBounds = targetBounds,
                        modifier = Modifier.weight(1f),
                        onTap = { onTap(choice.item) },
                        onDrop = { onDrop(choice.item) }
                    )
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ClothingChoiceTile(
    choice: ClothingChoice,
    selected: Boolean,
    tint: Color,
    targetBounds: Rect?,
    modifier: Modifier,
    onTap: () -> Unit,
    onDrop: () -> Unit
) {
    var dragOffset by remember(choice.item) { mutableStateOf(Offset.Zero) }
    var dragOriginCenter by remember(choice.item) { mutableStateOf(Offset.Zero) }
    var isDragging by remember(choice.item) { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .offset { IntOffset(dragOffset.x.roundToInt(), dragOffset.y.roundToInt()) }
            .onGloballyPositioned { coordinates ->
                if (!isDragging) {
                    dragOriginCenter = coordinates.positionInRoot() + Offset(
                        coordinates.size.width / 2f,
                        coordinates.size.height / 2f
                    )
                }
            }
            .zIndex(if (isDragging) 10f else 0f)
            .pointerInput(choice.item, targetBounds) {
                detectDragGestures(
                    onDragStart = {
                        isDragging = true
                    },
                    onDragEnd = {
                        val dropCenter = dragOriginCenter + dragOffset
                        if (targetBounds?.contains(dropCenter) == true) onDrop()
                        dragOffset = Offset.Zero
                        isDragging = false
                    },
                    onDragCancel = {
                        dragOffset = Offset.Zero
                        isDragging = false
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffset += dragAmount
                    }
                )
            }
            .clickable(role = Role.Button, onClick = onTap)
            .semantics {
                contentDescription = if (selected) {
                    "${choice.label}, puesta en Ada. Toca o arrastra otra prenda para cambiarla."
                } else {
                    "${choice.label}. Toca o arrastra hasta Ada para ponérsela."
                }
                role = Role.Button
            },
        shape = RoundedCornerShape(27.dp),
        color = tint.copy(alpha = if (selected || isDragging) 1f else 0.90f),
        shadowElevation = if (isDragging || selected) 9.dp else 4.dp,
        border = BorderStroke(
            width = if (selected || isDragging) 3.dp else 1.dp,
            color = if (selected || isDragging) Color(0xFFFFB83D) else Color.White.copy(alpha = 0.96f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp),
            contentAlignment = Alignment.Center
        ) {
            val illustrationRes = choice.illustrationRes
            if (illustrationRes != null) {
                Image(
                    painter = painterResource(illustrationRes),
                    contentDescription = null,
                    modifier = Modifier.size(78.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text(choice.emoji, fontSize = 47.sp)
            }
            if (selected) {
                Text(
                    text = "✓",
                    color = Color(0xFF287650),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                )
            }
        }
    }
}
