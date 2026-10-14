package com.noteremind.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noteremind.app.ui.theme.AppColors
import com.noteremind.app.util.DateUtils
import java.time.LocalDate

data class EditorResult(
    val title: String,
    val millis: Long,
    val lunar: Boolean,
    val repeatValue: Int,
    val repeatUnit: Int,
    val remind: Boolean,
    val icon: String,
    val color: String,
    /** v2.0：备注（不在列表显示，仅编辑时可见） */
    val note: String = ""
)

/** 面板内各行的左右内边距 */
private val ROW_PAD = 18.dp

/** 图标列宽 + 间距，用于让分隔线与文字左对齐 */
private val ICON_COL = 31.dp

/** v2.0：统一行高——所有行固定等高，比 v1.9 更紧凑且各行完全一致 */
private val ROW_H = 48.dp

/** 标题输入胶囊的高度（在 48dp 行内垂直居中） */
private val FIELD_H = 40.dp

/** v2.0：备注框高度（约两行文字，固定高度避免输入时页面跳动） */
private val NOTE_H = 64.dp

/** 「每 N [单位] 重复」里两个底色框的尺寸（v1.6：两者完全等大） */
private val BOX_W = 52.dp
private val BOX_H = 32.dp

private val PillShape = RoundedCornerShape(percent = 50)

/**
 * 「别忘了」/「纪念日」通用输入界面（v2.0）：
 * - 整页显示（不再只占屏幕下半部分），面板铺满屏幕
 * - 顶部「返回」「保存」白胶囊按钮，标题居中
 * - 各项为「前置简约矢量图标 + 文字」的平铺行，不用卡片底色区分
 * - v2.0：所有行统一 48dp 等高（更紧凑）；新增「备注框」（备注不在列表显示，
 *   仅点击修改后可见）；颜色栏右侧同步显示当前所选颜色（圆形色块 + 名称）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorSheet(
    title: String,
    placeholder: String = "输入提醒事项…",
    initialTitle: String = "",
    initialMillis: Long? = null,
    initialLunar: Boolean = false,
    initialRepeatValue: Int = 0,
    initialRepeatUnit: Int = 0,
    initialRemind: Boolean = false,
    initialIcon: String = "star",
    initialColor: String = "m1",
    initialNote: String = "",
    showRepeat: Boolean = true,
    showColor: Boolean = true,
    dateHint: String = "日期",
    onDismiss: () -> Unit,
    onSave: (EditorResult) -> Unit
) {
    val today = LocalDate.now()

    var text by remember { mutableStateOf(initialTitle) }
    // 未指定日期时默认显示当日日期
    var millis by remember { mutableStateOf(initialMillis ?: DateUtils.dateToMillis(today)) }
    var lunar by remember { mutableStateOf(initialLunar) }
    var repeatValue by remember { mutableStateOf(initialRepeatValue) }
    var repeatUnit by remember { mutableStateOf(initialRepeatUnit) }
    var remind by remember { mutableStateOf(initialRemind) }
    var icon by remember { mutableStateOf(initialIcon) }
    var colorKey by remember { mutableStateOf(initialColor) }
    // v2.0：备注（不在列表显示）
    var note by remember { mutableStateOf(initialNote) }

    var showCalendar by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    val canSave = text.isNotBlank()

    SlideUpSheet(onDismiss = onDismiss) { close ->
        Column(modifier = Modifier.fillMaxSize()) {

            // 顶部：返回胶囊 + 居中标题 + 保存胶囊
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SheetPillButton("返回", onClick = { close() })
                Text(
                    title,
                    fontSize = 19.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AppColors.InkBrown,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                SheetPillButton("保存", enabled = canSave, onClick = {
                    if (canSave) {
                        onSave(
                            EditorResult(
                                title = text.trim(),
                                millis = millis,
                                lunar = lunar,
                                repeatValue = repeatValue,
                                repeatUnit = repeatUnit,
                                remind = remind,
                                icon = icon,
                                color = colorKey,
                                note = note.trim()
                            )
                        )
                        close()
                    }
                })
            }

            // 内容：紧贴页面上部依次排列，各行等高（下方留空，不再整页均分）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.Top
            ) {
                // ① 事项：胶囊输入框（前置图标），固定 48dp 行高
                RowGroup {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ROW_H)
                            .padding(horizontal = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(FIELD_H)
                                .gradientShadow(PillShape, base = 3.dp, layers = 3)
                                .clip(PillShape)
                                .background(AppColors.SheetField)
                                .padding(horizontal = ROW_PAD)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SheetLeadingIcon(Icons.Filled.Edit)
                                BasicTextField(
                                    value = text,
                                    onValueChange = { text = it },
                                    singleLine = true,
                                    textStyle = TextStyle(fontSize = 16.sp, color = AppColors.InkBrown),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { inner ->
                                        if (text.isEmpty()) {
                                            Text(placeholder, fontSize = 16.sp, color = AppColors.TextHint)
                                        }
                                        inner()
                                    }
                                )
                            }
                        }
                    }
                }

                // ② v2.0：备注框——备注不在列表显示，只点击修改后显示
                RowGroup {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(NOTE_H)
                                .gradientShadow(RoundedCornerShape(14.dp), base = 2.dp, layers = 2)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AppColors.SheetField)
                                .padding(horizontal = ROW_PAD, vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    Icons.Filled.Note,
                                    contentDescription = null,
                                    tint = AppColors.TextSecondary,
                                    modifier = Modifier
                                        .size(19.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                BasicTextField(
                                    value = note,
                                    onValueChange = { note = it },
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        lineHeight = 19.sp,
                                        color = AppColors.InkBrown
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { inner ->
                                        if (note.isEmpty()) {
                                            Text(
                                                "写点备注…（不在列表中显示）",
                                                fontSize = 14.sp,
                                                lineHeight = 19.sp,
                                                color = AppColors.TextHint
                                            )
                                        }
                                        inner()
                                    }
                                )
                            }
                        }
                    }
                }

                // ③ 日期
                RowGroup {
                    SheetRow(
                        icon = Icons.Filled.CalendarMonth,
                        label = dateHint,
                        onClick = { showCalendar = true }
                    ) {
                        val d = DateUtils.millisToDate(millis)
                        Text(
                            "${DateUtils.formatCN(d)} ${DateUtils.weekdayName(d)}" +
                                if (lunar) "（阴历）" else "",
                            fontSize = 14.sp,
                            color = AppColors.InkBrownSoft
                        )
                        Spacer(Modifier.width(6.dp))
                        SheetChevron()
                    }
                }

                // ④ 重复：每 [N] [单位] 重复（两个底色框等宽等高、内容居中）
                if (showRepeat) {
                    RowGroup {
                        SheetRow(icon = Icons.Filled.Repeat, label = "重复") {
                            Text("每", fontSize = 14.sp, color = AppColors.InkBrownSoft)
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(BOX_W, BOX_H)
                                    .gradientShadow(RoundedCornerShape(9.dp), base = 2.dp, layers = 2)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(AppColors.SheetField),
                                contentAlignment = Alignment.Center
                            ) {
                                BasicTextField(
                                    value = if (repeatValue <= 0) "" else repeatValue.toString(),
                                    onValueChange = { v ->
                                        repeatValue = v.filter { it.isDigit() }.take(3).toIntOrNull() ?: 0
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    textStyle = TextStyle(
                                        fontSize = 14.sp,
                                        color = AppColors.InkBrown,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { inner ->
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (repeatValue <= 0) {
                                                Text(
                                                    "1",
                                                    fontSize = 14.sp,
                                                    color = AppColors.TextHint,
                                                    textAlign = TextAlign.Center,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                            inner()
                                        }
                                    }
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                            // 单位：点击后以胶囊按钮形式浮层展开
                            InlineChooser(
                                options = listOf("天", "周", "月", "年"),
                                selectedIndex = repeatUnit,
                                onSelect = { repeatUnit = it },
                                panelWidth = 200.dp
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(BOX_W, BOX_H)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(AppColors.SheetField),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            DateUtils.unitName(repeatUnit),
                                            fontSize = 14.sp,
                                            color = AppColors.InkBrown
                                        )
                                        Icon(
                                            Icons.Filled.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = AppColors.InkBrownSoft,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(6.dp))
                            Text("重复", fontSize = 14.sp, color = AppColors.InkBrownSoft)
                        }
                    }
                }

                // ⑤ 设置提醒（开关缩小到与文字等高，整行与其他行一致）
                RowGroup {
                    SheetRow(icon = Icons.Filled.NotificationsActive, label = "设置提醒") {
                        // 外层固定高度与文字行一致，点击整个开关区域都能切换
                        Box(
                            modifier = Modifier
                                .size(width = 46.dp, height = 22.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { remind = !remind },
                            contentAlignment = Alignment.Center
                        ) {
                            // 关闭 Material3 的 48dp 最小触控尺寸（正是它把该行撑高）
                            CompositionLocalProvider(
                                LocalMinimumInteractiveComponentEnforcement provides false
                            ) {
                                Switch(
                                    checked = remind,
                                    onCheckedChange = { remind = it },
                                    modifier = Modifier.scale(0.66f),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = AppColors.Accent,
                                        uncheckedThumbColor = Color.White,
                                        uncheckedTrackColor = AppColors.Divider
                                    )
                                )
                            }
                        }
                    }
                }

                // ⑥ 图标（预览色随所选颜色变化，与列表一致）
                RowGroup(showDivider = showColor) {
                    SheetRow(
                        icon = Icons.Filled.EmojiEmotions,
                        label = "图标",
                        onClick = { showIconPicker = true }
                    ) {
                        val entryColor = MorandiPalette.byKey(colorKey)
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .gradientShadow(RoundedCornerShape(10.dp), base = 2.dp, layers = 2)
                                .clip(RoundedCornerShape(10.dp))
                                .background(entryColor.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                IconSet.byKey(icon),
                                contentDescription = null,
                                tint = entryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("更换", fontSize = 13.sp, color = AppColors.InkBrownSoft)
                        Spacer(Modifier.width(2.dp))
                        SheetChevron()
                    }
                }

                // ⑦ 颜色（v2.0：栏右侧同步显示当前所选颜色，选中即有反馈）
                if (showColor) {
                    RowGroup(showDivider = false) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(ROW_H)
                                    .padding(horizontal = ROW_PAD),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SheetLeadingIcon(Icons.Filled.Palette)
                                Text("颜色", fontSize = 16.sp, color = AppColors.InkBrown)
                                Spacer(Modifier.weight(1f))
                                // 右侧：当前所选颜色的圆形色块 + 名称，随点选实时同步
                                val entry = MorandiPalette.all.find { it.key == colorKey }
                                    ?: MorandiPalette.all[0]
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .shadow(
                                            elevation = 4.dp,
                                            shape = CircleShape,
                                            ambientColor = Color(0x33000000),
                                            spotColor = Color(0x22000000)
                                        )
                                        .clip(CircleShape)
                                        .background(entry.color)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    entry.name,
                                    fontSize = 13.sp,
                                    color = AppColors.InkBrownSoft
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(start = ROW_PAD + ICON_COL, end = ROW_PAD, bottom = 6.dp)
                            ) {
                                MorandiPalette.all.forEach { entry ->
                                    val active = entry.key == colorKey
                                    Box(
                                        modifier = Modifier
                                            .padding(end = 14.dp)
                                            .size(34.dp)
                                            .shadow(
                                                elevation = if (active) 8.dp else 3.dp,
                                                shape = CircleShape,
                                                ambientColor = Color(0x33000000),
                                                spotColor = Color(0x22000000)
                                            )
                                            .clip(CircleShape)
                                            .background(entry.color)
                                            .clickable(
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ) { colorKey = entry.key }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCalendar) {
        CalendarSheet(
            initialMillis = millis,
            initialLunar = lunar,
            onDismiss = { showCalendar = false },
            onConfirm = { m, l ->
                millis = m
                lunar = l
                showCalendar = false
            }
        )
    }

    if (showIconPicker) {
        IconPickerSheet(
            selected = icon,
            onDismiss = { showIconPicker = false },
            onPick = { icon = it; showIconPicker = false }
        )
    }
}

/** 一行 + 其下的分隔线，作为整体参与「平均分布」 */
@Composable
private fun RowGroup(showDivider: Boolean = true, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        content()
        if (showDivider) SheetDivider(indent = true)
    }
}

/** 面板里行首的简约矢量小图标（含右侧间距） */
@Composable
private fun SheetLeadingIcon(icon: ImageVector) {
    Icon(
        icon,
        contentDescription = null,
        tint = AppColors.TextSecondary,
        modifier = Modifier.size(19.dp)
    )
    Spacer(Modifier.width(12.dp))
}

@Composable
private fun SheetChevron() {
    Icon(
        Icons.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = AppColors.TextHint,
        modifier = Modifier.size(18.dp)
    )
}

/** 细分隔线；indent = true 时与文字左边缘对齐 */
@Composable
private fun SheetDivider(indent: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (indent) ROW_PAD + ICON_COL else 0.dp)
            .height(1.dp)
            .background(AppColors.Divider)
    )
}

/** 无底色的平铺行：前置图标 + 文字 + 右侧内容；v2.0 起固定 48dp 行高，各行完全一致 */
@Composable
private fun SheetRow(
    icon: ImageVector,
    label: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier
            .then(
                if (onClick == null) Modifier
                else Modifier.clickable(
                    indication = null,
                    interactionSource = MutableInteractionSource()
                ) { onClick() }
            )
            .fillMaxWidth()
            .height(ROW_H)
            .padding(horizontal = ROW_PAD),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SheetLeadingIcon(icon)
        Text(label, fontSize = 16.sp, color = AppColors.InkBrown)
        Spacer(Modifier.weight(1f))
        trailing()
    }
}

/** 40 图标选择器 */
@Composable
fun IconPickerSheet(
    selected: String,
    onDismiss: () -> Unit,
    onPick: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.16f))
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismiss() }
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
                )
                .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
                .background(AppColors.SheetBg)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("选择图标", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = AppColors.InkBrown)
                Spacer(Modifier.height(12.dp))
                val rows = IconSet.all.chunked(6)
                rows.forEach { rowItems ->
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        rowItems.forEach { entry ->
                            val active = entry.key == selected
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(13.dp))
                                    .background(if (active) AppColors.AccentSoft else Color.Transparent)
                                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onPick(entry.key) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    entry.image,
                                    contentDescription = null,
                                    tint = if (active) AppColors.Accent else AppColors.InkBrownSoft,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
