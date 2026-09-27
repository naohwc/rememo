package com.noteremind.app.ui.components

import androidx.compose.ui.graphics.Color

/**
 * 条目配色（v1.4）：
 * 相比 v1.3 的莫兰迪淡彩，提高了饱和度与明度差，
 * 让 12 个颜色在色板里一眼可辨（相邻色不再糊成一片）。
 */
object MorandiPalette {

    data class Entry(val key: String, val name: String, val color: Color)

    val all: List<Entry> = listOf(
        Entry("m1", "松绿", Color(0xFF3F8F63)),
        Entry("m2", "草绿", Color(0xFF8CBF4E)),
        Entry("m3", "橄榄", Color(0xFFBFA22E)),
        Entry("m4", "芥黄", Color(0xFFE8B93B)),
        Entry("m5", "杏橙", Color(0xFFE8863C)),
        Entry("m6", "豆沙", Color(0xFFC9524A)),
        Entry("m7", "樱粉", Color(0xFFE87A9B)),
        Entry("m8", "藕紫", Color(0xFFB061C0)),
        Entry("m9", "雾紫", Color(0xFF6F6FD0)),
        Entry("m10", "雾蓝", Color(0xFF3F86CE)),
        Entry("m11", "青碧", Color(0xFF2FA3A3)),
        Entry("m12", "暖灰", Color(0xFF8C8375))
    )

    private val map = all.associateBy { it.key }

    fun byKey(key: String): Color = map[key]?.color ?: all[0].color
}
