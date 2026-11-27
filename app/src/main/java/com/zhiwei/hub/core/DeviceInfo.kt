package com.zhiwei.hub.core

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.io.File

/** 进程快照 */
data class ProcInfo(
    val name: String,
    val pid: Int,
    val importance: Int,
    val pkg: String?
) {
    /** 重要性：前台 > 可见 > 服务 > 后台 > 空 */
    val importanceLabel: String
        get() = when (importance) {
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND -> "前台"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE -> "可见"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_SERVICE -> "服务"
            ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED -> "缓存"
            else -> "后台"
        }
}

/** 完整设备信息快照 */
data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val brand: String,
    val androidVersion: String,
    val sdkInt: Int,
    val cpuCores: Int,
    val cpuAbi: String,
    val cpuMaxFreqKhz: Long,
    val totalMem: Long,
    val availMem: Long,
    val storageTotal: Long,
    val storageFree: Long,
    val screenW: Int,
    val screenH: Int,
    val densityDpi: Int,
    val batteryLevel: Int,
    val processes: List<ProcInfo>
) {
    val usedMem: Long get() = (totalMem - availMem).coerceAtLeast(0L)
    val storageUsed: Long get() = (storageTotal - storageFree).coerceAtLeast(0L)
}

object DeviceInfoProbe {

    fun collect(ctx: Context): DeviceInfo {
        val am = ctx.contextService(ActivityManager::class.java)

        // 内存
        var totalMem = 0L
        var availMem = 0L
        am?.let {
            val mi = ActivityManager.MemoryInfo()
            it.getMemoryInfo(mi)
            totalMem = mi.totalMem
            availMem = mi.availMem
        }

        // 存储（内部存储）
        var storageTotal = 0L
        var storageFree = 0L
        runCatching {
            val sf = StatFs(Environment.getDataDirectory().path)
            storageTotal = sf.totalBytes
            storageFree = sf.availableBytes
        }

        // 屏幕
        val dm = ctx.resources.displayMetrics
        val screenW = dm.widthPixels
        val screenH = dm.heightPixels
        val densityDpi = dm.densityDpi

        // 电池
        var battery = -1
        runCatching {
            val bm = ctx.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            battery = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
        }

        // CPU 最大频率（从 sysfs 读，失败回退 0）
        var cpuMaxFreq = 0L
        runCatching {
            val f = File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
            if (f.exists()) cpuMaxFreq = f.readText().trim().toLongOrNull() ?: 0L
        }

        // 进程列表
        val procs = am?.let { runningProcesses(it) } ?: emptyList()

        return DeviceInfo(
            model = Build.MODEL ?: "未知",
            manufacturer = Build.MANUFACTURER ?: "未知",
            brand = Build.BRAND ?: "未知",
            androidVersion = Build.VERSION.RELEASE ?: "未知",
            sdkInt = Build.VERSION.SDK_INT,
            cpuCores = Runtime.getRuntime().availableProcessors(),
            cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "未知",
            cpuMaxFreqKhz = cpuMaxFreq,
            totalMem = totalMem,
            availMem = availMem,
            storageTotal = storageTotal,
            storageFree = storageFree,
            screenW = screenW,
            screenH = screenH,
            densityDpi = densityDpi,
            batteryLevel = battery,
            processes = procs
        )
    }

    private fun runningProcesses(am: ActivityManager): List<ProcInfo> {
        return try {
            am.runningAppProcesses
                ?.sortedByDescending { it.importance }
                ?.map {
                    ProcInfo(
                        name = it.processName ?: "?",
                        pid = it.pid,
                        importance = it.importance,
                        pkg = it.pkgList?.firstOrNull()
                    )
                }
                ?: emptyList()
        } catch (t: Throwable) {
            emptyList()
        }
    }
}