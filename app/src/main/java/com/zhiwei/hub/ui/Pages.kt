package com.zhiwei.hub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhiwei.hub.BuildConfig
import com.zhiwei.hub.core.Fmt
import com.zhiwei.hub.core.launchIntentOf
import com.zhiwei.hub.core.openAppInfo
import com.zhiwei.hub.data.Catalog

/** 自适应大标题：滚动时字号与高度平滑收起。 */
@Composable
fun BigHeader(
    title: String,
    subtitle: String,
    accent: Color,
    scroll: androidx.compose.foundation.ScrollState,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScopeShim.() -> Unit = {}
) {
    val collapse = (scroll.value / 320f).coerceIn(0f, 1f)
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 10.dp, bottom = 6.dp)
    ) {
        if (onBack != null) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 10.dp)) {
                CircleIcon(Glyph.BACK, accent, onBack)
                Spacer(Modifier.width(10.dp))
                Text(title, color = ZW.Text, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
        } else {
            Text(
                title,
                color = ZW.Text,
                fontSize = (34 - 12 * collapse).sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.8).sp
            )
        }
        Text(
            subtitle,
            color = ZW.TextDim,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = (6 - 4 * collapse).dp)
        )
    }
}

object RowScopeShim

@Composable
fun CircleIcon(g: Glyph, accent: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon24(g, ZW.Text, 17.dp) }
}

/* ---------------- 主页 ---------------- */

@Composable
fun HomeContent(
    state: HubState,
    accent: Color,
    nav: NavController,
    scroll: androidx.compose.foundation.ScrollState,
    onOpenInstall: () -> Unit,
    onOpenApp: (String) -> Unit,
    onRefresh: () -> Unit,
    onSettings: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 118.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("知微中心", color = ZW.Text, fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp)
                Text(
                    if (state.scanning) state.scanningMessage
                    else if (state.lastScan > 0) "上次扫描 ${Fmt.ago(state.lastScan)}"
                    else "尚未扫描",
                    color = ZW.TextDim, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp)
                )
            }
            CircleIcon(Glyph.REFRESH, accent, onRefresh)
            Spacer(Modifier.width(8.dp))
            CircleIcon(Glyph.SETTINGS, accent, onSettings)
        }

        Spacer(Modifier.height(18.dp))
        OverviewCard(state, accent, onOpenInstall)
        Spacer(Modifier.height(12.dp))
        StatRow(state, accent)

        val snap = state.snapshot
        val needUsageAccess = snap != null && !snap.usageAccess
        if (needUsageAccess) {
            Spacer(Modifier.height(12.dp))
            UsageAccessCard(accent)
        }

        Spacer(Modifier.height(20.dp))
        SectionLabel("系列应用", "${Catalog.apps.size} 项")

        Catalog.apps.forEach { app ->
            val v = state.views[app.key] ?: return@forEach
            AppRow(v, accent) { onOpenApp(app.key) }
            Spacer(Modifier.height(9.dp))
        }

        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("知微系列 · v${BuildConfig.VERSION_NAME}", color = ZW.TextFaint, fontSize = 10.5.sp)
        }
    }
}

@Composable
fun UsageAccessCard(accent: Color) {
    val ctx = LocalContext.current
    GlassCard(accent = ZW.Amber, corner = 18.dp, contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon24(Glyph.INFO, ZW.Amber, 18.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("授权使用情况访问", color = ZW.Text, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                Text("授权后可显示前台运行时长与最近使用时间", color = ZW.TextFaint, fontSize = 11.sp)
            }
            Spacer(Modifier.width(8.dp))
            GhostButton("授权", accent = ZW.Amber) {
                com.zhiwei.hub.core.PackageProbe.openUsageAccessSettings(ctx)
            }
        }
    }
}

/* ---------------- 详情页 ---------------- */

@Composable
fun DetailContent(
    key: String,
    state: HubState,
    accent: Color,
    scroll: androidx.compose.foundation.ScrollState,
    onBack: () -> Unit,
    onCheck: () -> Unit,
    onInstall: () -> Unit
) {
    val ctx = LocalContext.current
    val view = state.views[key]
    if (view == null) {
        EmptyState(Glyph.INFO, "找不到该应用", "目录里没有 $key", accent)
        return
    }
    val st = view.state
    val c1 = Color(view.app.c1)

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(start = 16.dp, end = 16.dp, bottom = 118.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp, bottom = 14.dp)) {
            CircleIcon(Glyph.BACK, accent, onBack)
            Spacer(Modifier.weight(1f))
            CircleIcon(Glyph.REFRESH, accent, onCheck)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            AppBadge(view, 66)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(view.app.name, color = ZW.Text, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                Text(
                    view.app.tagline, color = ZW.TextDim, fontSize = 12.sp,
                    modifier = Modifier.padding(top = 3.dp)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 状态卡
        GlassCard(accent = c1, corner = 22.dp, contentPadding = 16.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusDot(
                    when {
                        !view.installed -> ZW.TextFaint
                        view.phase == RunPhase.FOREGROUND -> ZW.Green
                        st.running -> ZW.Amber
                        else -> ZW.TextFaint
                    },
                    st.running
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (view.installed) view.statusLabel else "未安装",
                    color = ZW.Text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.weight(1f))
                if (view.installed) Badge("v${st.versionName ?: "?"}", c1)
            }
            Spacer(Modifier.height(14.dp))
            InfoLine("包名", view.app.pkg)
            if (view.installed) {
                InfoLine("安装大小", Fmt.bytes(st.apkSize))
                InfoLine("安装时间", Fmt.date(st.firstInstall))
                InfoLine("最近更新", Fmt.date(st.lastUpdate))
                if (st.foregroundMs > 0) InfoLine("累计前台", Fmt.duration(st.foregroundMs))
                if (st.lastTimeUsed > 0) InfoLine("最近使用", Fmt.ago(st.lastTimeUsed))
            }
            InfoLine("线上版本", if (view.checking) "检查中…" else view.remote?.version ?: "—")
        }

        if (view.hasUpdate) {
            Spacer(Modifier.height(10.dp))
            GlassCard(accent = ZW.Amber, corner = 18.dp, contentPadding = 14.dp) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon24(Glyph.DOWNLOAD, ZW.Amber, 18.dp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("发现新版本 ${view.remote?.version}", color = ZW.Text, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "当前 ${st.versionName} → ${view.remote?.version} · ${Fmt.bytes(view.remote?.apkSize ?: 0L)}",
                            color = ZW.TextFaint, fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        SectionLabel("操作")
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            if (view.installed) {
                PrimaryButton("打开", c1, glyph = Glyph.PLAY) {
                    ctx.packageManager.getLaunchIntentForPackage(view.app.pkg)?.let {
                        it.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                        runCatching { ctx.startActivity(it) }
                    }
                }
                GhostButton("应用信息", glyph = Glyph.INFO) { ctx.openAppInfo(view.app.pkg) }
            } else {
                PrimaryButton("去安装", accent, glyph = Glyph.DOWNLOAD) { onInstall() }
            }
        }

        Spacer(Modifier.height(18.dp))
        SectionLabel("简介")
        GlassCard(corner = 18.dp, contentPadding = 15.dp) {
            Text(view.app.desc, color = ZW.TextDim, fontSize = 12.5.sp, lineHeight = 20.sp)
        }

        if (view.remote != null && view.remote!!.notes.isNotBlank()) {
            Spacer(Modifier.height(14.dp))
            SectionLabel("更新说明")
            GlassCard(corner = 18.dp, contentPadding = 15.dp) {
                Text(
                    view.remote?.notes?.take(1200).orEmpty(),
                    color = ZW.TextDim, fontSize = 12.sp, lineHeight = 19.sp
                )
            }
        }
    }
}

@Composable
fun InfoLine(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = ZW.TextFaint, fontSize = 11.5.sp, modifier = Modifier.width(70.dp))
        Text(value, color = ZW.TextDim, fontSize = 11.5.sp, modifier = Modifier.weight(1f))
    }
}
