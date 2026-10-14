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
 * v2.3 阴影色板（在 v2.2 基础上再加深一档）：
 * - 列表卡片：三层纯黑白灰投影（近 69% / 中 54% / 远 36%），
 *   演进：v2.0 20%/10% → v2.1 40%/26%/14% → v2.2 54%/38%/24% → v2.3 69%/54%/36%
 * - 加号 FAB：黑 36%/下移 4 + 黑 20%/下移 10 的双层投影
 * - 卡片四周仍无浅色描边（v2.1 已移除 CardGlow），只靠投影拉开层次
 */
private val ListShadowNear = Color(0xB0000000)
private val ListShadowMid = Color(0x8A000000)
private val ListShadowFar = Color(0x5C000000)
private val FabShadowNear = Color(0x5C000000)
private val FabShadowFar = Color(0x33000000)

/** 弹层 / 图标选择面板投影（v2.3：显式指定更深的纯黑投影） */
val SheetShadowDeep = Color(0x80000000)

/** 颜色圆点 / 小色块的投影（v2.3 加深） */
val DotShadowNear = Color(0x5C000000)
val DotShadowFar = Color(0x3D000000)

/**
 * 外渐变阴影（v1.6，v2.2 加深）：
 * 叠加多层「扩散半径递增 + 透明度递减」的阴影，模拟由内向外渐渐变淡的
 * 渐变光晕。minSdk 26 上无法使用 RenderEffect 模糊，用分层阴影是最稳的等效方案。
 * v2.2：起始透明度 0.22 → 0.30，逐层递减 0.06 → 0.07，整体更深。
 * v2.3：起始 0.30 → 0.38，逐层递减 0.07 → 0.09。
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
        val a = (0.38f - i * 0.09f).coerceAtLeast(0.12f)
        m = m.shadow(
            elevation = e,
            shape = shape,
            ambientColor = tint.copy(alpha = a),
            spotColor = tint.copy(alpha = a * 0.9f),
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
 * 列表卡片（v2.1）：
 * 纯白卡片 + 三层纯色投影（无描边、无浅色辉光）：
 *   近层 elevation / 黑 40%  → 贴卡片勾边
 *   中层 elevation+6dp / 黑 26% → 过渡
 *   远层 elevation+16dp / 黑 14% → 向外扩散
 * 别忘了 / 纪念日 / 人生轨迹 / 设置四个页面共用。
 * v2.1 变更：去掉 v2.0 的 1px 浅色描边（CardGlow #F2F3F5），
 * 该描边在浅底上会呈现"浅色轮廓/发光"效果；阴影整体再加一档。
 */
fun Modifier.creamCard(
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 7.dp
): Modifier = this
    .shadow(
        elevation = elevation + 16.dp,
        shape = shape,
        ambientColor = ListShadowFar,
        spotColor = ListShadowFar,
        clip = false
    )
    .shadow(
        elevation = elevation + 6.dp,
        shape = shape,
        ambientColor = ListShadowMid,
        spotColor = ListShadowMid,
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

/**
 * 底部导航栏投影（v2.3 新增）：
 * 三层纯黑投影，扩散距离比列表卡片更大——胶囊导航浮在滚动内容之上，
 * 需要更宽更深的投影才能拉开层次。与 frostGlass 内部的渐变阴影叠加使用。
 */
fun Modifier.barShadow(shape: Shape): Modifier = this
    .shadow(
        elevation = 26.dp,
        shape = shape,
        ambientColor = ListShadowFar,
        spotColor = ListShadowFar,
        clip = false
    )
    .shadow(
        elevation = 15.dp,
        shape = shape,
        ambientColor = ListShadowMid,
        spotColor = ListShadowMid,
        clip = false
    )
    .shadow(
        elevation = 8.dp,
        shape = shape,
        ambientColor = ListShadowNear,
        spotColor = ListShadowNear,
        clip = false
    )
