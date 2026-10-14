package com.noteremind.app.data

import kotlinx.serialization.Serializable

/** 「别忘了」提醒事项 */
@Serializable
data class ReminderItem(
    val id: String,
    val title: String,
    /** 阳历时间戳（当天 0 点） */
    val dateMillis: Long,
    /** 是否按阴历录入 */
    val lunar: Boolean = false,
    /** 重复间隔，0 表示不重复 */
    val repeatValue: Int = 0,
    /** 0天 1周 2月 3年 */
    val repeatUnit: Int = 0,
    /** 是否写入系统日历 */
    val useSystemCalendar: Boolean = false,
    /** 图标名，见 IconSet */
    val icon: String = "star",
    /** 莫兰迪色 key，见 MorandiPalette */
    val color: String = "m1",
    val order: Int = 0,
    val createdAt: Long = 0L
)

/**
 * 人生轨迹的归属：一个人（锚点=生日）或一件事（锚点=起始日期）。
 */
@Serializable
data class TrackOwner(
    val id: String,
    val name: String,
    /** true = 人（显示 N 岁）；false = 事件（显示 第 N 年） */
    val isPerson: Boolean = true,
    /** 锚点日期：人的生日 / 事件的起始日 */
    val anchorMillis: Long
)

/** 「人生轨迹」事件 */
@Serializable
data class LifeEvent(
    val id: String,
    val text: String,
    val dateMillis: Long,
    /** 归属的 TrackOwner.id；空串表示「我」（用全局生日算年龄） */
    val ownerId: String = "",
    /** v1.9：条目颜色 key，见 MorandiPalette（年龄/第几年徽标底色取此色） */
    val color: String = "m1",
    val createdAt: Long = 0L
)

/** 「纪念日」 */
@Serializable
data class Anniversary(
    val id: String,
    val title: String,
    val dateMillis: Long,
    val lunar: Boolean = false,
    val icon: String = "cake",
    /** v1.6：条目颜色 key，见 MorandiPalette（图标随之着色） */
    val color: String = "m1",
    val remind: Boolean = false,
    val createdAt: Long = 0L
)

/** 全量数据（备份/恢复的最小单元） */
@Serializable
data class AppData(
    val reminders: List<ReminderItem> = emptyList(),
    val lifeEvents: List<LifeEvent> = emptyList(),
    val anniversaries: List<Anniversary> = emptyList(),
    val birthdayMillis: Long? = null,
    val birthdayLunar: Boolean = false,
    /** 多人轨迹的归属列表 */
    val owners: List<TrackOwner> = emptyList()
)
