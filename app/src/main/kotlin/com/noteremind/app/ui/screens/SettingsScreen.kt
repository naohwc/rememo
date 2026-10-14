package com.noteremind.app.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.noteremind.app.data.AppRepo
import com.noteremind.app.ui.components.CalendarSheet
import com.noteremind.app.ui.components.creamCard
import com.noteremind.app.ui.theme.AppColors
import com.noteremind.app.util.DateUtils

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val data by AppRepo.data.collectAsState()

    var showBirthdayPicker by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            val ok = runCatching {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    os.write(AppRepo.exportJson().toByteArray(Charsets.UTF_8))
                }
            }.isSuccess
            toast(context, if (ok) "备份已保存" else "备份失败")
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val text = runCatching {
                context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            }.getOrNull()
            val ok = text?.let { AppRepo.importJson(it) } ?: false
            toast(context, if (ok) "恢复成功" else "恢复失败：文件格式不正确")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.BgNeutral)
    ) {
        Text(
            "设置",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AppColors.TextPrimary,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
        )

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .creamCard(RoundedCornerShape(18.dp))
        ) {
            // 我的生日
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { showBirthdayPicker = true }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("我的生日", fontSize = 16.sp, color = AppColors.TextPrimary)
                Spacer(Modifier.width(10.dp))
                Text(
                    if (data.birthdayMillis != null) {
                        val b = DateUtils.millisToDate(data.birthdayMillis!!)
                        "${b.year}年${b.monthValue}月${b.dayOfMonth}日" + if (data.birthdayLunar) "（阴历）" else ""
                    } else "未设置",
                    fontSize = 13.sp,
                    color = AppColors.TextSecondary
                )
                Spacer(Modifier.weight(1f))
                Text("设置", fontSize = 13.sp, color = AppColors.Accent)
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = AppColors.TextHint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = 16.dp)
                    .background(AppColors.Divider)
            )

            // 数据备份与恢复
            SettingsRow("数据备份与恢复") { showBackupDialog = true }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .padding(horizontal = 16.dp)
                    .background(AppColors.Divider)
            )

            SettingsRow("关于") { showAbout = true }
        }
    }

    if (showBirthdayPicker) {
        CalendarSheet(
            initialMillis = data.birthdayMillis,
            initialLunar = data.birthdayLunar,
            onDismiss = { showBirthdayPicker = false },
            onConfirm = { millis, lunar ->
                AppRepo.setBirthday(millis, lunar)
                showBirthdayPicker = false
                toast(context, "生日已设置")
            }
        )
    }

    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("数据备份与恢复", fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Text(
                    "备份会把「别忘了」「人生轨迹」「纪念日」的全部数据导出为一个 JSON 文件；恢复会用它覆盖当前数据。",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showBackupDialog = false
                    runCatching {
                        exportLauncher.launch("reminder_backup.json")
                    }.onFailure { toast(context, "无法打开文件管理器") }
                }) { Text("备份到文件", color = AppColors.Accent) }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBackupDialog = false
                    runCatching {
                        importLauncher.launch(arrayOf("application/json", "*/*"))
                    }.onFailure { toast(context, "无法打开文件管理器") }
                }) { Text("从文件恢复", color = AppColors.Accent) }
            }
        )
    }

    if (showAbout) {
        AlertDialog(
            onDismissRequest = { showAbout = false },
            title = { Text("关于", fontSize = 17.sp, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Text("提醒记事 v2.4.1", fontSize = 15.sp, color = AppColors.TextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "一个记录提醒、人生轨迹与纪念日的小工具。支持阴阳历日期、重复提醒、写入系统日历、条目备注与本地数据备份。备注不在列表显示，仅编辑时可见。",
                        fontSize = 13.sp,
                        color = AppColors.TextSecondary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAbout = false }) { Text("好的", color = AppColors.Accent) }
            }
        )
    }
}

@Composable
private fun SettingsRow(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, fontSize = 16.sp, color = AppColors.TextPrimary, modifier = Modifier.weight(1f))
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = AppColors.TextHint,
            modifier = Modifier.size(18.dp)
        )
    }
}

private fun toast(context: Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}
