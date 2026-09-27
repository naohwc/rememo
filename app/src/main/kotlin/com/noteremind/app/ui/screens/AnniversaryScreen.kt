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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noteremind.app.data.Anniversary
import com.noteremind.app.data.AppRepo
import com.noteremind.app.ui.components.DayBadge
import com.noteremind.app.ui.components.IconSet
import com.noteremind.app.ui.components.InlineActionMenu
import com.noteremind.app.ui.components.MorandiPalette
import com.noteremind.app.ui.components.creamCard
import com.noteremind.app.ui.theme.AppColors
import com.noteremind.app.util.DateUtils
import com.noteremind.app.util.LunarCalendar

/**
 * 纪念日：列表22.png 样式；
 * 「剩余 N 天」改为两行徽标（数字按 3 个字符占位，底色与「已 N 天」一致），
 * 图标颜色跟随所选颜色（与「别忘了」一致）。
 */
@Composable
fun AnniversaryScreen(onEdit: (Anniversary) -> Unit) {
    val data by AppRepo.data.collectAsState()
    val list = remember(data.anniversaries) {
        data.anniversaries.sortedBy {
            DateUtils.daysUntilAnnual(it.dateMillis, it.lunar)
        }
    }
    var selectedId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // 点击列表/页面的任意空白处，收回展开的修改、删除菜单
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { selectedId = null }
    ) {
        Text(
            "纪念日",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
        )

        if (list.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("点右下角「+」添加纪念日", fontSize = 14.sp, color = AppColors.TextHint)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 130.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(list, key = { it.id }) { item ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        AnniversaryCard(
                            item = item,
                            selected = selectedId == item.id,
                            onClick = {
                                selectedId = if (selectedId == item.id) null else item.id
                            }
                        )
                        // v1.4：修改/删除菜单在内联展开，后续条目整体下移
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
                                    AppRepo.deleteAnniversary(item.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnniversaryCard(item: Anniversary, selected: Boolean, onClick: () -> Unit) {
    val date = DateUtils.millisToDate(item.dateMillis)
    val remain = DateUtils.daysUntilAnnual(item.dateMillis, item.lunar)
    val entryColor = MorandiPalette.byKey(item.color)
    val scale by animateFloatAsState(
        targetValue = if (selected) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "annivScale"
    )

    Box(
        modifier = Modifier
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
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(entryColor.copy(alpha = 0.16f), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    IconSet.byKey(item.icon),
                    contentDescription = null,
                    tint = entryColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    fontSize = 15.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.InkBrown,
                    maxLines = 1
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "${DateUtils.formatDash(date)} ${DateUtils.weekdayName(date)}" +
                        if (item.lunar) "（阴历）" else "",
                    fontSize = 11.sp,
                    lineHeight = 13.sp,
                    color = AppColors.InkBrownSoft
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    LunarCalendar.lunarText(date),
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                    color = AppColors.TextHint
                )
            }

            Spacer(Modifier.width(10.dp))

            // 右侧单行徽标「还有 [数字] 天」（v1.8：「剩余」改为「还有」），只有数字带条目颜色的底色
            DayBadge(
                label = "还有",
                number = remain,
                numberChars = 3,
                badgeColor = entryColor
            )
        }
    }
}
