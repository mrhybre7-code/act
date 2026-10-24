package com.m.adacolorytrazo.coloring

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.graphics.PointF
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.m.adacolorytrazo.R
import com.m.adacolorytrazo.ui.PlayfulAdaRounded
import com.m.adacolorytrazo.util.ImageSaveResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

private data class PaletteColor(val value: Int, val name: String)
private enum class ColoringTool { Brush, Fill, Eraser }

private val COLORING_PALETTE = listOf(
    PaletteColor(android.graphics.Color.BLACK, "negro"),
    PaletteColor(android.graphics.Color.DKGRAY, "gris oscuro"),
    PaletteColor(android.graphics.Color.LTGRAY, "gris claro"),
    PaletteColor(android.graphics.Color.WHITE, "blanco"),
    PaletteColor("#E53935".toColorInt(), "rojo"),
    PaletteColor("#FB8C00".toColorInt(), "naranja"),
    PaletteColor("#FDD835".toColorInt(), "amarillo"),
    PaletteColor("#43A047".toColorInt(), "verde"),
    PaletteColor("#1E88E5".toColorInt(), "azul"),
    PaletteColor("#8E24AA".toColorInt(), "violeta"),
    PaletteColor("#F06292".toColorInt(), "rosado"),
    PaletteColor("#795548".toColorInt(), "café")
)

@Suppress("DiscouragedApi")
@Composable
fun ColoringScreen(
    baseName: String,
    title: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHost = remember { SnackbarHostState() }
    val bwRes = remember(baseName) {
        context.resources.getIdentifier("${baseName}_bw", "drawable", context.packageName)
    }

    if (bwRes == 0) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("No se encontró el dibujo: ${baseName}_bw", color = Color.Red)
        }
        return
    }

    val model: ColoringViewModel = viewModel(key = "coloring-editor")
    val ui = model.state
    var imageBoxSize by remember { mutableStateOf(IntSize.Zero) }
    var selectedToolName by rememberSaveable { mutableStateOf(ColoringTool.Brush.name) }
    val selectedTool = ColoringTool.valueOf(selectedToolName)
    val brushSizeDp = if (selectedTool == ColoringTool.Eraser) 30 else 20
    var activeStroke by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var touchPoint by remember { mutableStateOf<Offset?>(null) }
    var touchFeedbackKey by remember { mutableIntStateOf(0) }
    val density = LocalDensity.current

    DisposableEffect(model, baseName) {
        onDispose { model.cancelWork() }
    }
    LaunchedEffect(baseName, bwRes) {
        model.initialize(context.applicationContext, bwRes)
    }
    LaunchedEffect(ui.error) {
        ui.error?.let { snackbarHost.showSnackbar(it) }
    }

    fun performSave() {
        scope.launch {
            try {
                when (val result = model.save(
                    context.applicationContext,
                    "${baseName}_${System.currentTimeMillis()}.png"
                )) {
                    ImageSaveResult.Success -> snackbarHost.showSnackbar("Dibujo guardado en la galería")
                    is ImageSaveResult.Failure -> snackbarHost.showSnackbar("No se pudo guardar: ${result.reason}")
                }
            } catch (_: CancellationException) {
                // Una rotación o salida cancela la operación y ImageSaver ya limpió su URI.
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            performSave()
        } else {
            scope.launch {
                snackbarHost.showSnackbar("Se necesita permiso de almacenamiento para guardar")
            }
        }
    }

    fun requestSave() {
        if (ui.isBusy) return
        if (
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            performSave()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHost) }
        ) { padding ->
            val bitmap = ui.bitmap
            if (bitmap == null) {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    if (ui.isLoading) CircularProgressIndicator()
                    else Text(ui.error ?: "Cargando dibujo…")
                }
            } else {
                val bitmapWidth = bitmap.width
                val bitmapHeight = bitmap.height
                val drawScale = if (imageBoxSize.width > 0 && imageBoxSize.height > 0) {
                    min(
                        imageBoxSize.width.toFloat() / bitmapWidth,
                        imageBoxSize.height.toFloat() / bitmapHeight
                    )
                } else 0f
                val brushWidthPx = with(density) { brushSizeDp.dp.toPx() }
                val brushWidthBitmap = if (drawScale > 0f) brushWidthPx / drawScale else 0f
                Column(
                    Modifier.fillMaxSize().padding(padding).padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (baseName == "barco") ColoringBoatCompanion()

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White.copy(alpha = 0.88f),
                        shadowElevation = 6.dp,
                        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.95f))
                    ) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth().height(62.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            items(COLORING_PALETTE, key = { it.name }) { color ->
                                ColoringPaletteSwatch(
                                    color = color,
                                    selected = ui.selectedColor == color.value,
                                    enabled = !ui.isBusy,
                                    onClick = { model.selectColor(color.value) }
                                )
                            }
                        }
                    }

                    LazyRow(
                        modifier = Modifier.fillMaxWidth().height(72.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        item {
                            ColoringToolButton(
                                icon = {
                                    Image(
                                        painter = painterResource(R.drawable.tool_brush_3d),
                                        contentDescription = null,
                                        modifier = Modifier.size(42.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                },
                                description = "Pincel",
                                selected = selectedTool == ColoringTool.Brush,
                                enabled = !ui.isBusy,
                                accent = Color(0xFFE76E5D),
                                onClick = { selectedToolName = ColoringTool.Brush.name }
                            )
                        }
                        item {
                            ColoringToolButton(
                                icon = {
                                    Image(
                                        painter = painterResource(R.drawable.tool_bucket_3d),
                                        contentDescription = null,
                                        modifier = Modifier.size(42.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                },
                                description = "Bote de pintura",
                                selected = selectedTool == ColoringTool.Fill,
                                enabled = !ui.isBusy,
                                accent = Color(0xFF329BC1),
                                onClick = { selectedToolName = ColoringTool.Fill.name }
                            )
                        }
                        item {
                            ColoringToolButton(
                                icon = {
                                    Image(
                                        painter = painterResource(R.drawable.tool_eraser_3d),
                                        contentDescription = null,
                                        modifier = Modifier.size(42.dp),
                                        contentScale = ContentScale.Fit
                                    )
                                },
                                description = "Goma de borrar",
                                selected = selectedTool == ColoringTool.Eraser,
                                enabled = !ui.isBusy,
                                accent = Color(0xFF9A6CC3),
                                onClick = { selectedToolName = ColoringTool.Eraser.name }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(18.dp))
                                .clickable(enabled = !ui.isBusy, onClick = ::requestSave)
                                .semantics { contentDescription = "Guardar dibujo" },
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFF39A975),
                            shadowElevation = 5.dp,
                            border = BorderStroke(2.dp, Color.White)
                        ) {
                            Text(
                                text = if (ui.isBusy) "Guardando…" else "Guardar dibujo",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                color = Color.White,
                                fontFamily = PlayfulAdaRounded,
                                fontSize = 11.sp,
                                lineHeight = 14.sp,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .shadow(14.dp, RoundedCornerShape(26.dp), clip = false)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color.White)
                            .border(4.dp, Color.White.copy(alpha = 0.96f), RoundedCornerShape(22.dp))
                            .onGloballyPositioned { imageBoxSize = it.size }
                            .pointerInput(selectedTool, ui.selectedColor, ui.isBusy, brushSizeDp, bitmap, imageBoxSize) {
                                if (ui.isBusy || drawScale <= 0f) return@pointerInput
                                val drawW = bitmapWidth * drawScale
                                val drawH = bitmapHeight * drawScale
                                val dx = (imageBoxSize.width - drawW) / 2f
                                val dy = (imageBoxSize.height - drawH) / 2f

                                if (selectedTool == ColoringTool.Fill) {
                                    detectTapGestures { tap ->
                                        touchPoint = tap
                                        touchFeedbackKey++
                                        val bx = ((tap.x - dx) / drawScale).toInt()
                                        val by = ((tap.y - dy) / drawScale).toInt()
                                        if (bx in 0 until bitmapWidth && by in 0 until bitmapHeight) {
                                            model.fillAt(bx, by)
                                        }
                                    }
                                } else {
                                    detectDragGestures(
                                        onDragStart = {
                                            activeStroke = listOf(it)
                                            touchPoint = it
                                            touchFeedbackKey++
                                        },
                                        onDrag = { change, _ ->
                                            activeStroke = activeStroke + change.position
                                            change.consume()
                                        },
                                        onDragEnd = {
                                            val bitmapPoints = activeStroke.map { point ->
                                                PointF((point.x - dx) / drawScale, (point.y - dy) / drawScale)
                                            }
                                            activeStroke = emptyList()
                                            if (bitmapPoints.isNotEmpty()) {
                                                when (selectedTool) {
                                                    ColoringTool.Brush -> model.paintStroke(bitmapPoints, brushWidthBitmap)
                                                    ColoringTool.Eraser -> model.eraseStroke(bitmapPoints, brushWidthBitmap)
                                                    ColoringTool.Fill -> Unit
                                                }
                                            }
                                        },
                                        onDragCancel = { activeStroke = emptyList() }
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                        if (activeStroke.isNotEmpty()) {
                            Canvas(Modifier.fillMaxSize()) {
                                val previewColor = if (selectedTool == ColoringTool.Eraser) {
                                    Color.White.copy(alpha = 0.96f)
                                } else {
                                    Color(ui.selectedColor)
                                }
                                if (activeStroke.size == 1) {
                                    drawCircle(
                                        color = previewColor,
                                        radius = brushWidthPx / 2f,
                                        center = activeStroke.first()
                                    )
                                } else {
                                    val path = Path().apply {
                                        moveTo(activeStroke.first().x, activeStroke.first().y)
                                        activeStroke.drop(1).forEach { lineTo(it.x, it.y) }
                                    }
                                    drawPath(
                                        path = path,
                                        color = previewColor,
                                        style = Stroke(
                                            width = brushWidthPx,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )
                                }
                            }
                        }
                        ColoringTouchSparkle(point = touchPoint, trigger = touchFeedbackKey)
                        if (ui.isBusy) {
                            Surface(
                                modifier = Modifier.align(Alignment.Center),
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.9f),
                                shadowElevation = 6.dp
                            ) {
                                CircularProgressIndicator(Modifier.padding(13.dp))
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
private fun ColoringBoatCompanion() {
    val density = LocalDensity.current
    val transition = rememberInfiniteTransition(label = "boat-companion")
    val bob by transition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1250, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "boat-bob"
    )
    Box(
        modifier = Modifier.fillMaxWidth().height(50.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Image(
            painter = painterResource(R.drawable.barco_icon_3d),
            contentDescription = "Barquito compañero",
            modifier = Modifier
                .size(64.dp)
                .graphicsLayer { translationY = bob * density.density }
                .semantics { contentDescription = "Barquito compañero animado" },
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun ColoringPaletteSwatch(
    color: PaletteColor,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 500f),
        label = "palette-pop-${color.name}"
    )
    Surface(
        modifier = Modifier
            .size(54.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = "Color ${color.name}" },
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.92f),
        shadowElevation = if (selected) 8.dp else 3.dp,
        border = BorderStroke(
            if (selected) 3.dp else 1.dp,
            if (selected) Color(0xFFFFC857) else Color.White
        )
    ) {
        Box(contentAlignment = Alignment.Center) {
            Canvas(Modifier.size(38.dp)) {
                val paintColor = Color(color.value)
                val shadowColor = Color(
                    paintColor.red * 0.68f,
                    paintColor.green * 0.68f,
                    paintColor.blue * 0.68f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(paintColor.copy(alpha = 0.88f), paintColor, shadowColor),
                        center = Offset(size.width * 0.34f, size.height * 0.29f),
                        radius = size.minDimension * 0.80f
                    )
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.66f),
                    radius = size.minDimension * 0.085f,
                    center = Offset(size.width * 0.30f, size.height * 0.25f)
                )
            }
        }
    }
}

@Composable
private fun ColoringToolButton(
    icon: @Composable () -> Unit,
    description: String,
    selected: Boolean,
    enabled: Boolean,
    accent: Color,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "tool-pop-$description"
    )
    Surface(
        modifier = Modifier
            .size(width = 78.dp, height = 68.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clickable(enabled = enabled, onClick = onClick)
            .semantics { contentDescription = description },
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color(0xFFFFF1C7) else Color.White.copy(alpha = 0.92f),
        contentColor = accent,
        shadowElevation = if (selected) 8.dp else 4.dp,
        border = BorderStroke(
            if (selected) 3.dp else 1.dp,
            if (selected) Color(0xFFFFC857) else Color.White
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) { icon() }
            Text(
                text = if (description == "Bote de pintura") "Bote" else description,
                color = accent,
                fontFamily = PlayfulAdaRounded,
                fontSize = 9.sp,
                lineHeight = 11.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                maxLines = 1
            )
            }
        }
}

@Composable
private fun ColoringTouchSparkle(point: Offset?, trigger: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            visible = false
            delay(16)
            visible = true
            delay(430)
            visible = false
        }
    }
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(260, easing = FastOutSlowInEasing),
        label = "touch-sparkle"
    )
    if (point != null && progress > 0.01f) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = 15f + 23f * progress
            val alpha = (1f - progress) * 0.9f
            drawCircle(
                color = Color(0xFFFFC857).copy(alpha = alpha),
                radius = radius,
                center = point,
                style = Stroke(width = 4f)
            )
            val arm = radius * 0.72f
            drawLine(Color.White.copy(alpha = alpha), Offset(point.x - arm, point.y), Offset(point.x + arm, point.y), 3f, StrokeCap.Round)
            drawLine(Color.White.copy(alpha = alpha), Offset(point.x, point.y - arm), Offset(point.x, point.y + arm), 3f, StrokeCap.Round)
        }
    }
}
