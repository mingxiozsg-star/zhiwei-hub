package com.zhiwei.hub.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** 极简线性图标：全部 canvas 绘制，不引第三方图标库。 */
enum class Glyph { BACK, REFRESH, SETTINGS, DOWNLOAD, CHECK, PLAY, IMAGE, INFO, PALETTE, LINK, TRASH, CLOSE, STOP, ROCKET }

@Composable
fun Icon24(g: Glyph, tint: Color = ZW.Text, size: Dp = 20.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val sw = w * 0.085f
        val st = Stroke(width = sw, cap = StrokeCap.Round)
        when (g) {
            Glyph.BACK -> drawPath(Path().apply {
                moveTo(w * 0.62f, h * 0.18f); lineTo(w * 0.3f, h * 0.5f); lineTo(w * 0.62f, h * 0.82f)
            }, tint, style = st)
            Glyph.CLOSE -> {
                drawLine(tint, Offset(w * 0.26f, h * 0.26f), Offset(w * 0.74f, h * 0.74f), sw, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.74f, h * 0.26f), Offset(w * 0.26f, h * 0.74f), sw, StrokeCap.Round)
            }
            Glyph.REFRESH -> {
                drawArc(tint, 40f, 280f, false, topLeft = Offset(w * 0.16f, h * 0.16f),
                    size = Size(w * 0.68f, h * 0.68f), style = st)
                drawPath(Path().apply {
                    moveTo(w * 0.78f, h * 0.2f); lineTo(w * 0.9f, h * 0.36f); lineTo(w * 0.7f, h * 0.4f)
                }, tint, style = Stroke(sw, cap = StrokeCap.Round))
            }
            Glyph.SETTINGS -> {
                drawCircle(tint, w * 0.16f, Offset(w * 0.5f, h * 0.5f), style = st)
                for (i in 0 until 8) {
                    val a = Math.toRadians(i * 45.0)
                    val cx = w * 0.5f; val cy = h * 0.5f
                    val r1 = w * 0.3f; val r2 = w * 0.42f
                    drawLine(tint,
                        Offset(cx + (Math.cos(a) * r1).toFloat(), cy + (Math.sin(a) * r1).toFloat()),
                        Offset(cx + (Math.cos(a) * r2).toFloat(), cy + (Math.sin(a) * r2).toFloat()),
                        sw, StrokeCap.Round)
                }
            }
            Glyph.DOWNLOAD -> {
                drawLine(tint, Offset(w * 0.5f, h * 0.18f), Offset(w * 0.5f, h * 0.62f), sw, StrokeCap.Round)
                drawPath(Path().apply {
                    moveTo(w * 0.32f, h * 0.46f); lineTo(w * 0.5f, h * 0.66f); lineTo(w * 0.68f, h * 0.46f)
                }, tint, style = st)
                drawLine(tint, Offset(w * 0.24f, h * 0.82f), Offset(w * 0.76f, h * 0.82f), sw, StrokeCap.Round)
            }
            Glyph.CHECK -> drawPath(Path().apply {
                moveTo(w * 0.24f, h * 0.53f); lineTo(w * 0.43f, h * 0.72f); lineTo(w * 0.78f, h * 0.28f)
            }, tint, style = st)
            Glyph.PLAY -> drawPath(Path().apply {
                moveTo(w * 0.36f, h * 0.22f); lineTo(w * 0.8f, h * 0.5f); lineTo(w * 0.36f, h * 0.78f); close()
            }, tint)
            Glyph.STOP -> drawRoundRect(tint, Offset(w * 0.28f, h * 0.28f), Size(w * 0.44f, h * 0.44f),
                androidx.compose.ui.geometry.CornerRadius(w * 0.08f))
            Glyph.IMAGE -> {
                drawRoundRect(tint, Offset(w * 0.16f, h * 0.2f), Size(w * 0.68f, h * 0.6f),
                    androidx.compose.ui.geometry.CornerRadius(w * 0.1f), style = st)
                drawCircle(tint, w * 0.06f, Offset(w * 0.33f, h * 0.36f))
                drawPath(Path().apply {
                    moveTo(w * 0.2f, h * 0.74f); lineTo(w * 0.42f, h * 0.48f); lineTo(w * 0.6f, h * 0.7f);
                    lineTo(w * 0.7f, h * 0.6f); lineTo(w * 0.8f, h * 0.74f)
                }, tint, style = st)
            }
            Glyph.INFO -> {
                drawCircle(tint, w * 0.4f, Offset(w * 0.5f, h * 0.5f), style = st)
                drawCircle(tint, w * 0.045f, Offset(w * 0.5f, h * 0.32f))
                drawLine(tint, Offset(w * 0.5f, h * 0.44f), Offset(w * 0.5f, h * 0.68f), sw, StrokeCap.Round)
            }
            Glyph.PALETTE -> {
                drawCircle(tint, w * 0.4f, Offset(w * 0.5f, h * 0.5f), style = st)
                drawCircle(tint, w * 0.055f, Offset(w * 0.38f, h * 0.36f))
                drawCircle(tint, w * 0.055f, Offset(w * 0.62f, h * 0.38f))
                drawCircle(tint, w * 0.055f, Offset(w * 0.36f, h * 0.6f))
            }
            Glyph.LINK -> {
                drawLine(tint, Offset(w * 0.42f, h * 0.42f), Offset(w * 0.58f, h * 0.58f), sw, StrokeCap.Round)
                drawArc(tint, 200f, 130f, false, Offset(w * 0.14f, h * 0.4f), Size(w * 0.42f, h * 0.42f), style = st)
                drawArc(tint, 20f, 130f, false, Offset(w * 0.44f, h * 0.18f), Size(w * 0.42f, h * 0.42f), style = st)
            }
            Glyph.TRASH -> {
                drawLine(tint, Offset(w * 0.2f, h * 0.28f), Offset(w * 0.8f, h * 0.28f), sw, StrokeCap.Round)
                drawPath(Path().apply {
                    moveTo(w * 0.28f, h * 0.28f); lineTo(w * 0.33f, h * 0.82f); lineTo(w * 0.67f, h * 0.82f);
                    lineTo(w * 0.72f, h * 0.28f)
                }, tint, style = st)
                drawLine(tint, Offset(w * 0.4f, h * 0.44f), Offset(w * 0.42f, h * 0.7f), sw * 0.8f, StrokeCap.Round)
                drawLine(tint, Offset(w * 0.6f, h * 0.44f), Offset(w * 0.58f, h * 0.7f), sw * 0.8f, StrokeCap.Round)
            }
            Glyph.ROCKET -> {
                drawPath(Path().apply {
                    moveTo(w * 0.5f, h * 0.14f)
                    cubicTo(w * 0.78f, h * 0.3f, w * 0.78f, h * 0.56f, w * 0.5f, h * 0.72f)
                    cubicTo(w * 0.22f, h * 0.56f, w * 0.22f, h * 0.3f, w * 0.5f, h * 0.14f)
                }, tint, style = st)
                drawCircle(tint, w * 0.07f, Offset(w * 0.5f, h * 0.4f))
            }
        }
    }
}

/** 状态点：运行时带呼吸光晕 */
@Composable
fun StatusDot(color: Color, pulse: Boolean, size: Dp = 8.dp) {
    val alpha by animateFloatAsState(if (pulse) 1f else 0.55f, label = "dot")
    Box(contentAlignment = Alignment.Center) {
        if (pulse) {
            Box(
                Modifier
                    .size(size * 2.4f)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(color.copy(alpha = 0.38f * alpha), Color.Transparent)))
            )
        }
        Box(Modifier.size(size).clip(CircleShape).background(color))
    }
}

@Composable
fun PrimaryButton(
    text: String,
    accent: Color,
    modifier: Modifier = Modifier,
    glyph: Glyph? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val inter = remember { MutableInteractionSource() }
    val pressed by inter.collectIsPressedAsState()
    val s by animateFloatAsState(if (pressed) 0.96f else 1f, label = "btn")
    val shape = RoundedCornerShape(15.dp)
    Box(
        modifier
            .scale(s)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    listOf(accent.copy(alpha = if (enabled) 0.95f else 0.3f),
                        accent.copy(alpha = if (enabled) 0.62f else 0.2f))
                )
            )
            .clickable(interactionSource = inter, indication = null, enabled = enabled, onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (glyph != null) {
                Icon24(glyph, Color.White.copy(alpha = if (enabled) 1f else 0.5f), 15.dp)
                Spacer(Modifier.width(7.dp))
            }
            Text(text, color = Color.White.copy(alpha = if (enabled) 1f else 0.55f),
                fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun GhostButton(
    text: String,
    modifier: Modifier = Modifier,
    accent: Color = ZW.TextDim,
    glyph: Glyph? = null,
    onClick: () -> Unit
) {
    val inter = remember { MutableInteractionSource() }
    val pressed by inter.collectIsPressedAsState()
    val shape = RoundedCornerShape(15.dp)
    Box(
        modifier
            .clip(shape)
            .background(Color.White.copy(alpha = if (pressed) 0.1f else 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), shape)
            .clickable(interactionSource = inter, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (glyph != null) {
                Icon24(glyph, accent, 14.dp)
                Spacer(Modifier.width(6.dp))
            }
            Text(text, color = accent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SectionLabel(text: String, trailing: String? = null, modifier: Modifier = Modifier) {
    Row(
        modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 6.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = ZW.TextFaint, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
        if (trailing != null) Text(trailing, color = ZW.TextFaint, fontSize = 11.5.sp)
    }
}

/** 数值统计块 */
@Composable
fun StatTile(label: String, value: String, unit: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.04f))
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.05f))
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(vertical = 12.dp, horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = accent, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(unit, color = accent.copy(alpha = 0.7f), fontSize = 11.sp,
                modifier = Modifier.padding(start = 2.dp, bottom = 3.dp))
        }
        Text(label, color = ZW.TextFaint, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

/** 进度条 */
@Composable
fun ThinBar(progress: Float, accent: Color, modifier: Modifier = Modifier, height: Dp = 4.dp) {
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
    ) {
        Box(
            Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(height)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(accent, accent.copy(alpha = 0.55f))))
        )
    }
}

/** 圆形开关 */
@Composable
fun SwitchRow(title: String, desc: String?, checked: Boolean, accent: Color, onChange: (Boolean) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onChange(!checked) }
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, color = ZW.Text, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
            if (desc != null) Text(desc, color = ZW.TextFaint, fontSize = 11.5.sp, modifier = Modifier.padding(top = 2.dp))
        }
        Box(
            Modifier
                .size(width = 44.dp, height = 26.dp)
                .clip(CircleShape)
                .background(if (checked) accent.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.12f))
        ) {
            Box(
                Modifier
                    .padding(3.dp)
                    .size(20.dp)
                    .align(if (checked) Alignment.CenterEnd else Alignment.CenterStart)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

/** 空态 */
@Composable
fun EmptyState(glyph: Glyph, title: String, desc: String, accent: Color) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.22f), Color.Transparent))),
            contentAlignment = Alignment.Center
        ) { Icon24(glyph, accent, 28.dp) }
        Text(title, color = ZW.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 14.dp))
        Text(desc, color = ZW.TextFaint, fontSize = 12.sp, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp, start = 40.dp, end = 40.dp), lineHeight = 18.sp)
    }
}
