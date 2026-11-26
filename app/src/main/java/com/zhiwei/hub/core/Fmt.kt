package com.zhiwei.hub.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

object Fmt {

    fun bytes(v: Long): String {
        if (v <= 0L) return "—"
        val kb = 1024.0
        return when {
            v < kb -> "$v B"
            v < kb * kb -> String.format(Locale.US, "%.0f KB", v / kb)
            v < kb * kb * kb -> String.format(Locale.US, "%.1f MB", v / kb / kb)
            else -> String.format(Locale.US, "%.2f GB", v / kb / kb / kb)
        }
    }

    fun duration(ms: Long): String {
        if (ms <= 0L) return "0 分钟"
        val min = ms / 60000L
        return when {
            min < 1L -> "不到 1 分钟"
            min < 60L -> "$min 分钟"
            min < 60L * 24 -> "${min / 60} 小时 ${min % 60} 分"
            else -> "${min / (60 * 24)} 天"
        }
    }

    /** 相对时间：刚刚 / x 分钟前 / x 小时前 / x 天前 / 日期 */
    fun ago(ts: Long): String {
        if (ts <= 0L) return "无记录"
        val d = System.currentTimeMillis() - ts
        if (d < 0) return "刚刚"
        val min = d / 60000L
        return when {
            d < 45_000L -> "刚刚"
            min < 60L -> "$min 分钟前"
            min < 60L * 24 -> "${min / 60} 小时前"
            min < 60L * 24 * 7 -> "${min / (60 * 24)} 天前"
            else -> date(ts)
        }
    }

    fun date(ts: Long): String = if (ts <= 0L) "—" else
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(ts))

    fun dateTime(ts: Long): String = if (ts <= 0L) "—" else
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(ts))

    /** 版本号比较：1.10.0 > 1.9.9 */
    fun versionGreater(a: String, b: String): Boolean {
        val pa = a.trim().removePrefix("v").split('.', '-', '+').mapNotNull { it.toIntOrNull() }
        val pb = b.trim().removePrefix("v").split('.', '-', '+').mapNotNull { it.toIntOrNull() }
        for (i in 0 until maxOf(pa.size, pb.size)) {
            val x = pa.getOrElse(i) { 0 }
            val y = pb.getOrElse(i) { 0 }
            if (x != y) return x > y
        }
        return false
    }

    fun pct(a: Int, b: Int): String = if (b <= 0) "0%" else "${(a * 100.0 / b).roundToLong()}%"
}
