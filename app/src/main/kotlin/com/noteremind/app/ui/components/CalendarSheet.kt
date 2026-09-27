package com.noteremind.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noteremind.app.ui.theme.AppColors
import com.noteremind.app.util.DateUtils
import com.noteremind.app.util.LunarCalendar
import java.time.LocalDate

private const val YEAR_START = 1900
private const val YEAR_END = 2100

/** 年份选择：每行高度，只显示 4 行 */
private val YEAR_ROW_H = 44.dp
private const val YEAR_VISIBLE_ROWS = 4

/** 日期网格：行高与日期圆点（v1.7 比 v1.6 缩小 20%）；v1.7 起网格紧贴页面上部、下方留空 */
private val DAY_ROW_H = 37.dp
private val DAY_CIRCLE = 33.dp

/**
 * 年份 / 月份面板的上边缘：
 * v1.5 时该面板是「居中、最高 460dp」的浮层，这里用同样的公式贴着顶部摆放，
 * 保证位置与原来一致，只是高度变矮（年份面板只显示 4 行）。
 */
private fun pickerTopPad(screenH: Dp): Dp = ((screenH - 460.dp) / 2).coerceAtLeast(40.dp)

/**
 * v1.6 日期选择：
 * - 整页显示（与输入界面一致，铺满屏幕，不露出下层页面）
 * - 年份 / 月份可直接点击选择（弹出面板只显示 4 行年份）
 * - 支持 阳历 / 阴历 切换，阴历的年月同样用阿拉伯数字显示
 */
@Composable
fun CalendarSheet(
    initialMillis: Long?,
    initialLunar: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (millis: Long, lunar: Boolean) -> Unit
) {
    var lunarMode by remember { mutableStateOf(initialLunar) }
    val today = LocalDate.now()
    var selected by remember {
        mutableStateOf(
            if (initialMillis != null && initialMillis > 0L) DateUtils.millisToDate(initialMillis)
            else today
        )
    }
    var cursorYear by remember { mutableStateOf(selected.year) }
    var cursorMonth by remember { mutableStateOf(selected.monthValue) }
    var lunarCursor by remember { mutableStateOf(LunarCalendar.solarToLunar(selected).copy(day = 1)) }

    var showYearPicker by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }

    
    fun syncFromSelected(date: LocalDate) {
        cursorYear = date.year
        cursorMonth = date.monthValue
        lunarCursor = LunarCalendar.solarToLunar(date).copy(day = 1)
    }

    /** 当前展示的年 / 月（阴历同样用阿拉伯数字） */
    val displayYear = if (!lunarMode) cursorYear else lunarCursor.year
    val displayMonth = if (!lunarMode) cursorMonth else lunarCursor.month

    fun gotoYear(year: Int) {
        val y = year.coerceIn(YEAR_START, YEAR_END)
        if (!lunarMode) {
            cursorYear = y
        } else {
            lunarCursor = lunarCursor.copy(year = y)
        }
    }

    fun gotoMonth(month: Int) {
        val m = month.coerceIn(1, 12)
        if (!lunarMode) {
            cursorMonth = m
        } else {
            lunarCursor = lunarCursor.copy(month = m, isLeap = false)
        }
    }

    SlideUpSheet(onDismiss = onDismiss) { close ->
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶部：返回胶囊 + 居中标题 + 今日 / 确定胶囊
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SheetPillButton("返回", compact = true, onClick = { close() })
                Text(
                    "选择日期",
                    fontSize = 19.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.InkBrown,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                SheetPillButton("今日", compact = true, onClick = {
                    selected = today
                    syncFromSelected(today)
                })
                Spacer(Modifier.width(8.dp))
                SheetPillButton("确定", compact = true, onClick = {
                    onConfirm(DateUtils.dateToMillis(selected), lunarMode)
                })
            }

            // 年 / 月（可直接点击选择） + 阴阳历切换
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 年份
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { showYearPicker = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${displayYear}年",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "选择年份",
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // 月份
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { showMonthPicker = true }
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        (if (lunarMode && lunarCursor.isLeap) "闰" else "") + "${displayMonth}月",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AppColors.TextPrimary
                    )
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "选择月份",
                        tint = AppColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                if (lunarMode) {
                    Text(
                        "阴历",
                        fontSize = 11.sp,
                        color = AppColors.Accent,
                        modifier = Modifier
                            .padding(start = 6.dp)
                            .background(AppColors.AccentSoft, RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(Modifier.weight(1f))

                CalendarModeToggle(lunarMode) { mode ->
                    lunarMode = mode
                    syncFromSelected(selected)
                }
            }

            Spacer(Modifier.height(4.dp))

            // 星期表头
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                listOf("一", "二", "三", "四", "五", "六", "日").forEach { w ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(w, fontSize = 12.sp, color = AppColors.TextSecondary, textAlign = TextAlign.Center)
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // 日期网格：紧贴页面上部，行高比 v1.6 缩小 20%，下方留空
            val lead: Int
            val dayCount: Int
            val dateOfDay: (Int) -> LocalDate
            if (!lunarMode) {
                val first = LocalDate.of(cursorYear, cursorMonth, 1)
                lead = first.dayOfWeek.value - 1
                dayCount = first.lengthOfMonth()
                dateOfDay = { d -> LocalDate.of(cursorYear, cursorMonth, d) }
            } else {
                val firstSolar = LunarCalendar.lunarToSolar(lunarCursor.year, lunarCursor.month, 1, lunarCursor.isLeap)
                lead = firstSolar.dayOfWeek.value - 1
                dayCount = LunarCalendar.daysInLunarMonth(lunarCursor.year, lunarCursor.month, lunarCursor.isLeap)
                dateOfDay = { d -> LunarCalendar.lunarToSolar(lunarCursor.year, lunarCursor.month, d, lunarCursor.isLeap) }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.Top
            ) {
                repeat(6) { row ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        repeat(7) { col ->
                            val idx = row * 7 + col
                            val dayNum = idx - lead + 1
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(DAY_ROW_H)
                                    .padding(1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (dayNum in 1..dayCount) {
                                    val date = dateOfDay(dayNum)
                                    val isSelected = date == selected
                                    val isToday = date == today
                                    Column(
                                        modifier = Modifier
                                            .size(DAY_CIRCLE)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> AppColors.Accent
                                                    isToday -> AppColors.AccentSoft
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = if (isToday && !isSelected) 1.dp else 0.dp,
                                                color = if (isToday && !isSelected) AppColors.Accent else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { selected = date },
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            if (!lunarMode) dayNum.toString() else LunarCalendar.dayName(dayNum),
                                            fontSize = 12.sp,
                                            lineHeight = 13.sp,
                                            fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else AppColors.TextPrimary
                                        )
                                        Text(
                                            if (!lunarMode) {
                                                LunarCalendar.dayName(LunarCalendar.solarToLunar(date).day)
                                            } else "${date.monthValue}/${date.dayOfMonth}",
                                            fontSize = 7.sp,
                                            lineHeight = 8.sp,
                                            color = if (isSelected) Color.White.copy(alpha = 0.85f) else AppColors.TextHint
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))

            // 已选预览
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "已选：${DateUtils.formatCN(selected)} ${DateUtils.weekdayName(selected)}  ${LunarCalendar.lunarText(selected)}",
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    color = AppColors.TextSecondary
                )
            }
        }
    }

    if (showYearPicker) {
        YearPickerSheet(
            currentYear = displayYear,
            onDismiss = { showYearPicker = false },
            onPick = { y ->
                gotoYear(y)
                showYearPicker = false
            }
        )
    }

    if (showMonthPicker) {
        MonthPickerSheet(
            currentMonth = displayMonth,
            onDismiss = { showMonthPicker = false },
            onPick = { m ->
                gotoMonth(m)
                showMonthPicker = false
            }
        )
    }
}

/**
 * 年份选择：1900 - 2100，四列网格，只显示 4 行（高度缩小），
 * 上边缘与 v1.5 的居中浮层保持一致，自动滚动到当前年份。
 */
@Composable
private fun YearPickerSheet(
    currentYear: Int,
    onDismiss: () -> Unit,
    onPick: (Int) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val topPad = pickerTopPad(maxHeight)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.16f))
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() }
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.CardCream,
            shadowElevation = 10.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = topPad, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "选择年份",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.InkBrown,
                    modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
                )
                val years = (YEAR_START..YEAR_END).toList()
                val rows = years.chunked(4)
                val state = rememberLazyListState()
                LaunchedEffect(Unit) {
                    state.scrollToItem(((currentYear - YEAR_START) / 4).coerceIn(0, rows.lastIndex))
                }
                LazyColumn(
                    state = state,
                    modifier = Modifier.height(YEAR_ROW_H * YEAR_VISIBLE_ROWS),
                    contentPadding = PaddingValues(bottom = 2.dp)
                ) {
                    items(rows) { rowItems ->
                        Row(modifier = Modifier.fillMaxWidth()) {
                            rowItems.forEach { y ->
                                val active = y == currentYear
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(YEAR_ROW_H)
                                        .padding(3.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (active) AppColors.Accent else AppColors.SheetField)
                                        .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onPick(y) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        y.toString(),
                                        fontSize = 14.sp,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                        color = if (active) Color.White else AppColors.ChipInk
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 月份选择：1 - 12，上边缘与年份选择面板对齐 */
@Composable
private fun MonthPickerSheet(
    currentMonth: Int,
    onDismiss: () -> Unit,
    onPick: (Int) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val topPad = pickerTopPad(maxHeight)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.16f))
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() }
        )
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = AppColors.CardCream,
            shadowElevation = 10.dp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = topPad, start = 24.dp, end = 24.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "选择月份",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.InkBrown,
                    modifier = Modifier.padding(start = 6.dp, bottom = 8.dp)
                )
                (1..12).toList().chunked(4).forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { m ->
                            val active = m == currentMonth
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(YEAR_ROW_H)
                                    .padding(3.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (active) AppColors.Accent else AppColors.SheetField)
                                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onPick(m) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${m}月",
                                    fontSize = 14.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                                    color = if (active) Color.White else AppColors.ChipInk
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarModeToggle(isLunar: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .gradientShadow(RoundedCornerShape(12.dp), base = 2.dp, layers = 2)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.SheetField)
            .padding(3.dp)
    ) {
        listOf(false to "阳历", true to "阴历").forEach { (value, label) ->
            val active = isLunar == value
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (active) Color.White else Color.Transparent)
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onChange(value) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 13.sp,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (active) AppColors.InkBrown else AppColors.InkBrownSoft
                )
            }
        }
    }
}
