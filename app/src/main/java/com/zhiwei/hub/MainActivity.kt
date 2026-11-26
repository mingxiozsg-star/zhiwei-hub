package com.zhiwei.hub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zhiwei.hub.data.Prefs
import com.zhiwei.hub.ui.AppearanceContent
import com.zhiwei.hub.ui.AboutContent
import com.zhiwei.hub.ui.Backdrop
import com.zhiwei.hub.ui.BarTab
import com.zhiwei.hub.ui.DetailContent
import com.zhiwei.hub.ui.GlassBar
import com.zhiwei.hub.ui.Glyph
import com.zhiwei.hub.ui.HubState
import com.zhiwei.hub.ui.HomeContent
import com.zhiwei.hub.ui.InstallCenterContent
import com.zhiwei.hub.ui.InstallController
import com.zhiwei.hub.ui.LayeredNavHost
import com.zhiwei.hub.ui.NavController
import com.zhiwei.hub.ui.Route
import com.zhiwei.hub.ui.SettingsContent
import com.zhiwei.hub.ui.ZhiweiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { ZhiweiTheme { AppRoot() } }
    }
}

@Composable
fun AppRoot() {
    val ctx = LocalContext.current
    val prefs = remember { Prefs(ctx) }
    val hub = remember { HubState(ctx, prefs) }
    val nav = remember { NavController() }
    val installer = remember { InstallController(ctx) }

    var accentKey by remember { mutableIntStateOf(0) }
    val accent = prefs.accent.color

    LaunchedEffect(Unit) { hub.start() }

    // 进入前台时顺手刷新运行状态
    DisposableEffect(Unit) {
        onDispose { }
    }

    BackHandler(enabled = nav.stack.size > 1) {
        nav.back()
    }

    Box(Modifier.fillMaxSize()) {
        Backdrop(prefs)

        LayeredNavHost(nav = nav, modifier = Modifier.fillMaxSize()) { route ->
            when (route) {
                is Route.Home -> {
                    val scroll = rememberScrollState()
                    HomeContent(
                        state = hub,
                        accent = accent,
                        nav = nav,
                        scroll = scroll,
                        onOpenInstall = { nav.push(Route.InstallCenter) },
                        onOpenApp = { nav.push(Route.Detail(it)) },
                        onRefresh = { hub.refresh() },
                        onSettings = { nav.push(Route.Settings) }
                    )
                }
                is Route.InstallCenter -> InstallCenterContent(
                    state = hub,
                    accent = accent,
                    dl = installer,
                    onBack = { nav.back() },
                    onOpenApp = { nav.push(Route.Detail(it)) }
                )
                is Route.Settings -> SettingsContent(
                    prefs = prefs,
                    accent = accent,
                    onBack = { nav.back() },
                    onAppearance = { nav.push(Route.Appearance) },
                    onAbout = { nav.push(Route.About) },
                    onRefresh = { hub.refresh() }
                )
                is Route.Appearance -> AppearanceContent(
                    prefs = prefs,
                    accent = accent,
                    onBack = { nav.back() }
                )
                is Route.About -> AboutContent(accent = accent, onBack = { nav.back() })
                is Route.Detail -> {
                    val scroll = rememberScrollState()
                    DetailContent(
                        key = route.key,
                        state = hub,
                        accent = accent,
                        scroll = scroll,
                        onBack = { nav.back() },
                        onCheck = { hub.checkOne(route.key) },
                        onInstall = { nav.push(Route.InstallCenter) }
                    )
                }
            }
        }

        // 底栏只挂在根页，子页不抢视觉
        if (nav.stack.size == 1) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                GlassBar(
                    tabs = listOf(
                        BarTab("总览", Glyph.CHECK),
                        BarTab("安装中心", Glyph.DOWNLOAD),
                        BarTab("设置", Glyph.SETTINGS)
                    ),
                    selected = 0,
                    accent = accent,
                    onSelect = { i ->
                        when (i) {
                            1 -> nav.push(Route.InstallCenter)
                            2 -> nav.push(Route.Settings)
                            else -> { }
                        }
                    }
                )
            }
        }
    }
}
