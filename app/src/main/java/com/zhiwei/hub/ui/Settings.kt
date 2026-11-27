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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.zhiwei.hub.data.Prefs

@Composable
fun SettingsContent(
    prefs: Prefs,
    accent: Color,
    onBack: (() -> Unit)?,
    onAppearance: () -> Unit,
    onAbout: () -> Unit,
    onRefresh: () -> Unit
) {
    val scroll = rememberScrollState()
    val ctx = LocalContext.current
    var autoScan by remember { mutableStateOf(prefs.autoScan) }
    var autoUpdate by remember { mutableStateOf(prefs.autoUpdate) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(start = 16.dp, end = 16.dp, bottom = 118.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)) {
            if (onBack != null) {
                CircleIcon(Glyph.BACK, accent, onBack)
                Spacer(Modifier.weight(1f))
            }
        }
        Text("设置", color = ZW.Text, fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp)
        Text("外观、扫描、关于", color = ZW.TextDim, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))

        Spacer(Modifier.height(18.dp))

        SectionLabel("外观")
        GlassCard(corner = 20.dp, contentPadding = 4.dp) {
            NavRow("背景与主题", "自定义背景、模糊、强调色", Glyph.PALETTE, accent, onAppearance)
        }

        Spacer(Modifier.height(14.dp))
        SectionLabel("扫描")
        GlassCard(corner = 20.dp, contentPadding = 14.dp) {
            SwitchRow(
                "打开时自动扫描",
                "进入应用即刷新安装与运行状态",
                autoScan, accent
            ) { autoScan = it; prefs.autoScan = it }
            Row(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.06f))) {}
            SwitchRow(
                "自动检查更新",
                "读取 GitHub Release 比对版本",
                autoUpdate, accent
            ) { autoUpdate = it; prefs.autoUpdate = it }
            Row(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.06f))) {}
            Row(
                Modifier.fillMaxWidth().clickable { onRefresh() }.padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("立即重新扫描", color = ZW.Text, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                Icon24(Glyph.REFRESH, accent, 16.dp)
            }
        }

        Spacer(Modifier.height(14.dp))
        SectionLabel("权限")
        GlassCard(corner = 20.dp, contentPadding = 14.dp) {
            Row(
                Modifier.fillMaxWidth()
                    .clickable { com.zhiwei.hub.core.PackageProbe.openUsageAccessSettings(ctx) }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("使用情况访问", color = ZW.Text, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                Text("去授权", color = accent, fontSize = 12.sp)
            }
            Row(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.06f))) {}
            Row(
                Modifier.fillMaxWidth()
                    .clickable { com.zhiwei.hub.net.ApkDownloader.requestInstallPermission(ctx) }
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("安装未知应用", color = ZW.Text, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                Text("去授权", color = accent, fontSize = 12.sp)
            }
        }

        Spacer(Modifier.height(14.dp))
        SectionLabel("其他")
        GlassCard(corner = 20.dp, contentPadding = 4.dp) {
            NavRow("关于知微中心", "版本、致谢与说明", Glyph.INFO, accent, onAbout)
        }

        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("始源创想科技IT", color = ZW.TextFaint, fontSize = 10.5.sp)
        }
    }
}

@Composable
fun NavRow(title: String, desc: String, glyph: Glyph, accent: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(34.dp).clip(RoundedCornerShape(11.dp))
                .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.35f), accent.copy(alpha = 0.12f)))),
            contentAlignment = Alignment.Center
        ) { Icon24(glyph, Color.White, 17.dp) }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, color = ZW.Text, fontSize = 14.5.sp, fontWeight = FontWeight.Medium)
            Text(desc, color = ZW.TextFaint, fontSize = 11.sp)
        }
        Box(Modifier.size(16.dp)) { Icon24(Glyph.BACK, ZW.TextFaint, 14.dp) }
    }
}

@Composable
fun AppearanceContent(prefs: Prefs, accent: Color, onBack: () -> Unit) {
    val scroll = rememberScrollState()
    val ctx = LocalContext.current
    var mode by remember { mutableStateOf(prefs.bgMode) }
    var gradIdx by remember { mutableStateOf(prefs.bgGradient) }
    var dim by remember { mutableStateOf(prefs.bgDim) }
    var blur by remember { mutableStateOf(prefs.bgBlur) }
    var uri by remember { mutableStateOf(prefs.bgUri) }
    val picker = rememberLauncherForImage { u ->
        uri = u
        prefs.bgUri = u
        mode = "image"
        prefs.bgMode = "image"
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(start = 16.dp, end = 16.dp, bottom = 118.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)) {
            CircleIcon(Glyph.BACK, accent, onBack)
        }
        Text("外观", color = ZW.Text, fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp)
        Text("背景、模糊、强调色", color = ZW.TextDim, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))

        Spacer(Modifier.height(18.dp))
        SectionLabel("背景来源")
        GlassCard(corner = 20.dp, contentPadding = 12.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SegChip("图片", mode == "image", accent) {
                    if (uri == null) picker() else { mode = "image"; prefs.bgMode = "image" }
                }
                SegChip("渐变", mode == "gradient", accent) { mode = "gradient"; prefs.bgMode = "gradient" }
                SegChip("纯色", mode == "solid", accent) { mode = "solid"; prefs.bgMode = "solid" }
            }
            Spacer(Modifier.height(12.dp))
            GhostButton(if (uri == null) "选择图片" else "更换图片", accent = accent, glyph = Glyph.IMAGE) { picker() }
        }

        Spacer(Modifier.height(14.dp))
        SectionLabel("渐变预设")
        GlassCard(corner = 20.dp, contentPadding = 12.dp) {
            Prefs.GRADIENTS.forEachIndexed { i, (name, colors) ->
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { gradIdx = i; prefs.bgGradient = i }
                        .padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(width = 46.dp, height = 26.dp).clip(RoundedCornerShape(8.dp))
                            .background(Brush.horizontalGradient(colors.map { Color(it) }))
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(name, color = ZW.Text, fontSize = 13.5.sp)
                    Spacer(Modifier.weight(1f))
                    if (i == prefs.bgGradient) Icon24(Glyph.CHECK, accent, 16.dp)
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        SectionLabel("微调")
        GlassCard(corner = 20.dp, contentPadding = 14.dp) {
            Text("暗化 ${(dim * 100).toInt()}%", color = ZW.Text, fontSize = 13.sp)
            Slider(
                value = dim, onValueChange = { dim = it; prefs.bgDim = it },
                valueRange = 0f..0.9f,
                colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent.copy(alpha = 0.7f))
            )
            Text("模糊 ${blur.toInt()}", color = ZW.Text, fontSize = 13.sp)
            Slider(
                value = blur, onValueChange = { blur = it; prefs.bgBlur = it },
                valueRange = 0f..25f,
                colors = SliderDefaults.colors(thumbColor = accent, activeTrackColor = accent.copy(alpha = 0.7f))
            )
        }

        Spacer(Modifier.height(14.dp))
        SectionLabel("强调色")
        GlassCard(corner = 20.dp, contentPadding = 14.dp) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Accent.entries.forEach { a ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { prefs.accent = a }
                    ) {
                        Box(
                            Modifier.size(40.dp).clip(CircleShape)
                                .background(Brush.linearGradient(listOf(a.color, a.second)))
                                .border(
                                    if (prefs.accent == a) 2.5.dp else 0.dp,
                                    if (prefs.accent == a) Color.White else Color.Transparent,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) { if (prefs.accent == a) Icon24(Glyph.CHECK, Color.White, 16.dp) }
                        Text(a.label, color = ZW.TextFaint, fontSize = 10.5.sp, modifier = Modifier.padding(top = 5.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SegChip(text: String, active: Boolean, accent: Color, onClick: () -> Unit) {
    Box(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) accent.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.05f))
            .border(1.dp, if (active) accent.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = if (active) accent else ZW.TextDim, fontSize = 12.5.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun AboutContent(accent: Color, onBack: () -> Unit) {
    val scroll = rememberScrollState()
    Column(
        Modifier.fillMaxSize().verticalScroll(scroll).padding(start = 16.dp, end = 16.dp, bottom = 118.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)) {
            CircleIcon(Glyph.BACK, accent, onBack)
        }
        Text("关于", color = ZW.Text, fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp)
        Text("知微中心 v${BuildConfig.VERSION_NAME}", color = ZW.TextDim, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))

        Spacer(Modifier.height(18.dp))
        GlassCard(accent = accent, corner = 22.dp, contentPadding = 16.dp) {
            Text("知微中心", color = ZW.Text, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text(
                "把知微系列应用集中在一处：检测是否安装、是否在跑、版本是否最新。缺的直接从安装中心下载安装。",
                color = ZW.TextDim, fontSize = 12.5.sp, lineHeight = 20.sp
            )
        }

        Spacer(Modifier.height(14.dp))
        SectionLabel("说明")
        GlassCard(corner = 18.dp, contentPadding = 15.dp) {
            Text(
                "· 安装状态与版本来自系统 PackageManager\n" +
                    "· 运行状态来自 MainActivityManager 进程列表\n" +
                    "· 前台时长需「使用情况访问」权限，否则显示为未知\n" +
                    "· 更新检测读取 GitHub Release，不走第三方服务器",
                color = ZW.TextDim, fontSize = 12.sp, lineHeight = 21.sp
            )
        }

        Spacer(Modifier.height(14.dp))
        SectionLabel("联系")
        GlassCard(corner = 18.dp, contentPadding = 15.dp) {
            Text("官网 建设中", color = ZW.TextDim, fontSize = 12.sp)
            Text("邮箱 ailixiya@qycxkjit.wecom.work", color = ZW.TextDim, fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp))
            Text("QQ 群 1137595303", color = ZW.TextDim, fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp))
        }

        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("© 始源创想科技IT", color = ZW.TextFaint, fontSize = 10.5.sp)
        }
    }
}
