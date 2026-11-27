package com.zhiwei.hub.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BarTab(val label: String, val glyph: Glyph)

/**
 * 悬浮玻璃底栏：胶囊 + 果冻指示器 + 顶部镜面光。
 * 选中块用背景位移而不是切换，所以「移动」是连绕的。
 */
@Composable
fun GlassBar(
    tabs: List<BarTab>,
    selected: Int,
    accent: Color,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(62.dp)
                .clip(RoundedCornerShape(31.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.13f), Color.White.copy(alpha = 0.05f))
                    )
                )
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.20f),
                            Color.White.copy(alpha = 0.02f),
                            Color.Transparent
                        ),
                        start = androidx.compose.ui.geometry.Offset.Zero,
                        end = androidx.compose.ui.geometry.Offset.Infinite,
                        tileMode = androidx.compose.ui.graphics.TileMode.Decal
                    )
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.32f), Color.White.copy(alpha = 0.06f))
                    ),
                    RoundedCornerShape(31.dp)
                )
        )
        Row(
            Modifier.fillMaxWidth().height(62.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEachIndexed { i, tab ->
                val active = i == selected
                val scale by animateFloatAsState(if (active) 1f else 0.94f, label = "tabScale")
                val inter = remember { MutableInteractionSource() }
                Box(
                    Modifier
                        .weight(1f)
                        .padding(horizontal = 3.dp)
                        .height(46.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(23.dp))
                        .background(if (active) accent.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable(interactionSource = inter, indication = null) { onSelect(i) },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon24(tab.glyph, if (active) accent else ZW.TextFaint, 19.dp)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            tab.label,
                            color = if (active) accent else ZW.TextFaint,
                            fontSize = 9.5.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
