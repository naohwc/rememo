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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noteremind.app.data.AppRepo
import com.noteremind.app.data.LifeEvent
import com.noteremind.app.data.TrackOwner
import com.noteremind.app.ui.theme.AppColors
import com.noteremind.app.util.DateUtils
import java.time.LocalDate

private val ROW_PAD = 18.dp
private val ICON_COL = 31.dp

/** v2.0：统一行高——所有行固定等高，比 v1.9 更紧凑且各行完全一致 */
private val ROW_H = 48.dp

/** 标题输入胶囊的高度（在 48dp 行内垂直居中） */
private val FIELD_H = 40.dp

/** v2.0：备注框高度（约两行文字，固定高度避免输入时页面跳动） */
private val NOTE_H = 64.dp

private val PillShape = RoundedCornerShape(percent = 50)

/**
 * 人生轨迹输入（v2.0）：
 * - 整页灰面板 + 白胶囊按钮 + 前置矢量图标平铺行，所有行统一 48dp 等高
 * - 新增「备注框」（备注不在列表显示，仅点击修改后可见）
 * - 每一项编辑时都显示其起始日 / 生日：整行点击即可直接更改（取消「修改日期」按钮）；
 *   归属为「我」时显示我的生日（点击可改全局生日）
 * - v2.0：归属人的标签颜色保持统一——选中已有归属时颜色栏载入该归属的颜色，
 *   保存后该归属名下所有条目的标签底色全部改为该颜色（修改一个则全部更改）
 */
@Composable
fun LifeEventSheet(
    initial: LifeEvent?,
    onDismiss: () -> Unit,
    onSave: (String, Long, String, String, String) -> Unit
) {
    val today = LocalDate.now()
    val data by AppRepo.data.collectAsState()
    val owners = data.owners

    var text by remember { mutableStateOf(initial?.text ?: "") }
    var millis by remember { mutableStateOf(initial?.dateMillis ?: DateUtils.dateToMillis(today)) }
    // v2.0：备注（不在列表显示）
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var showCalendar by remember { mutableStateOf(false) }
    var calendarForAnchor by remember { mutableStateOf(false) }

    val initialOwner = initial?.ownerId?.let { id -> owners.find { it.id == id } }
    var ownerName by remember { mutableStateOf(initialOwner?.name ?: "") }
    var matchedOwner by remember { mutableStateOf<TrackOwner?>(initialOwner) }
    var isPerson by remember { mutableStateOf(initialOwner?.isPerson ?: true) }
    var anchorMillis by remember {
        mutableStateOf(initialOwner?.anchorMillis ?: (initial?.dateMillis ?: DateUtils.dateToMillis(today)))
    }
    // 正在修改生日/起始日的已有归属（null = 新建归属的锚点日期）
    var editingOwnerAnchor by remember { mutableStateOf<TrackOwner?>(null) }
    // v2.0：正在修改「我的生日」（全局）
    var editingMyBirthday by remember { mutableStateOf(false) }
    // v2.0：条目颜色；已有归属优先用该归属的统一颜色（列表中标签底色即此色）
    var colorKey by remember {
        mutableStateOf(initialOwner?.color ?: (initial?.color ?: "m1"))
    }

    val canSave = text.isNotBlank()
    val trimmedName = ownerName.trim()
    // 名字有效且不是已有归属 → 需要设置类型与锚点
    val needOwnerSetup = trimmedName.isNotEmpty() && owners.none { it.name == trimmedName }

    // 归属候选：我 + 已有归属
    val ownerOptions = remember(owners) {
        buildList {
            add("我")
            owners.forEach { add(if (it.isPerson) it.name else it.name + "（事件）") }
        }
    }
    val ownerIndex = if (ownerName.isEmpty()) 0
    else owners.indexOfFirst { it.name == ownerName }.let { if (it >= 0) it + 1 else -1 }

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
                    if (initial == null) "记录人生点滴" else "编辑记录",
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
                        var ownerId = ""
                        if (trimmedName.isNotEmpty()) {
                            val exist = owners.find { it.name == trimmedName }
                            ownerId = exist?.id ?: run {
                                val id = AppRepo.newId()
                                AppRepo.upsertOwner(
                                    TrackOwner(
                                        id = id,
                                        name = trimmedName,
                                        isPerson = isPerson,
                                        anchorMillis = anchorMillis,
                                        color = colorKey
                                    )
                                )
                                id
                            }
                            if (exist != null) {
                                // 已有归属：颜色以本次编辑为准，保持归属人标签颜色统一
                                AppRepo.upsertOwner(exist.copy(color = colorKey))
                            }
                            // 该归属名下所有条目标签底色统一改为当前颜色（修改一个则全部更改）
                            AppRepo.recolorLifeEventsByOwner(ownerId, colorKey)
                        }
                        onSave(text.trim(), millis, ownerId, colorKey, note.trim())
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
                // ① 这一刻：胶囊输入框，固定 48dp 行高
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
                                .clip(PillShape)
                                .background(AppColors.SheetField)
                                .padding(horizontal = ROW_PAD)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SheetLeadingIcon(Icons.Filled.Edit)
                                BasicTextField(
                                    value = text,
                                    onValueChange = { text = it },
                                    textStyle = TextStyle(
                                        fontSize = 16.sp,
                                        color = AppColors.InkBrown,
                                        lineHeight = 21.sp
                                    ),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { inner ->
                                        if (text.isEmpty()) {
                                            Text(
                                                "输入人生中的这一刻…",
                                                fontSize = 16.sp,
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
                        label = "日期",
                        onClick = {
                            editingMyBirthday = false
                            editingOwnerAnchor = null
                            calendarForAnchor = false
                            showCalendar = true
                        }
                    ) {
                        val d = DateUtils.millisToDate(millis)
                        Text(
                            "${DateUtils.formatCN(d)} ${DateUtils.weekdayName(d)}",
                            fontSize = 14.sp,
                            color = AppColors.InkBrownSoft
                        )
                        Spacer(Modifier.width(6.dp))
                        SheetChevron()
                    }
                }

                // ④ 归属
                RowGroup {
                    SheetRow(icon = Icons.Filled.AccountCircle, label = "归属") {
                        Box(modifier = Modifier.weight(1f)) {
                            BasicTextField(
                                value = ownerName,
                                onValueChange = {
                                    ownerName = it
                                    matchedOwner = owners.find { o -> o.name == it.trim() }
                                    matchedOwner?.let { o ->
                                        isPerson = o.isPerson
                                        colorKey = o.color
                                    }
                                },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 14.sp, color = AppColors.InkBrown),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { inner ->
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        if (ownerName.isEmpty()) {
                                            Text(
                                                "留空为「我」",
                                                fontSize = 14.sp,
                                                color = AppColors.TextHint,
                                                textAlign = TextAlign.End,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                        inner()
                                    }
                                }
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        InlineChooser(
                            options = ownerOptions,
                            selectedIndex = ownerIndex,
                            panelWidth = 240.dp,
                            onSelect = { index ->
                                if (index == 0) {
                                    ownerName = ""
                                    matchedOwner = null
                                } else {
                                    val o = owners[index - 1]
                                    ownerName = o.name
                                    matchedOwner = o
                                    isPerson = o.isPerson
                                    anchorMillis = o.anchorMillis
                                    // v2.0：选中已有归属 → 颜色栏载入该归属的统一颜色
                                    colorKey = o.color
                                }
                            }
                        ) {
                            PillSurface(horizontalPadding = 14.dp, verticalPadding = 7.dp) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("选择", fontSize = 13.sp, color = AppColors.InkBrown)
                                    Icon(
                                        Icons.Filled.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = AppColors.InkBrownSoft,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ⑤ 起始日 / 生日：每一项编辑时都显示，整行点击即可直接更改
                //    （v2.0 取消「修改日期」按钮）
                when {
                    // 新建归属：类型 + 锚点日期
                    needOwnerSetup -> {
                        RowGroup {
                            SheetRow(icon = Icons.Filled.Person, label = "类型") {
                                OwnerTypeToggle(isPerson) { isPerson = it }
                            }
                        }
                        RowGroup(showDivider = false) {
                            SheetRow(
                                icon = Icons.Filled.CalendarMonth,
                                label = if (isPerson) "生日" else "起始",
                                onClick = {
                                    editingMyBirthday = false
                                    editingOwnerAnchor = null
                                    calendarForAnchor = true
                                    showCalendar = true
                                }
                            ) {
                                Text(
                                    DateUtils.formatCN(DateUtils.millisToDate(anchorMillis)),
                                    fontSize = 14.sp,
                                    color = AppColors.Accent
                                )
                                Spacer(Modifier.width(6.dp))
                                SheetChevron()
                            }
                        }
                    }
                    // 已有归属：显示其生日/起始日，点击整行直接修改
                    matchedOwner != null -> {
                        RowGroup(showDivider = false) {
                            val o = matchedOwner!!
                            SheetRow(
                                icon = Icons.Filled.CalendarMonth,
                                label = "${o.name}的${if (o.isPerson) "生日" else "起始"}",
                                onClick = {
                                    editingMyBirthday = false
                                    editingOwnerAnchor = o
                                    calendarForAnchor = true
                                    showCalendar = true
                                }
                            ) {
                                Text(
                                    DateUtils.formatCN(DateUtils.millisToDate(o.anchorMillis)),
                                    fontSize = 14.sp,
                                    color = AppColors.Accent
                                )
                                Spacer(Modifier.width(6.dp))
                                SheetChevron()
                            }
                        }
                    }
                    // 归属为「我」：显示我的生日，点击可直接更改（全局）
                    else -> {
                        RowGroup(showDivider = false) {
                            SheetRow(
                                icon = Icons.Filled.CalendarMonth,
                                label = "我的生日",
                                onClick = {
                                    editingOwnerAnchor = null
                                    editingMyBirthday = true
                                    calendarForAnchor = true
                                    showCalendar = true
                                }
                            ) {
                                val b = data.birthdayMillis
                                Text(
                                    if (b != null) DateUtils.formatCN(DateUtils.millisToDate(b)) else "未设置",
                                    fontSize = 14.sp,
                                    color = if (b != null) AppColors.Accent else AppColors.TextHint
                                )
                                Spacer(Modifier.width(6.dp))
                                SheetChevron()
                            }
                        }
                    }
                }

                // ⑥ 颜色（v2.0：栏右侧同步显示当前所选颜色；列表中「谁几岁 /
                //    事项第几年」徽标的底色即此处所选颜色，同一归属下保持统一）
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
                                        ambientColor = DotShadowNear,
                                        spotColor = DotShadowFar
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
                                            ambientColor = DotShadowNear,
                                            spotColor = DotShadowFar
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

    if (showCalendar) {
        CalendarSheet(
            initialMillis = when {
                !calendarForAnchor -> millis
                editingMyBirthday -> data.birthdayMillis
                editingOwnerAnchor != null -> editingOwnerAnchor!!.anchorMillis
                else -> anchorMillis
            },
            initialLunar = false,
            onDismiss = { showCalendar = false },
            onConfirm = { m, _ ->
                if (calendarForAnchor) {
                    val target = editingOwnerAnchor
                    when {
                        // 修改「我的生日」（全局）
                        editingMyBirthday -> {
                            AppRepo.setBirthday(m, false)
                            editingMyBirthday = false
                        }
                        // 修改已有归属的生日/起始日，立即保存
                        target != null -> {
                            val updated = target.copy(anchorMillis = m)
                            AppRepo.upsertOwner(updated)
                            matchedOwner = updated
                            editingOwnerAnchor = null
                        }
                        // 新建归属的锚点日期
                        else -> anchorMillis = m
                    }
                } else {
                    millis = m
                }
                showCalendar = false
            }
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
        Spacer(Modifier.width(12.dp))
        trailing()
    }
}

@Composable
private fun OwnerTypeToggle(isPerson: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .gradientShadow(RoundedCornerShape(12.dp), base = 2.dp, layers = 2)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.SheetField)
            .padding(3.dp)
    ) {
        listOf(true to "人", false to "事件").forEach { (value, label) ->
            val active = isPerson == value
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (active) Color.White else Color.Transparent)
                    .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onChange(value) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
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
