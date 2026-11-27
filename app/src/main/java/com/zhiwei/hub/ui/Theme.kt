package com.zhiwei.hub.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 调色板：以「深空 + 霓虹边缘光」为主调。
 * 深色不是主题选项，而是唯一主题 —— 让玻璃与光效有足够的对比空间。
 */
object ZW {
    val Bg = Color(0xFF0A1020)
    val Bg2 = Color(0xFF101A30)
    val Card = Color(0xFF151A2E)
    val CardHi = Color(0xFF1D2440)
    val Line = Color(0x22FFFFFF)
    val LineSoft = Color(0x0DFFFFFF)
    val Text = Color(0xFFF3F3F8)
    val TextDim = Color(0xFF9A9AAB)
    val TextFaint = Color(0xFF6C6C7C)

    val Pink = Color(0xFFFF6FA5)
    val Blue = Color(0xFF6FA8FF)
    val Cyan = Color(0xFF56D9C8)
    val Green = Color(0xFF4ADE80)
    val Amber = Color(0xFFFFC24B)
    val Red = Color(0xFFFF5D6A)
    val Purple = Color(0xFFA97BFF)
}

/** 主题色方案：设置页可切换，影响强调色与光晕。 */
enum class Accent(val label: String, val color: Color, val second: Color) {
    SAKURA("樱粉", Color(0xFFFF6FA5), Color(0xFFA97BFF)),
    AURORA("极光", Color(0xFF56D9C8), Color(0xFF6FA8FF)),
    EMBER("琥珀", Color(0xFFFFC24B), Color(0xFFFF6F6F)),
    OCEAN("海蓝", Color(0xFF3B82F6), Color(0xFF06B6D4));

    companion object {
        fun of(name: String?): Accent = entries.firstOrNull { it.name == name } ?: SAKURA
    }
}

private val ZWTypography = Typography(
    displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 40.sp, letterSpacing = (-1).sp),
    headlineMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 26.sp, letterSpacing = (-0.5).sp),
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 19.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 13.5.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 13.sp, letterSpacing = 0.2.sp),
    labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 0.6.sp)
)

@Composable
fun ZhiweiTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = ZW.Pink,
        onPrimary = Color.White,
        background = ZW.Bg,
        onBackground = ZW.Text,
        surface = ZW.Card,
        onSurface = ZW.Text,
        surfaceVariant = ZW.CardHi,
        onSurfaceVariant = ZW.TextDim,
        outline = ZW.Line,
        error = ZW.Red
    )
    MaterialTheme(colorScheme = scheme, typography = ZWTypography, content = content)
}
