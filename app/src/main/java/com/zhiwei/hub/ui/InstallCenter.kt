package com.zhiwei.hub.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhiwei.hub.core.Fmt
import com.zhiwei.hub.data.Catalog
import com.zhiwei.hub.net.ApkDownloader
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

sealed interface Dl {
    data object Idle : Dl
    data class Running(val done: Long, val total: Long) : Dl
    data class Done(val file: File) : Dl
    data class Failed(val reason: String) : Dl
}

/** 下载 + 安装编排；状态用快照 Map，UI 直接读。 */
class InstallController(private val ctx: Context) {
    val states = mutableStateMapOf<String, Dl>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun download(
        app: com.zhiwei.hub.data.SeriesApp,
        release: com.zhiwei.hub.net.ReleaseInfo,
        onDone: (File) -> Unit
    ) {
        if (states[app.key] is Dl.Running) return
        states[app.key] = Dl.Running(0, release.apkSize)
        scope.launch {
            val f = withContext(Dispatchers.IO) {
                ApkDownloader.download(ctx, app, release) { d, t ->
                    states[app.key] = Dl.Running(d, t)
                }
            }
            if (f != null) {
                states[app.key] = Dl.Done(f)
                onDone(f)
            } else {
                states[app.key] = Dl.Failed("下载失败，请检查网络")
            }
        }
    }

    fun install(file: File) {
        ApkDownloader.install(ctx, file)
    }
}

@Composable
fun InstallCenterContent(
    state: HubState,
    accent: Color,
    dl: InstallController,
    onBack: () -> Unit,
    onOpenApp: (String) -> Unit
) {
    val ctx = LocalContext.current
    val scroll = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(start = 16.dp, end = 16.dp, bottom = 118.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)) {
            CircleIcon(Glyph.BACK, accent, onBack)
            Spacer(Modifier.weight(1f))
            GhostButton("全部检查", accent = accent, glyph = Glyph.REFRESH) { state.checkUpdates() }
        }

        Text("安装中心", color = ZW.Text, fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.8).sp)
        Text(
            "未安装的直接下，已装的看更新",
            color = ZW.TextDim, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp)
        )

        Spacer(Modifier.height(16.dp))

        val notInstalled = Catalog.apps.mapNotNull { state.views[it.key] }.filter { !it.installed }
        val updatable = Catalog.apps.mapNotNull { state.views[it.key] }.filter { it.installed && it.hasUpdate }

        if (notInstalled.isEmpty() && updatable.isEmpty()) {
            EmptyState(Glyph.CHECK, "全部就绪", "系列应用均已安装且为最新版本", ZW.Green)
        }

        if (notInstalled.isNotEmpty()) {
            SectionLabel("待安装", "${notInstalled.size} 个")
            notInstalled.forEach { v ->
                InstallRow(v, accent, dl, state, onOpenApp)
                Spacer(Modifier.height(9.dp))
            }
        }

        if (updatable.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            SectionLabel("可更新", "${updatable.size} 个")
            updatable.forEach { v ->
                InstallRow(v, accent, dl, state, onOpenApp)
                Spacer(Modifier.height(9.dp))
            }
        }

        val installed = Catalog.apps.mapNotNull { state.views[it.key] }.filter { it.installed && !it.hasUpdate }
        if (installed.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            SectionLabel("已是新版", "${installed.size} 个")
            installed.forEach { v ->
                GlassCard(corner = 18.dp, contentPadding = 13.dp, onClick = { onOpenApp(v.app.key) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppBadge(v, 42)
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            Text(v.app.name, color = ZW.Text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("v${v.state.versionName ?: "?"}", color = ZW.TextFaint, fontSize = 11.sp)
                        }
                        Icon24(Glyph.CHECK, ZW.Green, 17.dp)
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))
        GlassCard(corner = 16.dp, contentPadding = 13.dp) {
            Text(
                "安装包来自 GitHub Release，安装时由系统确认；本应用不静默安装。",
                color = ZW.TextFaint, fontSize = 11.sp, lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun InstallRow(
    v: AppView,
    accent: Color,
    dl: InstallController,
    state: HubState,
    onOpenApp: (String) -> Unit
) {
    val ctx = LocalContext.current
    val dls = dl.states[v.app.key] ?: Dl.Idle
    val c1 = Color(v.app.c1)

    GlassCard(accent = c1.copy(alpha = 0.5f), corner = 20.dp, contentPadding = 14.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppBadge(v, 48)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(v.app.name, color = ZW.Text, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    when {
                        v.hasUpdate -> "${v.state.versionName} → ${v.remote?.version}"
                        v.checking -> "检查中…"
                        v.remote != null -> "线上 v${v.remote?.version} · ${Fmt.bytes(v.remote?.apkSize ?: 0L)}"
                        else -> v.app.tagline
                    },
                    color = ZW.TextDim, fontSize = 11.sp
                )
            }
            Spacer(Modifier.width(8.dp))
            when (dls) {
                is Dl.Running -> {
                    Text(
                        if (dls.total > 0) Fmt.pct(dls.done.toInt(), dls.total.toInt())
                        else Fmt.bytes(dls.done),
                        color = c1, fontSize = 12.sp, fontWeight = FontWeight.SemiBold
                    )
                }
                is Dl.Done -> PrimaryButton("安装", ZW.Green, glyph = Glyph.DOWNLOAD) { dl.install(dls.file) }
                is Dl.Failed -> GhostButton("重试", accent = ZW.Red, glyph = Glyph.REFRESH) {
                    val rel = v.remote ?: return@GhostButton
                    dl.download(v.app, rel) { f -> dl.install(f) }
                }
                else -> PrimaryButton(
                    if (v.hasUpdate) "更新" else "下载",
                    c1,
                    glyph = Glyph.DOWNLOAD
                ) {
                    val rel = v.remote
                    if (rel == null) {
                        state.checkOne(v.app.key)
                    } else {
                        if (!ApkDownloader.canInstall(ctx)) {
                            ApkDownloader.requestInstallPermission(ctx)
                        } else {
                            dl.download(v.app, rel) { f -> dl.install(f) }
                        }
                    }
                }
            }
        }
        if (dls is Dl.Running) {
            Spacer(Modifier.height(10.dp))
            ThinBar(
                if (dls.total > 0) dls.done.toFloat() / dls.total else 0.02f,
                c1
            )
        }
        if (dls is Dl.Failed) {
            Spacer(Modifier.height(8.dp))
            Text(dls.reason, color = ZW.Red.copy(alpha = 0.8f), fontSize = 11.sp)
        }
    }
}
