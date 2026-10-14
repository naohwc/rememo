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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noteremind.app.data.AppRepo
import com.noteremind.app.data.LifeEvent
import com.noteremind.app.ui.components.InlineActionMenu
import com.noteremind.app.ui.components.MorandiPalette
import com.noteremind.app.ui.components.creamCard
import com.noteremind.app.ui.components.gradientShadow
import com.noteremind.app.ui.components.readableInk
import com.noteremind.app.ui.theme.AppColors
import com.noteremind.app.util.DateUtils
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** 时间轴列宽 / 竖线横坐标中心（dp） */
private const val AXIS_WIDTH_DP = 38
private const val AXIS_LINE_CENTER_DP = 14
/** 卡片内部左右内边距（dp）：年份节点据此与列表标题左对齐 */
private const val CARD_PAD_DP = 14

/**
 * 人生轨迹（v1.4）：
 * - 多人轨迹：按归属筛选，默认全部显示在同一时间线
 * - 年龄按对应人计算；事件类归属显示「第 N 年」
 * - 年份节点：年份前后各一条横线（不再用三角形）
 * - 日期与年龄移到卡片内「标题下一行」；删掉原来的阴历 / 距今文案
 * - 时间轴圆点缩小并垂直居中于条目
 */
@Composable
fun LifeTrackScreen(onEdit: (LifeEvent) -> Unit) {
    val data by AppRepo.data.collectAsState()
    val allEvents = remember(data.lifeEvents) {
        data.lifeEvents.sortedByDescending { it.dateMillis }
    }

    var selectedId by remember { mutableStateOf<String?>(null) }
    var filterTag by remember { mutableStateOf<String?>(null) } // null = 全部

    val events = remember(allEvents, filterTag) {
        if (filterTag == null) allEvents else allEvents.filter { it.ownerId == filterTag }
    }

    // 筛选标签：全部 / 我 / 各归属
    val tags: List<Pair<String?, String>> = remember(allEvents, data.owners) {
        buildList {
            add(null to "全部")
            if (allEvents.any { it.ownerId.isEmpty() }) add("" to "我")
            data.owners.forEach { add(it.id to it.name) }
        }
    }

    /** 年龄 / 第几年 标签（v1.9：带归属名——「我几岁 / 谁几岁 / 事项第几年」） */
    fun ageLabel(ownerId: String, date: LocalDate): String? {
        val owner = if (ownerId.isEmpty()) null else data.owners.find { it.id == ownerId }
        return when {
            owner == null && ownerId.isNotEmpty() -> null
            owner == null ->
                data.birthdayMillis?.let { DateUtils.ageAt(it, date)?.let { a -> "我${a}岁" } }
            owner.isPerson ->
                DateUtils.ageAt(owner.anchorMillis, date)?.let { "${owner.name}${it}岁" }
            else -> {
                val anchor = DateUtils.millisToDate(owner.anchorMillis)
                val years = ChronoUnit.YEARS.between(anchor, date).toInt() + 1
                "${owner.name}第${years}年"
            }
        }
    }

    /**
     * v2.0：标签底色——归属人名下所有条目统一用该归属的颜色（修改一个则全部更改）；
     * 「我」（无归属）沿用条目自身颜色。
     */
    fun chipColorFor(event: LifeEvent): Color {
        if (event.ownerId.isEmpty()) return MorandiPalette.byKey(event.color)
        val owner = data.owners.find { it.id == event.ownerId }
        return MorandiPalette.byKey(owner?.color ?: event.color)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            // 点击列表/页面的任意空白处，收回展开的修改、删除菜单
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { selectedId = null }
    ) {
        // 顶部标题（v1.6：去掉标题后的「我的生日……」，只保留模块标题）
        Text(
            "人生轨迹",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
        )

        // 筛选标签行（参考 标签.jpg：未选中灰白胶囊，选中白底阴影）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tags.forEach { (id, name) ->
                val active = filterTag == id
                val shape = RoundedCornerShape(20.dp)
                Box(
                    modifier = Modifier
                        .gradientShadow(
                            shape,
                            base = if (active) 3.dp else 2.dp,
                            layers = if (active) 3 else 2
                        )
                        .clip(shape)
                        .background(if (active) AppColors.TagActive else AppColors.TagIdle)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { filterTag = id }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        name,
                        fontSize = 13.sp,
                        lineHeight = 15.sp,
                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (active) AppColors.InkBrown else AppColors.InkBrownSoft
                    )
                }
            }
        }

        if (events.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "点右下角「+」记录人生中的一件事",
                    fontSize = 14.sp,
                    color = AppColors.TextHint
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 130.dp)
            ) {
                itemsIndexed(events, key = { _, e -> e.id }) { index, event ->
                    val date = DateUtils.millisToDate(event.dateMillis)
                    val age = ageLabel(event.ownerId, date)
                    // 年份变化处，在时间线前面标注年份节点
                    val showYear = index == 0 ||
                        DateUtils.millisToDate(events[index - 1].dateMillis).year != date.year

                    Column(modifier = Modifier.fillMaxWidth()) {
                        if (showYear) {
                            YearNode(year = date.year)
                            Spacer(Modifier.height(8.dp))
                        }

                        TimelineItem(
                            event = event,
                            date = date,
                            age = age,
                            chipColor = chipColorFor(event),
                            showLine = index != events.lastIndex,
                            selected = selectedId == event.id,
                            onClick = {
                                selectedId = if (selectedId == event.id) null else event.id
                            },
                            onEdit = { onEdit(event) },
                            onDelete = {
                                selectedId = null
                                AppRepo.deleteLifeEvent(event.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 单条轨迹：轴（圆点垂直居中 + 竖线）+ 卡片；点选时卡片缩小到 90%，
 * 「修改 / 删除」在卡片正下方内联展开，后续条目整体下移。
 */
@Composable
private fun TimelineItem(
    event: LifeEvent,
    date: LocalDate,
    age: String?,
    chipColor: Color,
    showLine: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.7f),
        label = "lifeScale"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                if (showLine) {
                    val w = 2.dp.toPx()
                    drawRect(
                        color = AppColors.Divider,
                        topLeft = Offset(AXIS_LINE_CENTER_DP.dp.toPx() - w / 2f, 0f),
                        size = Size(w, size.height)
                    )
                }
            }
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min).fillMaxWidth()) {
            // 时间轴：小圆点垂直居中于卡片（竖线由外层 drawBehind 贯穿）
            Box(
                modifier = Modifier
                    .width(AXIS_WIDTH_DP.dp)
                    .fillMaxHeight()
                    .padding(end = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .background(AppColors.Accent, CircleShape)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .creamCard(RoundedCornerShape(14.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onClick() }
                    .padding(horizontal = CARD_PAD_DP.dp, vertical = 12.dp)
            ) {
                // 标题
                Text(
                    event.text,
                    fontSize = 15.sp,
                    lineHeight = 18.sp,
                    color = AppColors.InkBrown
                )
                // 标题下一行：日期 + 年龄
                Spacer(Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "${DateUtils.formatCN(date)} ${DateUtils.weekdayName(date)}",
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        color = AppColors.InkBrownSoft
                    )
                    if (age != null) {
                        Spacer(Modifier.width(8.dp))
                        // v2.0：底色 = 归属人的统一颜色（由调用方按归属计算）；
                        // 文字按底色明暗自动切换深/白
                        Box(
                            modifier = Modifier
                                .gradientShadow(RoundedCornerShape(8.dp), base = 2.dp, layers = 2)
                                .clip(RoundedCornerShape(8.dp))
                                .background(chipColor)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                age,
                                fontSize = 11.sp,
                                lineHeight = 13.sp,
                                color = readableInk(chipColor)
                            )
                        }
                    }
                }
            }
        }

        // 修改 / 删除：缩进到与卡片左边缘对齐
        AnimatedVisibility(
            visible = selected,
            enter = expandVertically(
                animationSpec = tween(180),
                expandFrom = Alignment.Top
            ) + fadeIn(tween(160)),
            exit = shrinkVertically(
                animationSpec = tween(150),
                shrinkTowards = Alignment.Top
            ) + fadeOut(tween(120))
        ) {
            Box(modifier = Modifier.padding(start = AXIS_WIDTH_DP.dp)) {
                InlineActionMenu(onEdit = onEdit, onDelete = onDelete)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

/** 年份节点：年份前后各一条横线（v1.4 去掉三角形；v1.5 与列表标题左对齐） */
@Composable
private fun YearNode(year: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(AXIS_WIDTH_DP.dp)
                .padding(end = 10.dp)
                .height(1.dp)
                .background(AppColors.Divider)
        )
        // 与卡片内标题文字左边缘对齐
        Spacer(Modifier.width(CARD_PAD_DP.dp))
        Text(
            "${year}年",
            fontSize = 13.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.Accent,
            maxLines = 1,
            softWrap = false
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(AppColors.Divider)
        )
    }
}
