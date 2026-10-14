package com.noteremind.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noteremind.app.data.Anniversary
import com.noteremind.app.data.AppRepo
import com.noteremind.app.data.LifeEvent
import com.noteremind.app.data.ReminderItem
import com.noteremind.app.ui.components.CalendarSheet
import com.noteremind.app.ui.components.AppBackdrop
import com.noteremind.app.ui.components.EditorResult
import com.noteremind.app.ui.components.EditorSheet
import com.noteremind.app.ui.components.LifeEventSheet
import com.noteremind.app.ui.components.fabShadow
import com.noteremind.app.ui.components.barShadow
import com.noteremind.app.ui.components.barSurface
import com.noteremind.app.ui.components.gradientShadow
import com.noteremind.app.ui.screens.AnniversaryScreen
import com.noteremind.app.ui.screens.DontForgetScreen
import com.noteremind.app.ui.screens.LifeTrackScreen
import com.noteremind.app.ui.screens.SettingsScreen
import com.noteremind.app.ui.theme.AppColors
import com.noteremind.app.util.SystemCalendarHelper

private const val TAB_REMINDER = 0
private const val TAB_LIFE = 1
private const val TAB_ANNIVERSARY = 2
private const val TAB_SETTINGS = 3

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val data by AppRepo.data.collectAsState()

    var tab by rememberSaveable { mutableIntStateOf(TAB_REMINDER) }
    var editorKind by remember { mutableIntStateOf(-1) }
    var editingReminder by remember { mutableStateOf<ReminderItem?>(null) }
    var editingLife by remember { mutableStateOf<LifeEvent?>(null) }
    var editingAnniversary by remember { mutableStateOf<Anniversary?>(null) }
    var showBirthday by remember { mutableStateOf(false) }
    var pendingLifeEditor by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // 渐变底色（参考 底色.jpg：顶部淡蓝 → 近白）
        AppBackdrop(Modifier.fillMaxSize())

        // 页面内容
        Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            when (tab) {
                TAB_REMINDER -> DontForgetScreen(onEdit = { editingReminder = it; editorKind = TAB_REMINDER })
                TAB_LIFE -> LifeTrackScreen(onEdit = { editingLife = it; editorKind = TAB_LIFE })
                TAB_ANNIVERSARY -> AnniversaryScreen(onEdit = { editingAnniversary = it; editorKind = TAB_ANNIVERSARY })
                else -> SettingsScreen()
            }
        }

        // 底部毛玻璃导航
        GlassBottomBar(
            selected = tab,
            onSelect = { tab = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 14.dp)
                .navigationBarsPadding()
                .padding(bottom = 10.dp)
        )

        // 右下角加号（v1.9：黑底白图标）
        if (tab != TAB_SETTINGS) {
            BlackFab(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(end = 20.dp, bottom = 90.dp)
            ) {
                when (tab) {
                    TAB_REMINDER -> {
                        editingReminder = null
                        editorKind = TAB_REMINDER
                    }
                    TAB_LIFE -> {
                        if (data.birthdayMillis == null) {
                            pendingLifeEditor = true
                            showBirthday = true
                        } else {
                            editingLife = null
                            editorKind = TAB_LIFE
                        }
                    }
                    TAB_ANNIVERSARY -> {
                        editingAnniversary = null
                        editorKind = TAB_ANNIVERSARY
                    }
                }
            }
        }
    }

    // ---------- 各种弹出层 ----------

    if (editorKind == TAB_REMINDER) {
        val src = editingReminder
        EditorSheet(
            title = if (src == null) "添加记事" else "编辑记事",
            placeholder = "输入提醒事项…",
            initialTitle = src?.title ?: "",
            initialMillis = src?.dateMillis,
            initialLunar = src?.lunar ?: false,
            initialRepeatValue = src?.repeatValue ?: 0,
            initialRepeatUnit = src?.repeatUnit ?: 0,
            initialRemind = src?.useSystemCalendar ?: false,
            initialIcon = src?.icon ?: "bell",
            initialColor = src?.color ?: "m1",
            initialNote = src?.note ?: "",
            showRepeat = true,
            onDismiss = { editorKind = -1; editingReminder = null },
            onSave = { r: EditorResult ->
                val item = ReminderItem(
                    id = src?.id ?: AppRepo.newId(),
                    title = r.title,
                    dateMillis = r.millis,
                    lunar = r.lunar,
                    repeatValue = r.repeatValue,
                    repeatUnit = r.repeatUnit,
                    useSystemCalendar = r.remind,
                    icon = r.icon,
                    color = r.color,
                    note = r.note,
                    order = src?.order ?: ((data.reminders.maxOfOrNull { it.order } ?: -1) + 1),
                    createdAt = src?.createdAt ?: System.currentTimeMillis()
                )
                AppRepo.upsertReminder(item)
                editorKind = -1
                editingReminder = null
                if (r.remind) {
                    SystemCalendarHelper.insert(context, r.title, r.millis, r.repeatValue, r.repeatUnit)
                }
            }
        )
    }

    if (editorKind == TAB_ANNIVERSARY) {
        val src = editingAnniversary
        EditorSheet(
            title = if (src == null) "添加纪念日" else "编辑纪念日",
            placeholder = "输入值得纪念的日子…",
            initialTitle = src?.title ?: "",
            initialMillis = src?.dateMillis,
            initialLunar = src?.lunar ?: false,
            initialRemind = src?.remind ?: false,
            initialIcon = src?.icon ?: "cake",
            initialColor = src?.color ?: "m1",
            initialNote = src?.note ?: "",
            showRepeat = false,
            showColor = true,
            dateHint = "日期",
            onDismiss = { editorKind = -1; editingAnniversary = null },
            onSave = { r: EditorResult ->
                val item = Anniversary(
                    id = src?.id ?: AppRepo.newId(),
                    title = r.title,
                    dateMillis = r.millis,
                    lunar = r.lunar,
                    icon = r.icon,
                    color = r.color,
                    note = r.note,
                    remind = r.remind,
                    createdAt = src?.createdAt ?: System.currentTimeMillis()
                )
                AppRepo.upsertAnniversary(item)
                editorKind = -1
                editingAnniversary = null
                if (r.remind) {
                    // 纪念日按年重复写入系统日历
                    SystemCalendarHelper.insert(context, r.title, r.millis, 1, 3)
                }
            }
        )
    }

    if (editorKind == TAB_LIFE) {
        val src = editingLife
        LifeEventSheet(
            initial = src,
            onDismiss = { editorKind = -1; editingLife = null },
            onSave = { text, millis, ownerId, colorKey, note ->
                if (src == null) {
                    AppRepo.addLifeEvent(
                        LifeEvent(
                            id = AppRepo.newId(),
                            text = text,
                            dateMillis = millis,
                            ownerId = ownerId,
                            color = colorKey,
                            note = note,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    AppRepo.updateLifeEvent(
                        src.copy(
                            text = text,
                            dateMillis = millis,
                            ownerId = ownerId,
                            color = colorKey,
                            note = note
                        )
                    )
                }
                editorKind = -1
                editingLife = null
            }
        )
    }

    if (showBirthday) {
        CalendarSheet(
            initialMillis = data.birthdayMillis,
            initialLunar = data.birthdayLunar,
            onDismiss = { showBirthday = false; pendingLifeEditor = false },
            onConfirm = { millis, lunar ->
                AppRepo.setBirthday(millis, lunar)
                showBirthday = false
                if (pendingLifeEditor) {
                    pendingLifeEditor = false
                    editingLife = null
                    editorKind = TAB_LIFE
                }
            }
        )
    }
}

@Composable
private fun GlassBottomBar(
    selected: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        "别忘了" to Icons.Filled.Notifications,
        "人生轨迹" to Icons.Filled.Timeline,
        "纪念日" to Icons.Filled.Cake,
        "设置" to Icons.Filled.Settings
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .barShadow(RoundedCornerShape(28.dp))
            .barSurface(RoundedCornerShape(28.dp))
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, pair ->
            NavItem(
                label = pair.first,
                image = pair.second,
                active = index == selected,
                onClick = { onSelect(index) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * 单个导航项（参考 导航栏.jpg）：
 * - 选中：白色凸起胶囊底 + 柔和阴影，图标文字绿色
 * - 未选中：近黑图标文字
 * - 点击时背景放大→缩小→回正（duangduang 的弹性），并伴随一圈水波纹扩散
 */
@Composable
private fun NavItem(
    label: String,
    image: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var bumpKey by remember { mutableIntStateOf(0) }
    val scale = remember { Animatable(1f) }
    val ripple = remember { Animatable(0f) }
    val shape = RoundedCornerShape(24.dp)

    LaunchedEffect(bumpKey) {
        if (bumpKey == 0) return@LaunchedEffect
        scale.snapTo(0.80f)
        scale.animateTo(
            targetValue = 1f,
            animationSpec = keyframes {
                durationMillis = 560
                0.80f at 0
                1.18f at 150
                0.92f at 290
                1.06f at 400
                1.0f at 560
            }
        )
    }
    LaunchedEffect(bumpKey) {
        if (bumpKey == 0) return@LaunchedEffect
        ripple.snapTo(0f)
        ripple.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 560, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
            // v1.8：选中态改用外渐变半透阴影，与其他按钮统一
            .gradientShadow(
                shape,
                base = if (active) 2.dp else 0.dp,
                layers = if (active) 2 else 0
            )
            .clip(shape)
            .background(
                if (active) {
                    // 半透明玻璃底：比导航栏底色深一点，能透出下方内容
                    Brush.verticalGradient(
                        listOf(
                            AppColors.BarActiveGlass.copy(alpha = 0.62f),
                            AppColors.BarActiveGlass.copy(alpha = 0.40f)
                        )
                    )
                } else {
                    Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
                }
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                bumpKey++
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        // 水波纹：从中心扩散并淡出
        Canvas(modifier = Modifier.matchParentSize()) {
            val p = ripple.value
            if (p > 0f && p < 1f) {
                drawCircle(
                    color = AppColors.Accent,
                    radius = size.minDimension * 1.05f * p,
                    center = Offset(size.width / 2f, size.height / 2f),
                    alpha = (1f - p) * 0.30f
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                image,
                contentDescription = null,
                tint = if (active) AppColors.Accent else AppColors.BarIdleInk,
                modifier = Modifier.size(20.dp)
            )
            // v1.6：图标与文字间距缩小、导航栏整体高度略缩小
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                label,
                fontSize = 11.sp,
                lineHeight = 12.sp,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Medium,
                color = if (active) AppColors.Accent else AppColors.BarIdleInk
            )
        }
    }
}

/**
 * 右下角加号键（v1.9：黑底白图标 + 双层柔和投影，参考 xiaozhi-android-client 的 FAB）
 */
@Composable
private fun BlackFab(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(58.dp)
            .fabShadow(CircleShape)
            .clip(CircleShape)
            .background(AppColors.BarInk)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Filled.Add,
            contentDescription = "添加",
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}
