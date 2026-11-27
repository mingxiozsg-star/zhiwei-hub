package com.zhiwei.hub.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhiwei.hub.core.DeviceInfo
import com.zhiwei.hub.core.DeviceInfoProbe
import com.zhiwei.hub.core.Fmt

@Composable
fun DeviceContent(accent: Color, onBack: () -> Unit) {
    val ctx = LocalContext.current
    val scroll = rememberScrollState()
    var info by remember { mutableStateOf<DeviceInfo?>(null) }

    LaunchedEffect(Unit) {
        info = runCatching { DeviceInfoProbe.collect(ctx) }.getOrNull()
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(start = 16.dp, end = 16.dp, bottom = 40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
        ) {
            CircleIcon(Glyph.BACK, accent, onBack)
            Spacer(Modifier.weight(1f))
            if (refreshing) {
                Text("读取中…", color = ZW.TextDim, fontSize = 12.sp)
            }
        }
        Text("设备信息", color = ZW.Text, fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp)
        Text("内存 · 存储 · 处理器 · 进程", color = ZW.TextDim, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))

        val d = info
        if (d == null) {
            Spacer(Modifier.height(40.dp))
            EmptyState(Glyph.INFO, "读取失败", "无法获取设备信息", accent)
            return@Column
        }

        Spacer(Modifier.height(18.dp))

        // 概览：内存 + 存储 双环形或进度
        SectionLabel("资源")
        GlassCard(accent = accent, corner = 22.dp, contentPadding = 16.dp) {
            Column {
                // 内存
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("内存", color = ZW.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${Fmt.bytes(d.usedMem)} / ${Fmt.bytes(d.totalMem)}",
                            color = ZW.TextDim, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Text(
                        Fmt.pct(d.usedMem.toInt(), d.totalMem.toInt()),
                        color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                ThinBar(
                    if (d.totalMem > 0) d.usedMem.toFloat() / d.totalMem else 0f,
                    accent, height = 5.dp
                )
                Spacer(Modifier.height(16.dp))
                // 存储
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("存储", color = ZW.Text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${Fmt.bytes(d.storageUsed)} / ${Fmt.bytes(d.storageTotal)}",
                            color = ZW.TextDim, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Text(
                        Fmt.pct(d.storageUsed.toInt(), d.storageTotal.toInt()),
                        color = accent, fontSize = 13.sp, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.height(8.dp))
                ThinBar(
                    if (d.storageTotal > 0) d.storageUsed.toFloat() / d.storageTotal else 0f,
                    ZW.Cyan, height = 5.dp
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        SectionLabel("系统")
        GlassCard(corner = 18.dp, contentPadding = 15.dp) {
            InfoLine("型号", d.model)
            InfoLine("品牌", "${d.manufacturer} ${d.brand}")
            InfoLine("系统", "Android ${d.androidVersion} (API ${d.sdkInt})")
            InfoLine("电量", if (d.batteryLevel >= 0) "${d.batteryLevel}%" else "—")
        }

        Spacer(Modifier.height(14.dp))
        SectionLabel("处理器")
        GlassCard(corner = 18.dp, contentPadding = 15.dp) {
            InfoLine("核心数", "${d.cpuCores} 核")
            InfoLine("架构", d.cpuAbi)
            InfoLine("主频", if (d.cpuMaxFreqKhz > 0) "${d.cpuMaxFreqKhz / 1000} MHz" else "—")
        }

        Spacer(Modifier.height(14.dp))
        SectionLabel("显示")
        GlassCard(corner = 18.dp, contentPadding = 15.dp) {
            InfoLine("分辨率", "${d.screenW} × ${d.screenH}")
            InfoLine("密度", "${d.densityDpi} dpi")
        }

        Spacer(Modifier.height(14.dp))
        SectionLabel("进程", "${d.processes.size} 个")
        if (d.processes.isEmpty()) {
            GlassCard(corner = 18.dp, contentPadding = 15.dp) {
                Text("当前无可见进程", color = ZW.TextDim, fontSize = 12.sp)
            }
        } else {
            GlassCard(corner = 18.dp, contentPadding = 6.dp) {
                Column {
                    d.processes.forEachIndexed { i, p ->
                        if (i > 0) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.05f))
                            )
                        }
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(p.name, color = ZW.Text, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                Text("PID ${p.pid}", color = ZW.TextFaint, fontSize = 10.5.sp)
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(p.importanceLabel, color = accent, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text("知微中心 · 设备信息", color = ZW.TextFaint, fontSize = 10.5.sp)
        }
    }
}