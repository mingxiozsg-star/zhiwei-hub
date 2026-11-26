package com.zhiwei.hub.net

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/** 一个线上发布版本。 */
data class ReleaseInfo(
    val repo: String,
    val tag: String,
    val name: String,
    val notes: String,
    val publishedAt: Long,
    val apkUrl: String?,
    val apkSize: Long,
    val apkName: String?
) {
    val version: String get() = tag.removePrefix("v")
}

/**
 * 直接读 GitHub Release API，不引入额外依赖。
 * 私有/限流时静默失败，UI 侧退化为「仅本地扫描」。
 */
object ReleaseApi {

    private const val BASE = "https://api.github.com/repos/mingxiozsg-star"
    private const val UA = "ZhiweiHub/1.0 (Android)"

    fun latest(repo: String): ReleaseInfo? = runCatching {
        val conn = (URL("$BASE/$repo/releases/latest").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 9000
            readTimeout = 12000
            setRequestProperty("User-Agent", UA)
            setRequestProperty("Accept", "application/vnd.github+json")
        }
        val body = conn.inputStream.bufferedReader().use(BufferedReader::readText).orEmpty()
        val code = conn.responseCode
        conn.disconnect()
        if (code != 200 || body.isBlank()) return null

        val json = JSONObject(body)
        val assets = json.optJSONArray("assets")
        var apkUrl: String? = null
        var apkSize = 0L
        var apkName: String? = null
        if (assets != null) {
            // 优先取 release 包，其次任意 apk，避免抓到 debug 包
            val list = (0 until assets.length()).map { assets.getJSONObject(it) }
            val chosen = list.firstOrNull {
                it.optString("name").contains("release", true) && it.optString("name").endsWith(".apk", true)
            } ?: list.firstOrNull { it.optString("name").endsWith(".apk", true) }
            if (chosen != null) {
                apkUrl = chosen.optString("browser_download_url").ifBlank { null }
                apkSize = chosen.optLong("size", 0L)
                apkName = chosen.optString("name").ifBlank { null }
            }
        }

        ReleaseInfo(
            repo = repo,
            tag = json.optString("tag_name").ifBlank { "-" },
            name = json.optString("name").ifBlank { json.optString("tag_name") },
            notes = json.optString("body").orEmpty(),
            publishedAt = parseIso(json.optString("published_at")),
            apkUrl = apkUrl,
            apkSize = apkSize,
            apkName = apkName
        )
    }.getOrNull()

    private fun parseIso(s: String): Long {
        if (s.isBlank()) return 0L
        return runCatching {
            java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US)
                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                .parse(s)?.time ?: 0L
        }.getOrDefault(0L)
    }
}