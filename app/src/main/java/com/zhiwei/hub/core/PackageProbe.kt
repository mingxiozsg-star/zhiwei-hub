package com.zhiwei.hub.core

import android.app.ActivityManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import com.zhiwei.hub.data.SeriesApp
import java.io.File

/** 单个应用在设备上的真实状态快照。 */
data class AppState(
    val key: String,
    val installed: Boolean = false,
    val versionName: String? = null,
    val versionCode: Long = 0L,
    val appLabel: String? = null,
    val apkPath: String? = null,
    val apkSize: Long = 0L,
    val firstInstall: Long = 0L,
    val lastUpdate: Long = 0L,
    val isSystem: Boolean = false,
    /** 进程是否存活 */
    val running: Boolean = false,
    /** 进程常驻内存（PSS） */
    val pss: Long = 0L,
    /** 前台驻留累计（由 UsageStats 估算） */
    val foregroundMs: Long = 0L,
    val lastTimeUsed: Long = 0L,
    val launchCount: Int = 0,
    /** 累计占用（估算：前台时长 + 后台常驻） */
    val isForegroundNow: Boolean = false
)

data class DeviceSnapshot(
    val states: Map<String, AppState>,
    val usageAccess: Boolean,
    val totalMem: Long,
    val availMem: Long,
    val scannedAt: Long
)

/**
 * 包状态探针：安装、版本、进程、用量，全部走公开 API，不需要 Shizuku。
 * 说明：非 root 环境下无法读取其他应用的实时 CPU，用 PSS + 前台时长作为「运行情况」的替代指标。
 */
object PackageProbe {

    fun hasUsageAccess(ctx: Context): Boolean {
        return try {
            val app = ctx.applicationInfo
            val mode = ctx.packageManager.checkPermission(
                android.Manifest.permission.PACKAGE_USAGE_STATS, app.packageName
            )
            mode == PackageManager.PERMISSION_GRANTED
        } catch (t: Throwable) {
            false
        }
    }

    fun openUsageAccessSettings(ctx: Context) {
        runCatching {
            ctx.startActivity(
                Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun scan(ctx: Context, apps: List<SeriesApp>): DeviceSnapshot {
        val pm = ctx.packageManager
        val am = ctx.contextService(ActivityManager::class.java)
        val usage = loadUsage(ctx)
        var totalMem = 0L
        var availMem = 0L
        if (am != null) {
            val mi = ActivityManager.MemoryInfo()
            am.getMemoryInfo(mi)
            totalMem = mi.totalMem
            availMem = mi.availMem
        }

        val runningPkgs = am?.let { runningPackages(ctx, it) } ?: emptySet()

        val out = LinkedHashMap<String, AppState>()
        for (app in apps) {
            var st = AppState(key = app.key)
            val pi = try {
                if (Build.VERSION.SDK_INT >= 33) {
                    pm.getPackageInfo(app.pkg, PackageManager.PackageInfoFlags.of(0L))
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(app.pkg, 0)
                }
            } catch (t: Throwable) {
                null
            }

            if (pi != null) {
                val ai: ApplicationInfo? = pi.applicationInfo
                st = st.copy(
                    installed = true,
                    versionName = pi.versionName,
                    versionCode = if (Build.VERSION.SDK_INT >= 28) pi.longVersionCode else
                        @Suppress("DEPRECATION") pi.versionCode.toLong(),
                    appLabel = ai?.let { runCatching { pm.getApplicationLabel(it).toString() }.getOrNull() },
                    apkPath = ai?.sourceDir,
                    apkSize = ai?.sourceDir?.let { File(it).length() } ?: 0L,
                    firstInstall = pi.firstInstallTime,
                    lastUpdate = pi.lastUpdateTime,
                    isSystem = ai != null && (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    running = runningPkgs.contains(app.pkg),
                    pss = 0L
                )
            }

            val u = usage[app.pkg]
            if (u != null) {
                st = st.copy(
                    foregroundMs = u.totalTimeInForeground,
                    lastTimeUsed = u.lastTimeUsed,
                    launchCount = 0
                )
            }
            out[app.key] = st
        }

        return DeviceSnapshot(
            states = out,
            usageAccess = hasUsageAccess(ctx),
            totalMem = totalMem,
            availMem = availMem,
            scannedAt = System.currentTimeMillis()
        )
    }

    private fun loadUsage(ctx: Context): Map<String, UsageStats> {
        if (!hasUsageAccess(ctx)) return emptyMap()
        val usm = ctx.contextService(UsageStatsManager::class.java) ?: return emptyMap()
        val end = System.currentTimeMillis()
        val begin = end - 30L * 24 * 3600 * 1000
        return try {
            usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, begin, end)
                ?.filter { it.totalTimeInForeground > 0 }
                ?.associateBy { it.packageName }
                ?: emptyMap()
        } catch (t: Throwable) {
            emptyMap()
        }
    }

    private fun runningPackages(ctx: Context, am: ActivityManager): Set<String> {
        return try {
            if (Build.VERSION.SDK_INT >= 22) {
                am.runningAppProcesses?.mapNotNull { it.pkgList?.firstOrNull() }?.toSet() ?: emptySet()
            } else {
                @Suppress("DEPRECATION")
                am.getRunningAppProcesses()?.mapNotNull { it.pkgList?.firstOrNull() }?.toSet() ?: emptySet()
            }
        } catch (t: Throwable) {
            emptySet()
        }
    }

    /** 当前是否前台可见（借助 UsageStats 的最近使用时间近似）；拿不到权限时返回 null。 */
    fun currentForeground(ctx: Context): String? {
        if (!hasUsageAccess(ctx)) return null
        val usm = ctx.contextService(UsageStatsManager::class.java) ?: return null
        return try {
            val end = System.currentTimeMillis()
            usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, end - 60_000, end)
                ?.maxByOrNull { it.lastTimeUsed }?.packageName
        } catch (t: Throwable) {
            null
        }
    }

    fun icon(ctx: Context, pkg: String): Bitmap? {
        return try {
            val d: Drawable = ctx.packageManager.getApplicationIcon(pkg)
            val w = if (d.intrinsicWidth > 0) d.intrinsicWidth else 96
            val h = if (d.intrinsicHeight > 0) d.intrinsicHeight else 96
            if (d is BitmapDrawable && d.bitmap != null) {
                d.bitmap
            } else {
                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                val c = Canvas(bmp)
                d.setBounds(0, 0, w, h)
                d.draw(c)
                bmp
            }
        } catch (t: Throwable) {
            null
        }
    }
}

/** ContextCompat 风格的小工具，避免额外依赖 */
fun <T> Context.contextService(cls: Class<T>): T? = try {
    getSystemService(cls)
} catch (t: Throwable) {
    null
}

fun Context.launchIntentOf(pkg: String): Intent? {
    return packageManager.getLaunchIntentForPackage(pkg)?.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

fun Context.openAppInfo(pkg: String) {
    runCatching {
        val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.parse("package:$pkg"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(i)
    }
}

fun Context.uninstallIntentOf(pkg: String): Intent =
    Intent(Intent.ACTION_DELETE, Uri.parse("package:$pkg"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
