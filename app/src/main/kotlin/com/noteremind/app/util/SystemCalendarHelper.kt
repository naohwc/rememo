package com.noteremind.app.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast

/**
 * 跳转系统日历并建立日程（标题为提醒事项内容，可带重复规则）。
 * 使用 ACTION_INSERT，由系统日历应用完成写入，无需日历读写权限。
 */
object SystemCalendarHelper {

    fun insert(
        context: Context,
        title: String,
        millis: Long,
        repeatValue: Int,
        repeatUnit: Int
    ) {
        val intent = Intent(Intent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, title)
            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, millis)
            .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true)

        if (repeatValue > 0) {
            val freq = when (repeatUnit) {
                DateUtils.UNIT_DAY -> "DAILY"
                DateUtils.UNIT_WEEK -> "WEEKLY"
                DateUtils.UNIT_MONTH -> "MONTHLY"
                else -> "YEARLY"
            }
            intent.putExtra(CalendarContract.Events.RRULE, "FREQ=$freq;INTERVAL=$repeatValue")
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        runCatching {
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(context, "未找到可用的日历应用", Toast.LENGTH_SHORT).show()
        }
    }
}
