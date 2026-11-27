package com.zhiwei.hub.net

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.zhiwei.hub.data.SeriesApp
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** 下载进度回调：done/total 可能为 0（长度未知）。 */
fun interface ProgressSink {
    fun onProgress(done: Long, total: Long)
}

/**
 * APK 下载器：写到外部私有缓存目录，完成后交给系统安装器。
 * 不静默安装 —— 必须用户确认，这是 Android 的规矩，也避免被封禁。
 */
object ApkDownloader {

    fun cacheDir(ctx: Context): File =
        (ctx.getExternalFilesDir("apk") ?: File(ctx.filesDir, "apk")).apply { mkdirs() }

    fun targetFile(ctx: Context, app: SeriesApp, release: ReleaseInfo): File {
        val name = release.apkName?.takeIf { it.endsWith(".apk", true) }
            ?: "app-release.apk"
        return File(cacheDir(ctx), "${app.key}-${release.version}-$name")
    }

    /**
     * 同步下载；返回本地文件，失败返回 null。
     * 若同名文件已存在且大小与远端一致，直接复用（断点重下的简化版）。
     */
    fun download(
        ctx: Context,
        app: SeriesApp,
        release: ReleaseInfo,
        sink: ProgressSink? = null
    ): File? {
        val baseUrl = release.apkUrl ?: return null
        val out = targetFile(ctx, app, release)
        // 已完成且大小吻合，直接复用
        if (out.exists() && release.apkSize > 0 && out.length() == release.apkSize) return out

        // 逐个候选 URL 尝试，直到成功下载完
        for (cand in NetAccel.candidates(baseUrl)) {
            val f = downloadOnce(ctx, app, release, cand, sink)
            if (f != null) return f
        }
        return null
    }

    private fun downloadOnce(
        ctx: Context,
        app: SeriesApp,
        release: ReleaseInfo,
        url: String,
        sink: ProgressSink? = null
    ): File? {
        val out = targetFile(ctx, app, release)
        val part = File(out.absolutePath + ".part")

        var conn: HttpURLConnection? = null
        return try {
            conn = (URL(url).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = true
                connectTimeout = 12000
                readTimeout = 20000
                setRequestProperty("User-Agent", "ZhiweiHub/1.0 (Android)")
                setRequestProperty("Accept", "application/octet-stream")
            }
            val code = conn.responseCode
            if (code !in 200..299) return null
            val total = conn.contentLengthLong.takeIf { it > 0 } ?: release.apkSize

            conn.inputStream.use { input ->
                part.outputStream().use { output ->
                    val buf = ByteArray(64 * 1024)
                    var done = 0L
                    var lastEmit = 0L
                    while (true) {
                        val n = input.read(buf)
                        if (n <= 0) break
                        output.write(buf, 0, n)
                        done += n
                        val now = System.currentTimeMillis()
                        if (sink != null && (now - lastEmit > 120 || done == total)) {
                            lastEmit = now
                            sink.onProgress(done, total)
                        }
                    }
                    output.flush()
                }
            }
            // 校验大小：若声明了大小，不完整则丢弃，让下一个候选源重试
            if (release.apkSize > 0 && part.length() != release.apkSize) {
                runCatching { part.delete() }
                return null
            }
            if (out.exists()) out.delete()
            if (part.renameTo(out)) out else null
        } catch (t: Throwable) {
            runCatching { part.delete() }
            null
        } finally {
            runCatching { conn?.disconnect() }
        }
    }

    /** 是否允许安装未知来源（API 26+ 需逐应用授权）。 */
    fun canInstall(ctx: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 26) {
            ctx.packageManager.canRequestPackageInstalls()
        } else true
    }

    fun requestInstallPermission(ctx: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            runCatching {
                ctx.startActivity(
                    Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .setData(android.net.Uri.parse("package:${ctx.packageName}"))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            }
        }
    }

    /** 调起系统安装器。 */
    fun install(ctx: Context, apk: File): Boolean = runCatching {
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.fileprovider", apk)
        val i = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        ctx.startActivity(i)
        true
    }.getOrDefault(false)
}