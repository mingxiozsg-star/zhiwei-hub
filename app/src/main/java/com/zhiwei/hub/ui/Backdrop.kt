package com.zhiwei.hub.ui

import android.graphics.BitmapFactory
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zhiwei.hub.data.Prefs

/** 读取用户选择的图片；失败返回 null（自动回退渐变）。 */
@Composable
fun rememberBackdropBitmap(uri: String?): ImageBitmap? {
    val ctx = LocalContext.current
    return remember(uri) {
        if (uri.isNullOrBlank()) return@remember null
        runCatching {
            ctx.contentResolver.openInputStream(android.net.Uri.parse(uri))?.use { input ->
                val opts = BitmapFactory.Options().apply {
                    inSampleSize = 2
                    inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                }
                BitmapFactory.decodeStream(input, null, opts)?.asImageBitmap()
            }
        }.getOrNull()
    }
}

/**
 * 全局背景：渐变 / 图片 / 纯色三选一，外加暗化与模糊。
 * 模糊走 Modifier.blur（API 31+ 硬件渲染层），低版本自动退化。
 */
@Composable
fun Backdrop(prefs: Prefs, modifier: Modifier = Modifier) {
    val mode = prefs.bgMode
    val bitmap = rememberBackdropBitmap(prefs.bgUri)
    val grad = Prefs.GRADIENTS.getOrElse(prefs.bgGradient) { Prefs.GRADIENTS[0] }.second
    val dim = prefs.bgDim.coerceIn(0f, 0.92f)
    val blurRadius = prefs.bgBlur.coerceIn(0f, 25f)

    Box(modifier.fillMaxSize().background(ZW.Bg)) {
        when {
            mode == "image" && bitmap != null -> {
                val m = if (blurRadius > 0.5f && Build.VERSION.SDK_INT >= 31) {
                    Modifier.fillMaxSize().blur(blurRadius.dp)
                } else Modifier.fillMaxSize()
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = m
                )
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = dim)))
            }
            mode == "solid" -> {
                Box(Modifier.fillMaxSize().background(Color(grad[0])))
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = dim * 0.5f)))
            }
            else -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = grad.map { Color(it) }
                            )
                        )
                )
                Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = dim)))
                // 顶部霓虹光斑
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Color(grad[1]).copy(alpha = 0.5f), Color.Transparent),
                                center = androidx.compose.ui.geometry.Offset(120f, 0f),
                                radius = 900f
                            )
                        )
                )
            }
        }
    }
}

