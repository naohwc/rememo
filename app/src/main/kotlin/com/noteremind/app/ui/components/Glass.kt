package com.noteremind.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.noteremind.app.ui.theme.AppColors

/**
 * 外渐变阴影（v1.6）：
 * 叠加多层「扩散半径递增 + 透明度递减」的阴影，模拟由内向外渐渐变淡的
 * 渐变光晕。minSdk 26 上无法使用 RenderEffect 模糊，用分层阴影是最稳的等效方案。
 */
fun Modifier.gradientShadow(
    shape: Shape,
    base: Dp = 4.dp,
    layers: Int = 3,
    tint: Color = AppColors.ShadowTint
): Modifier {
    var m: Modifier = this
    repeat(layers) { i ->
        val e = base * (1f + i * 1.5f)
        val a = (0.22f - i * 0.06f).coerceAtLeast(0.06f)
        m = m.shadow(
            elevation = e,
            shape = shape,
            ambientColor = tint.copy(alpha = a),
            spotColor = tint.copy(alpha = a * 0.75f),
            clip = false
        )
    }
    return m
}

/**
 * 半透明毛玻璃：柔和外扩阴影 + 半透明白底渐变。
 * 不依赖第三方库，在浅色内容上呈现清晰的 frosted glass 观感。
 *
 * v1.2：增强「光感边缘 + 阴影轮廓」（参考 轮廓.jpg）。
 * v1.4：去掉白色描边与顶部内高光（轮廓光），只保留四周发散阴影。
 */
fun Modifier.frostGlass(
    shape: Shape,
    alpha: Float = 0.74f,
    elevation: Dp = 12.dp,
    tint: Color = Color.White
): Modifier = this
    .gradientShadow(shape = shape, base = elevation / 3f)
    .clip(shape)
    .background(
        brush = Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = (alpha + 0.10f).coerceAtMost(0.92f)),
                tint.copy(alpha = alpha)
            )
        )
    )

/** 页面 / 弹层统一底色（v1.5：#EFF1F3） */
@Composable
fun AppBackdrop(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColors.BgNeutral)
    )
}

/**
 * 列表卡片（v1.6）：
 * 纯白卡片（列表22.png）+ 外渐变阴影（多层扩散，向内实、向外虚），
 * 浮在浅灰页面底色上。
 */
fun Modifier.creamCard(
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 5.dp
): Modifier = this
    .gradientShadow(shape = shape, base = elevation, layers = 3)
    .clip(shape)
    .background(AppColors.CardCream)
