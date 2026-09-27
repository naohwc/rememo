package com.noteremind.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.noteremind.app.data.AppRepo
import com.noteremind.app.data.ReminderItem
import com.noteremind.app.ui.components.DayBadge
import com.noteremind.app.ui.components.IconSet
import com.noteremind.app.ui.components.InlineActionMenu
import com.noteremind.app.ui.components.MorandiPalette
import com.noteremind.app.ui.components.creamCard
import com.noteremind.app.ui.components.gradientShadow
import com.noteremind.app.ui.theme.AppColors
import com.noteremind.app.util.DateUtils
import java.time.LocalDate
import java.util.Collections

@Composable
fun DontForgetScreen(onEdit: (ReminderItem) -> Unit) {
    val data by AppRepo.data.collectAsState()

    val list = remember { mutableStateListOf<ReminderItem>() }
    LaunchedEffect(data.reminders) {
        val sorted = data.reminders.sortedBy { it.order }
        list.clear()
        list.addAll(sorted)
    }

    var selectedId by remember { mutableStateOf<String?>(null) }
    var draggingId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val heights = remember { mutableStateMapOf<String, Int>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // 点击列表/页面的任意空白处，收回展开的修改、删除菜单
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { selectedId = null }
    ) {
        // 顶部标题（v1.6：原「今天是……」换成模块标题）
        Text(
            "提醒记事",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
        )

        if (list.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "还没有提醒，点右下角「+」添加",
                    fontSize = 14.sp,
                    color = AppColors.TextHint
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 2.dp, bottom = 130.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(list, key = { _, item -> item.id }) { _, item ->
                    val isDragging = draggingId == item.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .zIndex(if (isDragging) 2f else 0f)
                            .offset { IntOffset(0, if (isDragging) dragOffset.toInt() else 0) }
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            ReminderCard(
                                item = item,
                                selected = selectedId == item.id,
                                onClick = {
                                    if (draggingId == null) {
                                        selectedId = if (selectedId == item.id) null else item.id
                                    }
                                },
                                modifier = Modifier.onSizeChanged { heights[item.id] = it.height },
                                onDragHandle = { change ->
                                    dragOffset += change
                                    val idx = list.indexOfFirst { it.id == item.id }
                                    if (idx >= 0) {
                                        if (dragOffset > 0 && idx < list.lastIndex) {
                                            val nh = heights[list[idx + 1].id] ?: 96
                                            if (dragOffset >= nh) {
                                                Collections.swap(list, idx, idx + 1)
                                                dragOffset -= nh
                                            }
                                        } else if (dragOffset < 0 && idx > 0) {
                                            val ph = heights[list[idx - 1].id] ?: 96
                                            if (-dragOffset >= ph) {
                                                Collections.swap(list, idx, idx - 1)
                                                dragOffset += ph
                                            }
                                        }
                                    }
                                },
                                onDragStart = { draggingId = item.id; dragOffset = 0f; selectedId = null },
                                onDragStop = {
                                    draggingId = null
                                    dragOffset = 0f
                                    AppRepo.saveReminderOrder(list.map { it.id })
                                }
                            )

                            // v1.4：修改/删除菜单在条目正下方展开，后续条目整体下移
                            AnimatedVisibility(
                                visible = selectedId == item.id,
                                enter = expandVertically(
                                    animationSpec = tween(180),
                                    expandFrom = Alignment.Top
                                ) + fadeIn(tween(160)),
                                exit = shrinkVertically(
                                    animationSpec = tween(150),
                                    shrinkTowards = Alignment.Top
                                ) + fadeOut(tween(120))
                            ) {
                                InlineActionMenu(
                                    onEdit = { onEdit(item) },
                                    onDelete = {
                                        selectedId = null
                                        AppRepo.deleteReminder(item.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 条目卡片（参考 列表22.png）：
 * - 左侧竖色块 = 所选颜色（同时是拖动把手）
 * - 左侧三行：小图标 + 标题 / 日期 / 重复 + 剩余 + 下次
 * - 右侧两行天数徽标「已 N 天」：数字按 4 个字符占位、字号与纪念日一致
 */
@Composable
private fun ReminderCard(
    item: ReminderItem,
    selected: Boolean,
    onClick: () -> Unit,
    onDragStart: () -> Unit,
    onDragStop: () -> Unit,
    onDragHandle: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val origin = DateUtils.millisToDate(item.dateMillis)
    val next = DateUtils.nextDate(item.dateMillis, item.lunar, item.repeatValue, item.repeatUnit, today)
    val remainDays = DateUtils.daysBetween(today, next)
    val elapsedDays = DateUtils.daysBetween(origin, today)
    val entryColor = MorandiPalette.byKey(item.color)
    val scale by animateFloatAsState(
        targetValue = if (selected) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "reminderScale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .creamCard(RoundedCornerShape(20.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 11.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // 竖色块：条目颜色（拖动把手）
            Box(
                modifier = Modifier
                    .size(width = 5.dp, height = 46.dp)
                    .background(entryColor, RoundedCornerShape(3.dp))
                    .pointerInput(item.id) {
                        detectDragGestures(
                            onDragStart = { onDragStart() },
                            onDragEnd = { onDragStop() },
                            onDragCancel = { onDragStop() },
                            onDrag = { _, amount -> onDragHandle(amount.y) }
                        )
                    }
            )

            Spacer(Modifier.width(11.dp))

            // 文字区
            Column(modifier = Modifier.weight(1f)) {
                // 第一行：图标 + 标题
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        IconSet.byKey(item.icon),
                        contentDescription = null,
                        tint = entryColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        item.title,
                        fontSize = 14.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.InkBrown,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 第二行：日期
                Spacer(Modifier.height(2.dp))
                Text(
                    "${DateUtils.formatCN(origin)} ${DateUtils.weekdayName(origin)}" +
                        if (item.lunar) "（阴历）" else "",
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    color = AppColors.InkBrownSoft,
                    maxLines = 1
                )

                // 第三行：重复 + 剩余（v1.8：未设置重复时不显示「剩余」）
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Chip(DateUtils.repeatText(item.repeatValue, item.repeatUnit))
                    if (item.repeatValue > 0) {
                        Spacer(Modifier.width(5.dp))
                        Chip(DateUtils.remainText(remainDays))
                    }
                }
            }

            Spacer(Modifier.width(10.dp))

            // 右侧（v1.8）：「已 N 天」占上面两行，「下次：…」在第三行，均右对齐；
            // 未设置重复时不显示「下次」
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End
            ) {
                DayBadge(
                    label = "已",
                    number = elapsedDays,
                    numberChars = 4,
                    badgeColor = entryColor
                )
                if (item.repeatValue > 0) {
                    Text(
                        "下次：${DateUtils.formatShort(next)}",
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                        color = AppColors.TextHint
                    )
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String) {
    Box(
        modifier = Modifier
            .gradientShadow(RoundedCornerShape(8.dp), base = 2.dp, layers = 2)
            .background(AppColors.ChipCream, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, fontSize = 11.sp, lineHeight = 13.sp, color = AppColors.ChipInk)
    }
}
