package com.m.adacolorytrazo.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.m.adacolorytrazo.R
import kotlin.math.roundToInt

private data class BodyPart(
    val id: String,
    val drawableName: String,
    val visualScale: Float = 1f
)

private val bodyOffsetsSaver = listSaver<Map<String, androidx.compose.ui.geometry.Offset>, Any>(
    save = { offsets ->
        offsets.flatMap { (id, point) -> listOf(id, point.x, point.y) }
    },
    restore = { saved ->
        saved.chunked(3).mapNotNull { row ->
            val id = row.getOrNull(0) as? String ?: return@mapNotNull null
            val x = (row.getOrNull(1) as? Number)?.toFloat() ?: return@mapNotNull null
            val y = (row.getOrNull(2) as? Number)?.toFloat() ?: return@mapNotNull null
            id to androidx.compose.ui.geometry.Offset(x, y)
        }.toMap()
    }
)

@Composable
fun BodyAssembleScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    HideBodySystemBars()

    // Todas las piezas proceden de la Ada aprobada: cabello oscuro, polera roja,
    // pantalón azul marino y zapatillas rojas. El rostro base queda vacío.
    val parts = remember {
        listOf(
            BodyPart("cara", "body_demo_face"),
            BodyPart("cabello", "body_demo_hair", 1.10f),
            BodyPart("oreja_izq", "body_demo_ear_left", 0.42f),
            BodyPart("oreja_der", "body_demo_ear_right", 0.42f),
            BodyPart("ojos", "body_demo_eyes", 0.75f),
            BodyPart("nariz", "body_demo_nose", 0.33f),
            BodyPart("boca", "body_demo_mouth", 0.40f),
            BodyPart("torso", "body_demo_torso", 1.07f),
            BodyPart("brazo_izq", "body_demo_arm_left", 0.90f),
            BodyPart("brazo_der", "body_demo_arm_right"),
            BodyPart("pierna_izq", "body_demo_leg_left_boot", 1.05f),
            BodyPart("pierna_der", "body_demo_leg_right_boot", 1.05f)
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(0.dp))
    ) {
        val widthPx = with(density) { maxWidth.toPx() }.roundToInt().coerceAtLeast(1)
        val heightPx = with(density) { maxHeight.toPx() }.roundToInt().coerceAtLeast(1)
        val backgroundScale = minOf(widthPx / 941f, heightPx / 1672f)
        val backgroundInsetLeftPx = ((widthPx - 941f * backgroundScale) / 2f).roundToInt()
        val backgroundInsetTopPx = ((heightPx - 1672f * backgroundScale) / 2f).roundToInt()
        val boardTopPx = (heightPx * 0.685f).roundToInt()
        val boardHeightPx = (heightPx - boardTopPx - with(density) { 8.dp.toPx() }.roundToInt())
            .coerceAtLeast(1)
        val horizontalMarginPx = with(density) { 9.dp.toPx() }.roundToInt()
        val boardInsetPx = with(density) { 10.dp.toPx() }.roundToInt()
        val gapPx = with(density) { 4.dp.toPx() }.roundToInt()
        val columns = 4
        val rows = 3
        val cellWidthPx = (
            widthPx - horizontalMarginPx * 2 - boardInsetPx * 2 - gapPx * (columns - 1)
        ).div(columns).coerceAtLeast(with(density) { 38.dp.toPx() }.roundToInt())
        val cellHeightPx = (
            boardHeightPx - boardInsetPx * 2 - gapPx * (rows - 1)
        ).div(rows).coerceAtLeast(with(density) { 48.dp.toPx() }.roundToInt())
        val tileLeftPx = horizontalMarginPx + boardInsetPx
        val tileTopPx = boardTopPx + boardInsetPx
        val maxDragX = (widthPx - cellWidthPx).coerceAtLeast(0).toFloat()
        val maxDragY = (heightPx - cellHeightPx).coerceAtLeast(0).toFloat()

        val initialOffsets = remember(widthPx, boardTopPx, cellWidthPx, cellHeightPx, gapPx) {
            parts.mapIndexed { index, part ->
                val column = index % columns
                val row = index / columns
                part.id to androidx.compose.ui.geometry.Offset(
                    x = (tileLeftPx + column * (cellWidthPx + gapPx)).toFloat(),
                    y = (tileTopPx + row * (cellHeightPx + gapPx)).toFloat()
                )
            }.toMap()
        }
        var offsets by rememberSaveable(stateSaver = bodyOffsetsSaver) {
            mutableStateOf(emptyMap())
        }
        LaunchedEffect(initialOffsets) {
            if (offsets.isEmpty()) offsets = initialOffsets
        }

        val gardenPainter = painterResource(R.drawable.body_garden_demo)
        Image(
            painter = gardenPainter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .scale(1.06f)
                .blur(24.dp),
            contentScale = ContentScale.Crop
        )
        Image(
            painter = gardenPainter,
            contentDescription = "Ada vacía en el jardín, lista para armar con las piezas",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // Bandeja compacta de 4 × 3: mantiene áreas táctiles amplias y dibuja
        // las piezas con el tamaño relativo que tienen en la Ada original.
        Box(
            modifier = Modifier
                .offset { IntOffset(0, boardTopPx) }
                .fillMaxWidth()
                .height(with(density) { boardHeightPx.toDp() })
                .padding(horizontal = with(density) { horizontalMarginPx.toDp() })
                .shadow(10.dp, RoundedCornerShape(30.dp))
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFF32A849))
                .padding(7.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFF8D293), Color(0xFFE5AC62), Color(0xFFD9954B))
                    )
                )
        )

        // La flecha ya está dibujada en el arte aprobado; este punto mantiene
        // su interacción sin superponer un segundo icono.
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset { IntOffset(backgroundInsetLeftPx, backgroundInsetTopPx) }
                .padding(start = 8.dp, top = 8.dp)
                .size(78.dp)
                .clickable(role = Role.Button, onClick = onBack)
                .semantics { contentDescription = "Volver a Jugar con Ada" }
                .background(Color.Transparent)
        )

        parts.forEach { part ->
            val resourceId = remember(part.drawableName) {
                context.resources.getIdentifier(part.drawableName, "drawable", context.packageName)
                    .takeIf { it != 0 } ?: R.drawable.ic_launcher_foreground
            }
            val position = offsets[part.id] ?: initialOffsets[part.id]
                ?: androidx.compose.ui.geometry.Offset.Zero

            Image(
                painter = painterResource(resourceId),
                contentDescription = "Pieza: ${part.id.replace('_', ' ')}. Arrastra para colocarla en Ada",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .offset { IntOffset(position.x.roundToInt(), position.y.roundToInt()) }
                    .size(
                        width = with(density) { cellWidthPx.toDp() },
                        height = with(density) { cellHeightPx.toDp() }
                    )
                    .graphicsLayer {
                        scaleX = part.visualScale
                        scaleY = part.visualScale
                    }
                    .pointerInput(part.id, maxDragX, maxDragY) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsets = offsets.toMutableMap().also { map ->
                                val current = map[part.id] ?: position
                                map[part.id] = androidx.compose.ui.geometry.Offset(
                                    x = (current.x + dragAmount.x).coerceIn(0f, maxDragX),
                                    y = (current.y + dragAmount.y).coerceIn(0f, maxDragY)
                                )
                            }
                        }
                    }
            )
        }
    }
}

@Composable
private fun HideBodySystemBars() {
    val view = LocalView.current
    DisposableEffect(view) {
        val activity = view.context.findBodyActivity()
        val controller = activity?.let { WindowInsetsControllerCompat(it.window, view) }
        val previousBehavior = controller?.systemBarsBehavior
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            controller?.show(WindowInsetsCompat.Type.systemBars())
            if (previousBehavior != null) controller.systemBarsBehavior = previousBehavior
        }
    }
}

private tailrec fun Context.findBodyActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findBodyActivity()
    else -> null
}
