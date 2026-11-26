package com.zhiwei.hub.data

/**
 * 知微系列应用目录。
 * 包名、仓库、下载源均与 GitHub 实际发布的 Release 对齐。
 */
data class SeriesApp(
    val key: String,
    val name: String,
    val pkg: String,
    val repo: String,
    val tagline: String,
    val desc: String,
    val glyph: String,
    val c1: Long,
    val c2: Long,
    /** Release 资源文件名过滤：null 表示取第一个 apk */
    val assetHint: String? = null
)

object Catalog {

    val apps: List<SeriesApp> = listOf(
        SeriesApp(
            key = "agent",
            name = "知微智能体",
            pkg = "com.zhiwei.agent",
            repo = "zhiwei-agent",
            tagline = "本地优先的手机端 AI 智能体",
            desc = "多轮对话、工具调用、终端执行与工作区读写，全部在手机上完成。",
            glyph = "智",
            c1 = 0xFF7C6BFF,
            c2 = 0xFFA97BFF
        ),
        SeriesApp(
            key = "browser",
            name = "知微浏览器",
            pkg = "com.java.myapplication",
            repo = "zhiwei-browser",
            tagline = "轻量纯粹的 Android 浏览器",
            desc = "WebView + Compose，内置 AI 翻译、总结与对话，正式包仅约 1.7 MB。",
            glyph = "览",
            c1 = 0xFF2FA8FF,
            c2 = 0xFF56D9C8
        ),
        SeriesApp(
            key = "manager",
            name = "知微管理器",
            pkg = "com.zhiwei.filemanager",
            repo = "zhiwei-manager",
            tagline = "双窗文件管理器 + 逆向工具链",
            desc = "双窗同步、Dex / Smali / Jar / APK 逆向，一套工具完成文件与拆包。",
            glyph = "管",
            c1 = 0xFFFF9A4B,
            c2 = 0xFFFFC24B
        ),
        SeriesApp(
            key = "qbot",
            name = "知微QQ机器人",
            pkg = "com.zhiwei.qbot",
            repo = "zhiwei-qbot",
            tagline = "QQ 官方机器人本地客户端",
            desc = "人设、识图、画图、语音、分句、按钮菜单，一条链路全在本地跑。",
            glyph = "Q",
            c1 = 0xFF12B8FF,
            c2 = 0xFF6FA8FF
        ),
        SeriesApp(
            key = "security",
            name = "知微安全",
            pkg = "com.zhiwei.security",
            repo = "zhiwei-security",
            tagline = "本地查杀 + 内置特权",
            desc = "本地病毒库、云端复核、Shizuku 提权内置，另有设备管理员反制与白名单。",
            glyph = "安",
            c1 = 0xFF34D399,
            c2 = 0xFF56D9C8
        ),
        SeriesApp(
            key = "limiter",
            name = "知微限制",
            pkg = "com.zhiwei.limiter",
            repo = "zhiwei-limiter",
            tagline = "后台与自启动管控",
            desc = "基于 Shizuku + appops，对未获授权自启动的应用执行后台强行停止。",
            glyph = "限",
            c1 = 0xFFFF6B6B,
            c2 = 0xFFFF9A4B
        ),
        SeriesApp(
            key = "store",
            name = "知微应用商店",
            pkg = "com.zhiwei.store",
            repo = "zhiwei-store",
            tagline = "多源应用下载中枢",
            desc = "官方商店、第三方商店与开源平台的下载源汇聚到一处。",
            glyph = "店",
            c1 = 0xFFFF6FA5,
            c2 = 0xFFA97BFF
        )
    )

    fun byKey(key: String): SeriesApp? = apps.firstOrNull { it.key == key }

    fun byPkg(pkg: String): SeriesApp? = apps.firstOrNull { it.pkg == pkg }
}
