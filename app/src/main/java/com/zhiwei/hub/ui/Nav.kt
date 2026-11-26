package com.zhiwei.hub.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs

sealed interface Route {
    data object Home : Route
    data object InstallCenter : Route
    data object Settings : Route
    data object Appearance : Route
    data object About : Route
    data class Detail(val key: String) : Route
}

class NavController {

    val stack = mutableStateListOf<Route>(Route.Home)

    var exiting by mutableStateOf<Route?>(null)
        internal set

    internal val top = Animatable(0f)

    val canPop: Boolean get() = stack.size > 1

    internal fun push(r: Route) {
        if (stack.lastOrNull() == r) return
        stack.add(r)
    }

    internal fun pop() {
        if (stack.size <= 1) return
        exiting = stack.removeAt(stack.lastIndex)
    }

    fun back() {
        if (stack.size > 1) pop()
    }

    fun goHome() {
        while (stack.size > 1) stack.removeAt(stack.lastIndex)
        exiting = null
    }

    companion object {
        val EASE = CubicBezierEasing(0.2f, 1f, 0.25f, 1f)
        val EASE_OUT = CubicBezierEasing(0.3f, 0.7f, 0f, 1f)
        const val PUSH_MS = 430
        const val POP_MS = 340
        const val COMMIT = 0.32f
    }
}

private val dragProgress = FloatArray(1) { Float.NaN }

@Composable
fun LayeredNavHost(
    nav: NavController,
    modifier: Modifier = Modifier,
    render: @Composable (Route) -> Unit
) {
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    var widthPx by remember { mutableFloatStateOf(1080f) }
    val edge = with(density) { 30.dp.toPx() }
    var pendingExit by remember { mutableStateOf<Route?>(null) }
    val stackSize = nav.stack.size

    LaunchedEffect(nav.exiting, stackSize) {
        val ex = nav.exiting
        if (ex != null) {
            pendingExit = ex
            nav.top.snapTo(stackSize.toFloat())
        }
    }

    LaunchedEffect(stackSize, nav.exiting) {
        val rest = nav.stack.lastIndex.toFloat()
        dragProgress[0] = Float.NaN
        if (abs(nav.top.value - rest) > 0.002f) {
            val forward = rest > nav.top.value
            nav.top.animateTo(
                rest,
                tween(
                    if (forward) NavController.PUSH_MS else NavController.POP_MS,
                    easing = if (forward) NavController.EASE else NavController.EASE_OUT
                )
            )
        }
        if (nav.exiting != null) {
            nav.exiting = null
            pendingExit = null
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(stackSize) {
                if (stackSize <= 1) return@pointerInput
                var dragPx = 0f
                var armed = false
                detectHorizontalDragGestures(
                    onDragStart = { off ->
                        armed = off.x <= edge && abs(nav.top.value - nav.stack.lastIndex) < 0.02f
                        dragPx = 0f
                    },
                    onDragEnd = {
                        if (armed) {
                            val frac = (dragPx / widthPx).coerceIn(0f, 1f)
                            val commit = frac > NavController.COMMIT
                            scope.launch {
                                if (commit) {
                                    nav.top.snapTo(nav.stack.lastIndex - frac)
                                    dragProgress[0] = Float.NaN
                                    nav.pop()
                                } else {
                                    nav.top.animateTo(
                                        nav.stack.lastIndex.toFloat(),
                                        tween(
                                            (NavController.POP_MS * frac).toInt().coerceIn(90, NavController.POP_MS),
                                            easing = NavController.EASE_OUT
                                        )
                                    )
                                    dragProgress[0] = Float.NaN
                                }
                            }
                        }
                        armed = false
                        dragPx = 0f
                    },
                    onDragCancel = {
                        if (armed) {
                            scope.launch {
                                nav.top.animateTo(
                                    nav.stack.lastIndex.toFloat(),
                                    tween(180, easing = NavController.EASE_OUT)
                                )
                                dragProgress[0] = Float.NaN
                            }
                        }
                        armed = false
                        dragPx = 0f
                    },
                    onHorizontalDrag = { change, amount ->
                        if (!armed) return@detectHorizontalDragGestures
                        dragPx = (dragPx + amount).coerceIn(0f, widthPx)
                        change.consume()
                        dragProgress[0] = nav.stack.lastIndex - dragPx / widthPx
                    }
                )
            }
    ) {
        widthPx = constraints.maxWidth.toFloat().coerceAtLeast(1f)

        val exitLayer = nav.exiting ?: pendingExit
        val count = nav.stack.size + if (exitLayer != null) 1 else 0
        val visual = if (dragProgress[0].isNaN()) nav.top.value else dragProgress[0]

        for (i in 0 until count) {
            val route: Route = if (i < nav.stack.size) nav.stack[i] else (exitLayer ?: continue)
            val depth = i - visual
            if (depth < -1.05f || depth > 1.05f) continue

            val behind = depth < 0f
            val tx = if (behind) widthPx * 0.24f * depth else widthPx * depth
            val scale = if (behind) 1f + 0.08f * depth else 1f - 0.045f * depth
            val dim = if (behind) (-depth).coerceIn(0f, 1f) * 0.58f else 0f

            Box(
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = tx
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0.5f)
                    }
                    .drawWithContent {
                        drawContent()
                        if (dim > 0f) drawRect(Color.Black.copy(alpha = dim), size = size)
                    }
            ) {
                render(route)
            }
        }
    }
}
