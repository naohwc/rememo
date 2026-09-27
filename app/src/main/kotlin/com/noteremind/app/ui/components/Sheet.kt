package com.noteremind.app.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.noteremind.app.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.pow

/** 整页面板（v1.6）：四角不做圆角，整屏铺满，不露出下层页面 */
private val SheetShape = RoundedCornerShape(0.dp)

/** 胶囊按钮：白底、全圆角 */
private val PillShape = RoundedCornerShape(percent = 50)

/**
 * v1.6 弹层容器：
 * - 整页显示：宽度撑满、高度占满（maxHeightFraction 默认 1），从下方滑出后铺满整屏
 * - 面板同色的整屏底随面板一起淡入，过渡期间也看不到下层页面
 * - 系统返回（含边缘左/右滑手势）= 关闭当前页，而不是退出应用
 */
@Composable
fun SlideUpSheet(
    topGap: Dp = 0.dp,
    onDismiss: () -> Unit,
    maxHeightFraction: Float = 1f,
    bottomOffset: Dp = 0.dp,
    content: @Composable (close: () -> Unit) -> Unit
) {
    val scope = rememberCoroutineScope()
    var visible by remember { mutableStateOf(false) }
    var closing by remember { mutableStateOf(false) }

    val close = {
        if (!closing) {
            closing = true
            scope.launch {
                visible = false
                delay(300)
                onDismiss()
            }
        }
        Unit
    }

    BackHandler(onBack = { close() })
    LaunchedEffect(Unit) { visible = true }

    Box(modifier = Modifier.fillMaxSize()) {
        // 面板以外区域点击可关闭（整页时仅动画途中可见）
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { close() }
        )

        // 与面板同色的整屏底：滑出过程中遮挡下层页面
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(160))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.SheetBg)
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                animationSpec = tween(340, easing = FastOutSlowInEasing),
                initialOffsetY = { it }
            ) + fadeIn(tween(200)),
            exit = slideOutVertically(
                animationSpec = tween(280, easing = FastOutLinearInEasing),
                targetOffsetY = { it }
            ) + fadeOut(tween(160)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(maxHeightFraction)
                    .padding(top = topGap, bottom = bottomOffset)
                    .clip(SheetShape)
                    .background(AppColors.SheetBg)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {}
                ) {
                    content(close)
                }
            }
        }
    }
}

/** 白胶囊外观（不含点击），供需要自行处理点击的场景（如行内选项触发器）复用 */
@Composable
fun PillSurface(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    enabled: Boolean = true,
    horizontalPadding: Dp = 20.dp,
    verticalPadding: Dp = 10.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .gradientShadow(
                shape = PillShape,
                base = if (enabled) 3.dp else 0.dp,
                layers = if (enabled) 3 else 0
            )
            .clip(PillShape)
            .background(if (selected) AppColors.Accent else AppColors.SheetPill)
            .padding(horizontal = horizontalPadding, vertical = verticalPadding),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

/** 弹出页里的白色胶囊按钮（返回 / 保存 / 今日 / 确定），带外渐变阴影 */
@Composable
fun SheetPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    compact: Boolean = false,
    selected: Boolean = false
) {
    PillSurface(
        modifier = modifier.clickable(
            enabled = enabled,
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() },
        selected = selected,
        enabled = enabled,
        horizontalPadding = if (compact) 15.dp else 20.dp,
        verticalPadding = 10.dp
    ) {
        Text(
            text,
            fontSize = 15.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = when {
                !enabled -> AppColors.SheetDisabled
                selected -> Color.White
                else -> AppColors.InkBrown
            }
        )
    }
}

/**
 * 「修改 / 删除」菜单（v1.6）：
 * 白色全圆角胶囊，图标在左、文字在右（横向排列），整体右对齐；
 * 作为列表条目的一部分紧贴条目下方展开，后续条目会被自然下移。
 */
@Composable
fun InlineActionMenu(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Row(
            modifier = Modifier
                .gradientShadow(shape = PillShape, base = 3.dp, layers = 3)
                .clip(PillShape)
                .background(AppColors.MenuPill)
                .padding(horizontal = 6.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MenuAction(Icons.Filled.Edit, "修改", AppColors.InkBrown) { onEdit() }
            Spacer(Modifier.width(2.dp))
            MenuAction(Icons.Filled.DeleteOutline, "删除", AppColors.Danger) { onDelete() }
        }
    }
}

@Composable
private fun MenuAction(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            label,
            fontSize = 13.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Medium,
            color = tint
        )
    }
}

/**
 * 列表右侧的天数徽标（v1.7）：
 * 「已 [数字] 天」/「剩余 [数字] 天」整体**排在一行**，只有数字带底色，
 * 「已 / 剩余 / 天」这些字不加底色；底色取条目所选颜色，
 * 数字比 v1.6（24sp）略小，居中显示在约两行文字高的色块里；
 * 数字按 numberChars 个字符预留固定宽度，因此各条目右侧文字位置完全对齐。
 */
private val DIGIT_SLOT = 11.dp

/** 数字色块高度 ≈ 两行正文高度 */
private val BADGE_H = 26.dp

@Composable
fun DayBadge(
    label: String,
    number: Long,
    numberChars: Int,
    badgeColor: Color,
    modifier: Modifier = Modifier,
    unit: String = "天"
) {
    // 底色明的时候用深字，暗的时候用白字，保证数字始终可读
    val onBadge = if (badgeColor.relativeLuminance() > 0.26f) AppColors.InkBrown else Color.White

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            label,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            color = AppColors.InkBrownSoft
        )
        Spacer(Modifier.width(4.dp))
        Box(
            modifier = Modifier
                .width(DIGIT_SLOT * numberChars)
                .height(BADGE_H)
                .gradientShadow(RoundedCornerShape(7.dp), base = 2.dp, layers = 2)
                .clip(RoundedCornerShape(7.dp))
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                number.toString(),
                fontSize = 17.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Bold,
                color = onBadge,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(4.dp))
        Text(
            unit,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            color = AppColors.InkBrownSoft
        )
    }
}

/** WCAG 相对亮度（0=黑, 1=白）：用来挑一个在给定底色上可读的文字颜色 */
private fun Color.relativeLuminance(): Float {
    fun lin(c: Float): Float =
        if (c <= 0.03928f) c / 12.92f else ((c + 0.055f) / 1.055f).pow(2.4f)
    return 0.2126f * lin(red) + 0.7152f * lin(green) + 0.0722f * lin(blue)
}

/**
 * 行内选项（v1.6）：
 * 触发按钮保持原样，展开后以「每个选项一个胶囊按钮」的形式横向排列（放不下自动换行），
 * 并以浮层形式悬浮在下一行之上（Popup），点击其他任意位置即收回。
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InlineChooser(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    panelWidth: Dp = 230.dp,
    trigger: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var triggerSize by remember { mutableStateOf(IntSize.Zero) }
    val density = LocalDensity.current

    Box(
        modifier = modifier.onGloballyPositioned { triggerSize = it.size }
    ) {
        Box(
            modifier = Modifier.clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { expanded = !expanded }
        ) {
            trigger()
        }

        if (expanded) {
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(
                    x = triggerSize.width - with(density) { panelWidth.roundToPx() },
                    y = triggerSize.height + with(density) { 6.dp.roundToPx() }
                ),
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                Column(
                    modifier = Modifier
                        .width(panelWidth)
                        .gradientShadow(shape = RoundedCornerShape(18.dp), base = 5.dp, layers = 3)
                        .clip(RoundedCornerShape(18.dp))
                        .background(AppColors.SheetBg)
                        .padding(10.dp)
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        options.forEachIndexed { index, label ->
                            SheetPillButton(
                                text = label,
                                compact = true,
                                selected = index == selectedIndex,
                                onClick = {
                                    onSelect(index)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
