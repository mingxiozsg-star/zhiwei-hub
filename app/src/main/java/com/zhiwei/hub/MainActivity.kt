package com.zhiwei.hub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.zhiwei.hub.data.Prefs
import com.zhiwei.hub.ui.AboutContent
import com.zhiwei.hub.ui.AppearanceContent
import com.zhiwei.hub.ui.Backdrop
import com.zhiwei.hub.ui.BarTab
import com.zhiwei.hub.ui.DetailContent
import com.zhiwei.hub.ui.DeviceContent
import com.zhiwei.hub.ui.GlassBar
import com.zhiwei.hub.ui.Glyph
import com.zhiwei.hub.ui.HubState
import com.zhiwei.hub.ui.HomeContent
import com.zhiwei.hub.ui.InstallCenterContent
import com.zhiwei.hub.ui.InstallController
import com.zhiwei.hub.ui.Route
import com.zhiwei.hub.ui.SettingsContent
import com.zhiwei.hub.ui.SubOverlay
import com.zhiwei.hub.ui.ZhiweiTheme
import kotlinx.coroutines.launch

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
    val installer = remember { InstallController(ctx) }
    val scope = rememberCoroutineScope()

    val accent = prefs.accent.color

    // 三个平级 tab：总览 / 安装中心 / 设置
    val pagerState = rememberPagerState(pageCount = { 3 })
    var subRoute by remember { mutableStateOf<Route?>(null) }

    LaunchedEffect(Unit) { hub.start() }

    BackHandler(enabled = subRoute != null) { subRoute = null }

    Box(Modifier.fillMaxSize()) {
        Backdrop(prefs)

        // 父页（三个平级 tab）只在无二级页时渲染，二级页打开时整层移除，
        // 让二级页的透明玻璃直接透出壁纸，而不是父页内容（杜绝穿模）
        if (subRoute == null) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                when (page) {
                    0 -> {
                        val scroll = rememberScrollState()
                        HomeContent(
                            state = hub,
                            accent = accent,
                            scroll = scroll,
                            onOpenInstall = { scope.launch { pagerState.animateScrollToPage(1) } },
                            onOpenApp = { subRoute = Route.Detail(it) },
                            onRefresh = { hub.refresh() },
                            onSettings = { scope.launch { pagerState.animateScrollToPage(2) } }
                        )
                    }
                    1 -> InstallCenterContent(
                        state = hub,
                        accent = accent,
                        dl = installer,
                        onBack = null,
                        onOpenApp = { subRoute = Route.Detail(it) }
                    )
                    2 -> SettingsContent(
                        prefs = prefs,
                        accent = accent,
                        onBack = null,
                        onAppearance = { subRoute = Route.Appearance },
                        onAbout = { subRoute = Route.About },
                        onDevice = { subRoute = Route.Device },
                        onRefresh = { hub.refresh() }
                    )
                }
            }

            // 底栏：与 pager 联动，点击切换平级 tab
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
                    selected = pagerState.currentPage,
                    accent = accent,
                    onSelect = { i -> scope.launch { pagerState.animateScrollToPage(i) } }
                )
            }
        }

        // 子页覆盖层
        subRoute?.let { r ->
            SubOverlay(route = r) { route ->
                when (route) {
                    is Route.Detail -> {
                        val scroll = rememberScrollState()
                        DetailContent(
                            key = route.key,
                            state = hub,
                            accent = accent,
                            scroll = scroll,
                            onBack = { subRoute = null },
                            onCheck = { hub.checkOne(route.key) },
                            onInstall = { scope.launch { pagerState.animateScrollToPage(1) } }
                        )
                    }
                    is Route.Appearance -> AppearanceContent(
                        prefs = prefs,
                        accent = accent,
                        onBack = { subRoute = null }
                    )
                    is Route.About -> AboutContent(accent = accent, onBack = { subRoute = null })
                    is Route.Device -> DeviceContent(accent = accent, onBack = { subRoute = null })
                }
            }
        }
    }
}