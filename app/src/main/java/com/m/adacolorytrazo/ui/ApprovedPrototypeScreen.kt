package com.m.adacolorytrazo.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.m.adacolorytrazo.R

/** A normalized, accessibility-labelled touch target over an approved 9:16 reference. */
data class PrototypeHotspot(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val label: String,
    val onClick: () -> Unit
)

/**
 * Displays the approved artwork without rebuilding or altering its pixels. The complete
 * portrait artwork is fitted in the viewport; a blurred crop of the same art fills any
 * extra space on taller phone screens. Invisible Compose hit targets make the cards and
 * play-screen canvases reachable while preserving the image itself.
 */
@Composable
fun ApprovedPrototypeScreen(
    imageRes: Int,
    screenDescription: String,
    hotspots: List<PrototypeHotspot>,
    modifier: Modifier = Modifier
) {
    HideSystemBarsForPrototype()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
    ) {
        val density = LocalDensity.current
        val viewportWidthPx = with(density) { maxWidth.toPx() }
        val viewportHeightPx = with(density) { maxHeight.toPx() }
        val scale = minOf(viewportWidthPx / 1440f, viewportHeightPx / 2560f)
        val renderedWidthPx = 1440f * scale
        val renderedHeightPx = 2560f * scale
        val insetLeftPx = (viewportWidthPx - renderedWidthPx) / 2f
        val insetTopPx = (viewportHeightPx - renderedHeightPx) / 2f

        Image(
            painter = painterResource(imageRes),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .scale(1.06f)
                .blur(24.dp),
            contentScale = ContentScale.Crop
        )
        Image(
            painter = painterResource(imageRes),
            contentDescription = screenDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        hotspots.forEach { hotspot ->
            val leftPx = insetLeftPx + hotspot.left.coerceIn(0f, 1f) * renderedWidthPx
            val topPx = insetTopPx + hotspot.top.coerceIn(0f, 1f) * renderedHeightPx
            val widthPx = (hotspot.right - hotspot.left).coerceAtLeast(0f) * renderedWidthPx
            val heightPx = (hotspot.bottom - hotspot.top).coerceAtLeast(0f) * renderedHeightPx

            Box(
                modifier = Modifier
                    .offset(
                        x = with(density) { leftPx.toDp() },
                        y = with(density) { topPx.toDp() }
                    )
                    .size(
                        width = with(density) { widthPx.toDp() },
                        height = with(density) { heightPx.toDp() }
                    )
                    .clickable(role = Role.Button, onClick = hotspot.onClick)
                    .semantics {
                        contentDescription = hotspot.label
                    }
            )
        }
    }
}

@Composable
private fun HideSystemBarsForPrototype() {
    val view = LocalView.current
    DisposableEffect(view) {
        val activity = view.context.findActivity()
        val controller = activity?.let { WindowInsetsControllerCompat(it.window, view) }
        val previousBehavior = controller?.systemBarsBehavior
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            controller?.show(WindowInsetsCompat.Type.systemBars())
            if (previousBehavior != null) controller?.systemBarsBehavior = previousBehavior
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun approvedImageForScreen(screen: String): Int = when (screen) {
    "main" -> R.drawable.approved_01_menu
    "create" -> R.drawable.approved_02_create
    "discover" -> R.drawable.approved_03_discover
    "play" -> R.drawable.approved_04_play_ada
    "body" -> R.drawable.approved_05_body
    "memorice" -> R.drawable.approved_06_memorice
    "weather" -> R.drawable.approved_07_weather
    "free_draw" -> R.drawable.approved_08_free_draw
    else -> error("Unknown approved prototype: $screen")
}
