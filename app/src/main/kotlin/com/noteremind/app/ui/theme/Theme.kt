package com.noteremind.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 浅色、中性、干净的「iOS 设置页」配色
// v1.5：底色 / 卡片 / 文字全部按 列表22.png 与 弹出.png 取色
object AppColors {
    val Bg = Color(0xFFEFF1F3)
    val BgWarm = Color(0xFFEFF1F3)
    val Surface = Color(0xFFFFFFFF)
    val TextPrimary = Color(0xFF181818)
    val TextSecondary = Color(0xFF6E6E6E)
    val TextHint = Color(0xFFABABAB)
    val Divider = Color(0xFFE2E3E5)
    val Accent = Color(0xFF3E6B52)
    val AccentSoft = Color(0xFFE7EFE9)
    val Danger = Color(0xFFD9604B)
    val BarInk = Color(0xFF181818)
    val ChipBg = Color(0xFFF0F1F2)
    /** v1.1：底部导航选中态的淡雅高亮底色 */
    val BarActive = Color(0xFFE4EDE7)
    /** 选中态前景（比 Accent 略柔） */
    val BarActiveInk = Color(0xFF3E6B52)

    // ---------- v1.5：参考「列表22.png / 弹出.png」 ----------

    /** 所有页面统一底色（列表22.png 取色 #EFF1F3） */
    val BgNeutral = Color(0xFFEFF1F3)

    /** 列表卡片：纯白（列表22.png） */
    val CardCream = Color(0xFFFFFFFF)
    /** v2.1：列表卡片描边已移除（原先此色在浅底上呈现浅色轮廓/发光），此处仅保留常量备用 */
    val CardGlow = Color(0xFFF2F3F5)
    /** 主文字：近黑（列表22.png 标题 #181818） */
    val InkBrown = Color(0xFF181818)
    /** 次级文字：中灰（列表22.png 正文 #6E6E6E） */
    val InkBrownSoft = Color(0xFF6E6E6E)
    /** 胶囊 chip 底（中性浅灰，替代原淡黄） */
    val ChipCream = Color(0xFFF1F2F3)
    /** chip 文字 */
    val ChipInk = Color(0xFF4A4A4A)

    /** 筛选标签未选中底（比页面底色深一档，保证在 #EFF1F3 底上可见） */
    val TagIdle = Color(0xFFE3E5E8)
    /** 筛选标签选中底（白底阴影） */
    val TagActive = Color(0xFFFFFFFF)

    /** 导航栏未选中图标文字 */
    val BarIdleInk = Color(0xFF2B2B2B)
    /** 导航栏选中项：半透明玻璃底 */
    val BarActiveGlass = Color(0xFFE3E5E8)

    // ---------- v1.5 弹层（弹出.png） ----------
    /** 弹出面板底色 #EFEFEF */
    val SheetBg = Color(0xFFEFEFEF)
    /** 面板内的胶囊输入框底色 #E5E5E5 */
    val SheetField = Color(0xFFE5E5E5)
    /** 面板内的白色胶囊按钮 #FAFAFA */
    val SheetPill = Color(0xFFFAFAFA)
    /** 面板内未选中/禁用文字 #B4B4B4 */
    val SheetDisabled = Color(0xFFB4B4B4)
    /** 修改/删除菜单胶囊底 */
    val MenuPill = Color(0xFFFFFFFF)

    // ---------- v1.6 ----------
    /** 列表右侧天数徽标底色（别忘了「已N天」与纪念日「剩余N天」共用） */
    val BadgeBg = Color(0xFFF1F2F3)
    /** 外渐变阴影的着色（中性冷灰，避免发黄） */
    val ShadowTint = Color(0xFF6B7684)
}

private val LightColors = lightColorScheme(
    primary = AppColors.Accent,
    onPrimary = Color.White,
    primaryContainer = AppColors.AccentSoft,
    onPrimaryContainer = AppColors.Accent,
    background = AppColors.Bg,
    onBackground = AppColors.TextPrimary,
    surface = AppColors.Surface,
    onSurface = AppColors.TextPrimary,
    surfaceVariant = AppColors.BgWarm,
    onSurfaceVariant = AppColors.TextSecondary,
    outline = AppColors.Divider
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7FBF95),
    onPrimary = Color(0xFF0F2A1C),
    background = Color(0xFF151614),
    surface = Color(0xFF1D1F1C),
    onBackground = Color(0xFFEDEEEA),
    onSurface = Color(0xFFEDEEEA),
    outline = Color(0xFF33352F)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography(),
        content = content
    )
}
