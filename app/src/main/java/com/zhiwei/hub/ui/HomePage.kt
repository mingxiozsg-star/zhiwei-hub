package com.zhiwei.hub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhiwei.hub.core.Fmt
import com.zhiwei.hub.data.Catalog

/** 应用图标：已安装则尝试真实图标，否则用字形 + 双色渐变。 */
@Composable
fun AppBadge(view: AppView, size: Int = 52) {
    val c1 = Color(view.app.c1)
    val c2 = Color(view.app.c2)
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val icon = androidx.compose.runtime.remember(view.app.key, view.installed) {
        if (view.installed) com.zhiwei.hub.core.PackageProbe.icon(ctx, view.app.pkg) else null
    }
    Box(
        Modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size * 0.3f).dp))
            .background(Brush.linearGradient(listOf(c1, c2))),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            androidx.compose.foundation.Image(
                bitmap = icon.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(size.dp).clip(RoundedCornerShape((size * 0.3f).dp))
            )
        } else {
            Text(
                view.app.glyph,
                color = Color.White,
                fontSize = (size * 0.42f).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** 单个应用行卡片 */
@Composable
fun AppRow(view: AppView, accent: Color, onTap: () -> Unit) {
    val installed = view.installed
    val dotColor = when {
        !installed -> ZW.TextFaint
        view.phase == RunPhase.FOREGROUND -> ZW.Green
        view.state.running -> ZW.Amber
        else -> ZW.TextFaint
    }
    GlassCard(
        accent = if (installed) Color(view.app.c1).copy(alpha = 0.5f) else Color.Transparent,
        corner = 20.dp,
        contentPadding = 14.dp,
        onClick = onTap
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppBadge(view)
            Spacer(Modifier.width(13.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(view.app.name, color = ZW.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    if (view.hasUpdate) {
                        Spacer(Modifier.width(6.dp))
                        Badge("更新", accent)
                    }
                }
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusDot(dotColor, view.state.running)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        if (installed) "${view.statusLabel} · ${view.state.versionName ?: ""}"
                        else view.app.tagline,
                        color = ZW.TextDim, fontSize = 11.5.sp
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    if (installed) "已安装" else "未安装",
                    color = if (installed) ZW.TextDim else accent,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium
                )
                if (installed && view.state.apkSize > 0) {
                    Text(Fmt.bytes(view.state.apkSize), color = ZW.TextFaint, fontSize = 10.5.sp)
                }
            }
        }
    }
}

@Composable
fun Badge(text: String, color: Color) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.35f), CircleShape)
            .padding(horizontal = 7.dp, vertical = 2.dp)
    ) {
        Text(text, color = color, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
    }
}

/** 健康度环形图 */
@Composable
fun HealthRing(percent: Int, accent: Color, size: Int = 92) {
    androidx.compose.foundation.Canvas(Modifier.size(size.dp)) {
        val stroke = this.size.width * 0.075f
        val inset = stroke / 2
        val arcSize = androidx.compose.ui.geometry.Size(this.size.width - stroke, this.size.height - stroke)
        drawArc(
            color = Color.White.copy(alpha = 0.07f),
            startAngle = 0f, sweepAngle = 360f, useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
            size = arcSize,
            style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
        drawArc(
            brush = Brush.sweepGradient(listOf(accent.copy(alpha = 0.35f), accent, accent.copy(alpha = 0.35f))),
            startAngle = -90f, sweepAngle = 360f * (percent / 100f), useCenter = false,
            topLeft = androidx.compose.ui.geometry.Offset(inset, inset),
            size = arcSize,
            style = androidx.compose.ui.graphics.drawscope.Stroke(stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
        )
    }
}

/** 顶部概览卡：健康度 + 三项统计 */
@Composable
fun OverviewCard(state: HubState, accent: Color, onOpenInstall: () -> Unit) {
    val snap = state.snapshot
    GlassCard(accent = accent, corner = 26.dp, contentPadding = 18.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center) {
                HealthRing(state.health, accent)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${state.health}", color = ZW.Text, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("健康度", color = ZW.TextFaint, fontSize = 9.5.sp)
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (state.installedCount == Catalog.apps.size) "全部就位"
                    else "还有 ${Catalog.apps.size - state.installedCount} 个未安装",
                    color = ZW.Text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    buildString {
                        append("运行中 ${state.runningCount}")
                        if (state.updateCount > 0) append(" · 可更新 ${state.updateCount}")
                        snap?.let { append(" · 可用内存 ${Fmt.bytes(it.availMem)}") }
                    },
                    color = ZW.TextDim, fontSize = 11.5.sp
                )
                Spacer(Modifier.height(12.dp))
                Row {
                    if (state.installedCount < Catalog.apps.size) {
                        PrimaryButton("安装中心", accent, glyph = Glyph.DOWNLOAD, onClick = onOpenInstall)
                    } else {
                        GhostButton("全部已安装", accent = ZW.Green, glyph = Glyph.CHECK, onClick = onOpenInstall)
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(state: HubState, accent: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
        StatTile("已安装", "${state.installedCount}", "/${Catalog.apps.size}", accent, Modifier.weight(1f))
        StatTile("运行中", "${state.runningCount}", "个", ZW.Green, Modifier.weight(1f))
        StatTile("可更新", "${state.updateCount}", "个", ZW.Amber, Modifier.weight(1f))
    }
}
