package com.zhiwei.hub.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer

/**
 * 子页面（详情 / 外观 / 关于）——覆盖在三个平级 tab 之上。
 * 从右侧滑入，无黑色遮罩（背景壁纸保持可见）。
 */
sealed interface Route {
    data class Detail(val key: String) : Route
    data object Appearance : Route
    data object About : Route
}

private val EASE = CubicBezierEasing(0.2f, 1f, 0.25f, 1f)

@Composable
fun SubOverlay(
    route: Route,
    content: @Composable (Route) -> Unit
) {
    val progress = remember(route) { Animatable(1f) }
    LaunchedEffect(route) {
        progress.snapTo(1f)
        progress.animateTo(0f, tween(320, easing = EASE))
    }
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val w = constraints.maxWidth.toFloat().coerceAtLeast(1f)
        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = w * progress.value
                    transformOrigin = TransformOrigin(0f, 0.5f)
                }
        ) {
            content(route)
        }
    }
}