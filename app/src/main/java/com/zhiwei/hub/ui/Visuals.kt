package com.zhiwei.hub.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 液态玻璃卡片：半透明基底透出壁纸 + 斜向镜面高光 + 上亮下暗边缘反光。
 * 按压时轻微内缩，松手弹性归位 —— 全部走 animateFloatAsState 的可打断语义。
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    accent: Color = Color.Transparent,
    corner: Dp = 22.dp,
    contentPadding: Dp = 16.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val inter = remember { MutableInteractionSource() }
    val pressed by inter.collectIsPressedAsState()
    val s by animateFloatAsState(
        targetValue = if (pressed && onClick != null) 0.982f else 1f,
        label = "cardScale"
    )
    val shape = RoundedCornerShape(corner)

    Box(
        modifier
            .fillMaxWidth()
            .scale(s)
            .clip(shape)
            // 液态玻璃基底：半透明白色渐变，透出壁纸，而非不透明深色盖死
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.05f))
                )
            )
            // 顶部斜向镜面高光（specular highlight）
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.22f),
                        Color.White.copy(alpha = 0.02f),
                        Color.Transparent
                    ),
                    start = Offset.Zero,
                    end = Offset.Infinite,
                    tileMode = androidx.compose.ui.graphics.TileMode.Decal
                )
            )
            // 边缘反光：上亮下暗，模拟玻璃边缘折射
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.30f), Color.White.copy(alpha = 0.05f))
                ),
                shape
            )
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = inter,
                    indication = null,
                    onClick = onClick
                ) else Modifier
            )
    ) {
        if (accent != Color.Transparent) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            0f to Color.Transparent,
                            0.5f to accent.copy(alpha = 0.9f),
                            1f to Color.Transparent
                        )
                    )
            )
        }
        Column(Modifier.padding(contentPadding), content = content)
    }
}
