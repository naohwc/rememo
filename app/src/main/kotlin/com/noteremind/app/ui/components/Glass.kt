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
 * v2.4 阴影色板（**范围收紧 + 颜色加深**）：
 * 用户反馈"范围小一点、颜色深一点"；实测多层 shadow 的模糊阴影会向下偏移
 * 渗入形状内部，扩散距离越大渗入越深（导航栏胶囊上沿因此出现一条横贯亮线），
 * 所以同时收紧 elevation 并提高不透明度：
 * - 列表卡片：近 95% / 中 83% / 远 61%，扩散由 +16/+6/+0 收紧到 +6/+2/+0
 * - 加号 FAB：黑 83% / 黑 54%，elevation 8/3
 * - 色板演进：v2.0 20%/10% → v2.1 40/26/14 → v2.2 54/38/24 → v2.3 69/54/36 → v2.4 95/83/61
 */
private val ListShadowNear = Color(0xF2000000)
private val ListShadowMid = Color(0xD4000000)
private val ListShadowFar = Color(0x9C000000)
private val FabShadowNear = Color(0xD4000000)
private val FabShadowFar = Color(0x8A000000)

/** 弹层 / 图标选择面板投影（v2.4 加深） */
val SheetShadowDeep = Color(0xC4000000)

/** 底部导航栏单层投影（v2.4：单层 + 收紧范围，避免边缘亮线） */
private val BarShadow = Color(0xB8000000)

/** 颜色圆点 / 小色块的投影（v2.4 加深） */
val DotShadowNear = Color(0xD4000000)
val DotShadowFar = Color(0xA0000000)

/**
 * 底部导航栏实底（v2.4 新增，修复"亮线"）：
 * 原先导航栏用 frostGlass 的半透明背景。Compose 的 shadow 画在 layer 内容**下方**，
 * 半透明背景会让阴影从内部透出，与 frostGlass 自带渐变阴影、barShadow 叠加后
 * 在胶囊上沿合成出一条横贯的亮线（实测该处亮度 251，周边仅 236）。
 * 改用不透明底：颜色与"半透明白叠 #EFF1F3"的结果一致，观感不变，但不再透出阴影。
 */
fun Modifier.barSurface(shape: Shape): Modifier = this
    .clip(shape)
    .background(Color(0xFFFBFBFC))

/**
 * 外渐变阴影（v1.6，v2.2 加深）：
 * 叠加多层「扩散半径递增 + 透明度递减」的阴影，模拟由内向外渐渐变淡的
 * 渐变光晕。minSdk 26 上无法使用 RenderEffect 模糊，用分层阴影是最稳的等效方案。
 * v2.2：起始透明度 0.22 → 0.30，逐层递减 0.06 → 0.07，整体更深。
 * v2.3：起始 0.30 → 0.38，逐层递减 0.07 → 0.09。
 * v2.4：起始 0.38 → 0.49，逐层递减 0.09 → 0.11（+30%）。
 *       再调：起始 0.49 → 0.62，递减 0.11 → 0.14（颜色加深），扩散范围仍由各调用处的 base 控制。
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
        val a = (0.62f - i * 0.14f).coerceAtLeast(0.20f)
        m = m.shadow(
            elevation = e,
            shape = shape,
            ambientColor = Color.Transparent,
            spotColor = tint.copy(alpha = a),
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
 * 列表卡片：
 * 纯白卡片 + 三层纯色投影（无描边、无浅色辉光）：
 *   近层 elevation      / 黑 95% → 贴卡片勾边
 *   中层 elevation+2dp  / 黑 83% → 过渡
 *   远层 elevation+6dp  / 黑 61% → 向外扩散
 * 别忘了 / 纪念日 / 人生轨迹 / 设置四个页面共用。
 * v2.4：扩散距离由 +16/+6/+0 收紧到 +6/+2/+0（范围更紧凑），
 *       同时颜色大幅加深——近距离的浓阴影，立体感更硬朗且不会糊成一片。
 */
fun Modifier.creamCard(
    shape: Shape = RoundedCornerShape(20.dp),
    elevation: Dp = 7.dp
): Modifier = this
    .shadow(
        elevation = elevation + 3.dp,
        shape = shape,
        ambientColor = Color.Transparent,
        spotColor = ListShadowFar,
        clip = false
    )
    .shadow(
        elevation = elevation + 1.dp,
        shape = shape,
        ambientColor = Color.Transparent,
        spotColor = ListShadowMid,
        clip = false
    )
    .shadow(
        elevation = elevation,
        shape = shape,
        ambientColor = Color.Transparent,
        spotColor = ListShadowNear,
        clip = false
    )
    .clip(shape)
    .background(AppColors.CardCream)

/** 加号 FAB 的双层投影（v2.4：范围收紧、颜色加深） */
fun Modifier.fabShadow(shape: Shape): Modifier = this
    .shadow(
        elevation = 6.dp,
        shape = shape,
        ambientColor = Color.Transparent,
        spotColor = FabShadowFar,
        clip = false
    )
    .shadow(
        elevation = 2.dp,
        shape = shape,
        ambientColor = Color.Transparent,
        spotColor = FabShadowNear,
        clip = false
    )

/**
 * 底部导航栏投影（v2.3 新增，v2.4 改单层 + 收紧）：
 * v2.3.1 用三层叠加（26/15/8dp），实测在胶囊上沿合成出一条横贯的亮线
 * （顶部几行未被偏移阴影覆盖 → 露出纯背景色 251，往下被压暗到 234）。
 * 曾试过去掉半透明底仍未消除 → 确认是多层 shadow 的模糊阴影渗入形状内部所致。
 * 现改为**单层 12dp 深色**：深度够、渗入浅，亮线消除。
 */
fun Modifier.barShadow(shape: Shape): Modifier = this
    .shadow(
        elevation = 8.dp,
        shape = shape,
        ambientColor = Color.Transparent,
        spotColor = BarShadow,
        clip = false
    )
