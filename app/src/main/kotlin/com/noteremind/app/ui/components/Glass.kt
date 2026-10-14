package com.noteremind.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
 * v2.0 阴影色板：
 * - 列表卡片：近层 黑 20% / 6dp + 远层 黑 10% / 14dp 的双层投影
 *   （v1.9 的单层 4% 在 #EFF1F3 底上几乎看不见，v2.0 应要求加重）
 * - 加号 FAB：黑 20%/下移 4 + 黑 10%/下移 10 的双层投影（xiaozhi FAB 同款）
 */
private val ListShadowNear = Color(0x33000000)
private val ListShadowFar = Color(0x1A000000)
private val FabShadowNear = Color(0x33000000)
private val FabShadowFar = Color(0x1A000000)

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
 * 列表卡片（v2.0）：
 * 纯白卡片 + 1px 浅灰细边 + 「近深远浅」双层投影：
 * 近层 6dp / 黑 20% 贴卡片勾勒轮廓，远层 14dp / 黑 10% 向外扩散，
 * 在 #EFF1F3 页面底色上清晰可见。别忘了 / 纪念日 / 人生轨迹 / 设置共用。
 */
fun Modifier.creamCard(
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 6.dp
): Modifier = this
    .shadow(
        elevation = elevation + 8.dp,
        shape = shape,
        ambientColor = ListShadowFar,
        spotColor = ListShadowFar,
        clip = false
    )
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = ListShadowNear,
        spotColor = ListShadowNear,
        clip = false
    )
    .clip(shape)
    .background(AppColors.CardCream)
    .border(1.dp, AppColors.CardGlow, shape)

/** v1.9：加号 FAB 的双层柔和投影（xiaozhi FAB 同款：近层深、远层淡） */
fun Modifier.fabShadow(shape: Shape): Modifier = this
    .shadow(
        elevation = 10.dp,
        shape = shape,
        ambientColor = FabShadowFar,
        spotColor = FabShadowFar,
        clip = false
    )
    .shadow(
        elevation = 4.dp,
        shape = shape,
        ambientColor = FabShadowNear,
        spotColor = FabShadowNear,
        clip = false
    )
