package com.zhiwei.hub.net

/**
 * GitHub 访问加速：中国大陆直连 github.com / api.github.com 经常被墙或限速，
 * 通过镜像前缀做多级 fallback。空串表示直连（作为最后兜底）。
 */
object NetAccel {

    // 镜像按优先级排序；最后一个空串 = 直连兜底
    private val PREFIXES = listOf(
        "https://ghfast.top/",
        "https://gh-proxy.com/",
        "https://ghproxy.net/",
        ""
    )

    /** 生成候选 URL 列表：镜像在前，直连兜底。 */
    fun candidates(url: String): List<String> =
        PREFIXES.map { prefix -> prefix + url }
}