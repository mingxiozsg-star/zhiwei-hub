package com.zhiwei.hub.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.zhiwei.hub.core.AppState
import com.zhiwei.hub.core.DeviceSnapshot
import com.zhiwei.hub.core.Fmt
import com.zhiwei.hub.core.PackageProbe
import com.zhiwei.hub.data.Catalog
import com.zhiwei.hub.data.Prefs
import com.zhiwei.hub.data.SeriesApp
import com.zhiwei.hub.net.ReleaseApi
import com.zhiwei.hub.net.ReleaseInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class RunPhase { UNKNOWN, STOPPED, FOREGROUND, BACKGROUND }

/** 一个应用在 UI 上的完整状态 */
data class AppView(
    val app: SeriesApp,
    val state: AppState = AppState(key = app.key),
    val remote: ReleaseInfo? = null,
    val checking: Boolean = false,
    val phase: RunPhase = RunPhase.UNKNOWN
) {
    val installed get() = state.installed
    val hasUpdate: Boolean
        get() {
            val r = remote ?: return false
            val local = state.versionName ?: return false
            return Fmt.versionGreater(r.version, local)
        }

    val statusLabel: String
        get() = when {
            !installed -> "未安装"
            phase == RunPhase.FOREGROUND -> "前台运行"
            state.running -> "后台运行"
            else -> "未运行"
        }
}

class HubState(val ctx: Context, val prefs: Prefs) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var scanJob: Job? = null

    var scanning by mutableStateOf(false)
        private set
    var snapshot by mutableStateOf<DeviceSnapshot?>(null)
        private set
    var scanningMessage by mutableStateOf("")
        private set

    val views = mutableStateMapOf<String, AppView>()
    val releases = mutableStateMapOf<String, ReleaseInfo?>()

    var lastScan by mutableStateOf(0L)
        private set

    init {
        Catalog.apps.forEach { views[it.key] = AppView(app = it) }
    }

    fun start() {
        if (prefs.autoScan) refresh()
        if (prefs.autoUpdate) checkUpdates()
    }

    fun refresh() {
        if (scanning) return
        scanning = true
        scanningMessage = "正在读取已安装应用…"
        scanJob = scope.launch {
            val snap = withContext(Dispatchers.IO) { PackageProbe.scan(ctx, Catalog.apps) }
            scanningMessage = "正在统计运行情况…"
            val fg = withContext(Dispatchers.IO) { PackageProbe.currentForeground(ctx) }
            snapshot = snap
            lastScan = snap.scannedAt
            Catalog.apps.forEach { app ->
                val st = snap.states[app.key] ?: AppState(key = app.key)
                val prev = views[app.key] ?: AppView(app = app)
                val phase = when {
                    !st.installed -> RunPhase.UNKNOWN
                    fg == app.pkg -> RunPhase.FOREGROUND
                    st.running -> RunPhase.BACKGROUND
                    else -> RunPhase.STOPPED
                }
                views[app.key] = prev.copy(state = st, phase = phase)
            }
            scanning = false
            scanningMessage = ""
        }
    }

    fun checkUpdates() {
        Catalog.apps.forEach { app ->
            val prev = views[app.key] ?: return@forEach
            if (prev.checking) return@forEach
            views[app.key] = prev.copy(checking = true)
            scope.launch {
                val rel = withContext(Dispatchers.IO) { ReleaseApi.latest(app.repo) }
                if (rel != null) releases[app.key] = rel
                val cur = views[app.key] ?: return@launch
                views[app.key] = cur.copy(remote = rel ?: cur.remote, checking = false)
            }
        }
    }

    fun checkOne(key: String) {
        val app = Catalog.byKey(key) ?: return
        val cur = views[key] ?: return
        if (cur.checking) return
        views[key] = cur.copy(checking = true)
        scope.launch {
            val rel = withContext(Dispatchers.IO) { ReleaseApi.latest(app.repo) }
            if (rel != null) releases[key] = rel
            val now = views[key] ?: return@launch
            views[key] = now.copy(remote = rel ?: now.remote, checking = false)
        }
    }

    val installedCount: Int get() = views.values.count { it.installed }
    val runningCount: Int get() = views.values.count { it.state.running }
    val updateCount: Int get() = views.values.count { it.hasUpdate }
    val missing: List<AppView> get() = Catalog.apps.mapNotNull { views[it.key] }.filter { !it.installed }

    /** 系列健康度：安装 + 运行 + 最新，三项加权 */
    val health: Int
        get() {
            val total = Catalog.apps.size
            if (total == 0) return 0
            val inst = installedCount
            val run = runningCount
            val up = views.values.count { it.installed && !it.hasUpdate }
            return ((inst + run + up) * 100.0 / (total * 3)).toInt()
        }
}
