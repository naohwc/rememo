package com.noteremind.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
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

/** v1.6：行内上下间距与输入界面统一（15dp 的 80%）；v1.7 起各行等距、内容贴上部 */
private val ROW_V_PAD = 12.dp

private val PillShape = RoundedCornerShape(percent = 50)

/**
 * 人生轨迹输入（v1.6）：
 * - 整页灰面板 + 白胶囊按钮 + 前置矢量图标平铺行
 * - 文字 + 日期 + 归属（是谁的人生轨迹）；新建归属时可设类型与锚点日期
 * - 归属的「选择」与重复单位一样，改为胶囊按钮浮层展开
 */
@Composable
fun LifeEventSheet(
    initial: LifeEvent?,
    onDismiss: () -> Unit,
    onSave: (String, Long, String) -> Unit
) {
    val today = LocalDate.now()
    val data by AppRepo.data.collectAsState()
    val owners = data.owners

    var text by remember { mutableStateOf(initial?.text ?: "") }
    var millis by remember { mutableStateOf(initial?.dateMillis ?: DateUtils.dateToMillis(today)) }
    var showCalendar by remember { mutableStateOf(false) }
    var calendarForAnchor by remember { mutableStateOf(false) }

    val initialOwner = initial?.ownerId?.let { id -> owners.find { it.id == id } }
    var ownerName by remember { mutableStateOf(initialOwner?.name ?: "") }
    var matchedOwner by remember { mutableStateOf<TrackOwner?>(initialOwner) }
    var isPerson by remember { mutableStateOf(initialOwner?.isPerson ?: true) }
    var anchorMillis by remember {
        mutableStateOf(initialOwner?.anchorMillis ?: (initial?.dateMillis ?: DateUtils.dateToMillis(today)))
    }
    // v1.8：正在修改生日/起始日的已有归属（null = 新建归属的锚点日期）
    var editingOwnerAnchor by remember { mutableStateOf<TrackOwner?>(null) }

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
                    .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                        anchorMillis = anchorMillis
                                    )
                                )
                                id
                            }
                        }
                        onSave(text.trim(), millis, ownerId)
                        close()
                    }
                })
            }

            // 内容：紧贴页面上部依次排列，各行间距一致（下方留空，不再整页均分）
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.Top
            ) {
                // ① 这一刻：胶囊输入框
                RowGroup {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = ROW_V_PAD)
                            .clip(PillShape)
                            .background(AppColors.SheetField)
                            .padding(horizontal = ROW_PAD, vertical = 13.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SheetLeadingIcon(Icons.Filled.Edit)
                            BasicTextField(
                                value = text,
                                onValueChange = { text = it },
                                textStyle = TextStyle(fontSize = 16.sp, color = AppColors.InkBrown, lineHeight = 21.sp),
                                modifier = Modifier.fillMaxWidth(),
                                decorationBox = { inner ->
                                    if (text.isEmpty()) {
                                        Text("输入人生中的这一刻…", fontSize = 16.sp, color = AppColors.TextHint)
                                    }
                                    inner()
                                }
                            )
                        }
                    }
                }

                // ② 日期
                RowGroup {
                    SheetRow(
                        icon = Icons.Filled.CalendarMonth,
                        label = "日期",
                        onClick = {
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

                // ③ 归属
                RowGroup(showDivider = needOwnerSetup || matchedOwner != null) {
                    SheetRow(icon = Icons.Filled.AccountCircle, label = "归属") {
                        Box(modifier = Modifier.weight(1f)) {
                            BasicTextField(
                                value = ownerName,
                                onValueChange = {
                                    ownerName = it
                                    matchedOwner = owners.find { o -> o.name == it.trim() }
                                    matchedOwner?.let { o -> isPerson = o.isPerson }
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

                // ④ 新建归属：类型 + 锚点日期
                if (needOwnerSetup) {
                    RowGroup {
                        SheetRow(icon = Icons.Filled.Person, label = "类型") {
                            OwnerTypeToggle(isPerson) { isPerson = it }
                        }
                    }
                    RowGroup(showDivider = false) {
                        SheetRow(
                            icon = Icons.Filled.CalendarMonth,
                            label = if (isPerson) "生日" else "起始"
                        ) {
                            Text(
                                DateUtils.formatCN(DateUtils.millisToDate(anchorMillis)),
                                fontSize = 14.sp,
                                color = AppColors.Accent
                            )
                            Spacer(Modifier.width(10.dp))
                            // v1.8：修改日期按钮，弹出日期选择界面
                            SheetPillButton("修改日期", compact = true, onClick = {
                                editingOwnerAnchor = null
                                calendarForAnchor = true
                                showCalendar = true
                            })
                        }
                    }
                } else if (matchedOwner != null) {
                    // 已有归属（v1.8）：显示其生日/起始日，可用「修改日期」按钮直接修改
                    RowGroup(showDivider = false) {
                        val o = matchedOwner!!
                        SheetRow(
                            icon = Icons.Filled.CalendarMonth,
                            label = "${o.name}的${if (o.isPerson) "生日" else "起始"}"
                        ) {
                            Text(
                                DateUtils.formatCN(DateUtils.millisToDate(o.anchorMillis)),
                                fontSize = 14.sp,
                                color = AppColors.Accent
                            )
                            Spacer(Modifier.width(10.dp))
                            SheetPillButton("修改日期", compact = true, onClick = {
                                editingOwnerAnchor = o
                                calendarForAnchor = true
                                showCalendar = true
                            })
                        }
                    }
                }
            }
        }
    }

    if (showCalendar) {
        CalendarSheet(
            initialMillis = if (calendarForAnchor) anchorMillis else millis,
            initialLunar = false,
            onDismiss = { showCalendar = false },
            onConfirm = { m, _ ->
                if (calendarForAnchor) {
                    val target = editingOwnerAnchor
                    if (target != null) {
                        // v1.8：修改已有归属的生日/起始日，立即保存
                        val updated = target.copy(anchorMillis = m)
                        AppRepo.upsertOwner(updated)
                        matchedOwner = updated
                        editingOwnerAnchor = null
                    } else {
                        anchorMillis = m
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
            .padding(horizontal = ROW_PAD, vertical = ROW_V_PAD),
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
