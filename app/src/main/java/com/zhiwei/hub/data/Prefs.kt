package com.zhiwei.hub.data

import android.content.Context
import com.zhiwei.hub.ui.Accent

/** 轻量偏好存储：背景、主题、刷新策略。 */
class Prefs(ctx: Context) {

    private val sp = ctx.getSharedPreferences("zhiwei_hub", Context.MODE_PRIVATE)

    /** gradient / image / solid */
    var bgMode: String
        get() = sp.getString("bg_mode", "gradient") ?: "gradient"
        set(v) = sp.edit().putString("bg_mode", v).apply()

    var bgUri: String?
        get() = sp.getString("bg_uri", null)
        set(v) = sp.edit().putString("bg_uri", v).apply()

    var bgGradient: Int
        get() = sp.getInt("bg_grad", 0)
        set(v) = sp.edit().putInt("bg_grad", v).apply()

    /** 0f..0.9f 背景暗化 */
    var bgDim: Float
        get() = sp.getFloat("bg_dim", 0.12f)
        set(v) = sp.edit().putFloat("bg_dim", v).apply()

    /** 0f..25f 背景模糊 */
    var bgBlur: Float
        get() = sp.getFloat("bg_blur", 0f)
        set(v) = sp.edit().putFloat("bg_blur", v).apply()

    var accent: Accent
        get() = Accent.of(sp.getString("accent", null))
        set(v) = sp.edit().putString("accent", v.name).apply()

    /** 打开应用时自动扫描 */
    var autoScan: Boolean
        get() = sp.getBoolean("auto_scan", true)
        set(v) = sp.edit().putBoolean("auto_scan", v).apply()

    /** 自动检查更新 */
    var autoUpdate: Boolean
        get() = sp.getBoolean("auto_update", true)
        set(v) = sp.edit().putBoolean("auto_update", v).apply()

    /** 卡片显示密度：comfortable / compact */
    var density: String
        get() = sp.getString("density", "comfortable") ?: "comfortable"
        set(v) = sp.edit().putString("density", v).apply()

    companion object {
        val GRADIENTS = listOf(
            "深空紫" to intArrayOf(0xFF120B2B.toInt(), 0xFF2A1150.toInt(), 0xFF0A0714.toInt()),
            "极光青" to intArrayOf(0xFF04201F.toInt(), 0xFF0B3B47.toInt(), 0xFF05100F.toInt()),
            "暮色橙" to intArrayOf(0xFF231007.toInt(), 0xFF4A1F10.toInt(), 0xFF0C0604.toInt()),
            "樱雾粉" to intArrayOf(0xFF2A0E1C.toInt(), 0xFF4A1B33.toInt(), 0xFF12060B.toInt()),
            "石墨黑" to intArrayOf(0xFF0B0B0F.toInt(), 0xFF16161C.toInt(), 0xFF050507.toInt())
        )
    }
}