package com.noteremind.app.util

import java.time.LocalDate

/**
 * 1900-2100 年 公历(阳历) <-> 农历(阴历) 互转
 *
 * 经典算法：以 lunarInfo 表为基础，1900-01-31 对应农历 1900 年正月初一。
 */
object LunarCalendar {

    const val MIN_YEAR = 1900
    const val MAX_YEAR = 2100

    /** 每年农历信息：0-3 位为闰月月份(0 表示无闰月)，4-15 位为 1-12 月大小(30天=1)，16 位为闰月大小 */
    private val LUNAR_INFO = longArrayOf(
        0x04bd8L, 0x04ae0L, 0x0a570L, 0x054d5L, 0x0d260L, 0x0d950L, 0x16554L, 0x056a0L, 0x09ad0L, 0x055d2L, // 1900-1909
        0x04ae0L, 0x0a5b6L, 0x0a4d0L, 0x0d250L, 0x1d255L, 0x0b540L, 0x0d6a0L, 0x0ada2L, 0x095b0L, 0x14977L, // 1910-1919
        0x04970L, 0x0a4b0L, 0x0b4b5L, 0x06a50L, 0x06d40L, 0x1ab54L, 0x02b60L, 0x09570L, 0x052f2L, 0x04970L, // 1920-1929
        0x06566L, 0x0d4a0L, 0x0ea50L, 0x06e95L, 0x05ad0L, 0x02b60L, 0x186e3L, 0x092e0L, 0x1c8d7L, 0x0c950L, // 1930-1939
        0x0d4a0L, 0x1d8a6L, 0x0b550L, 0x056a0L, 0x1a5b4L, 0x025d0L, 0x092d0L, 0x0d2b2L, 0x0a950L, 0x0b557L, // 1940-1949
        0x06ca0L, 0x0b550L, 0x15355L, 0x04da0L, 0x0a5b0L, 0x14573L, 0x052b0L, 0x0a9a8L, 0x0e950L, 0x06aa0L, // 1950-1959
        0x0aea6L, 0x0ab50L, 0x04b60L, 0x0aae4L, 0x0a570L, 0x05260L, 0x0f263L, 0x0d950L, 0x05b57L, 0x056a0L, // 1960-1969
        0x096d0L, 0x04dd5L, 0x04ad0L, 0x0a4d0L, 0x0d4d4L, 0x0d250L, 0x0d558L, 0x0b540L, 0x0b6a0L, 0x195a6L, // 1970-1979
        0x095b0L, 0x049b0L, 0x0a974L, 0x0a4b0L, 0x0b27aL, 0x06a50L, 0x06d40L, 0x0af46L, 0x0ab60L, 0x09570L, // 1980-1989
        0x04af5L, 0x04970L, 0x064b0L, 0x074a3L, 0x0ea50L, 0x06b58L, 0x055c0L, 0x0ab60L, 0x096d5L, 0x092e0L, // 1990-1999
        0x0c960L, 0x0d954L, 0x0d4a0L, 0x0da50L, 0x07552L, 0x056a0L, 0x0abb7L, 0x025d0L, 0x092d0L, 0x0cab5L, // 2000-2009
        0x0a950L, 0x0b4a0L, 0x0baa4L, 0x0ad50L, 0x055d9L, 0x04ba0L, 0x0a5b0L, 0x15176L, 0x052b0L, 0x0a930L, // 2010-2019
        0x07954L, 0x06aa0L, 0x0ad50L, 0x05b52L, 0x04b60L, 0x0a6e6L, 0x0a4e0L, 0x0d260L, 0x0ea65L, 0x0d530L, // 2020-2029
        0x05aa0L, 0x076a3L, 0x096d0L, 0x04afbL, 0x04ad0L, 0x0a4d0L, 0x1d0b6L, 0x0d250L, 0x0d520L, 0x0dd45L, // 2030-2039
        0x0b5a0L, 0x056d0L, 0x055b2L, 0x049b0L, 0x0a577L, 0x0a4b0L, 0x0aa50L, 0x1b255L, 0x06d20L, 0x0ada0L, // 2040-2049
        0x14b63L, 0x09370L, 0x049f8L, 0x04970L, 0x064b0L, 0x168a6L, 0x0ea50L, 0x06b20L, 0x1a6c4L, 0x0aae0L, // 2050-2059
        0x0a2e0L, 0x0d2e3L, 0x0c960L, 0x0d557L, 0x0d4a0L, 0x0da50L, 0x05d55L, 0x056a0L, 0x0a6d0L, 0x055d4L, // 2060-2069
        0x052d0L, 0x0a9b8L, 0x0a950L, 0x0b4a0L, 0x0b6a6L, 0x0ad50L, 0x055a0L, 0x0aba4L, 0x0a5b0L, 0x052b0L, // 2070-2079
        0x0b273L, 0x06930L, 0x07337L, 0x06aa0L, 0x0ad50L, 0x14b55L, 0x04b60L, 0x0a570L, 0x054e4L, 0x0d160L, // 2080-2089
        0x0e968L, 0x0d520L, 0x0daa0L, 0x16aa6L, 0x056d0L, 0x04ae0L, 0x0a9d4L, 0x0a2d0L, 0x0d150L, 0x0f252L, // 2090-2099
        0x0d520L  // 2100
    )

    private val BASE_DATE = LocalDate.of(1900, 1, 31) // 农历 1900 年正月初一

    /** 农历日期 */
    data class LunarDate(
        val year: Int,
        val month: Int,
        val day: Int,
        val isLeap: Boolean
    )

    private fun info(year: Int): Long {
        val idx = (year - MIN_YEAR).coerceIn(0, LUNAR_INFO.size - 1)
        return LUNAR_INFO[idx]
    }

    /** 该年闰哪个月，0 表示不闰 */
    fun leapMonth(year: Int): Int = (info(year) and 0xF).toInt()

    /** 闰月天数：29 或 30 */
    fun leapDays(year: Int): Int {
        return if (leapMonth(year) == 0) 0 else if (info(year) and 0x10000L != 0L) 30 else 29
    }

    /** 农历某月天数：29 或 30 */
    fun monthDays(year: Int, month: Int): Int {
        return if (info(year) and (0x10000 shr month).toLong() != 0L) 30 else 29
    }

    /** 农历一年的总天数 */
    fun yearDays(year: Int): Int {
        var sum = 348
        var i = 0x8000
        while (i > 0x8) {
            if (info(year) and i.toLong() != 0L) sum++
            i = i shr 1
        }
        return sum + leapDays(year)
    }

    /** 公历 -> 农历 */
    fun solarToLunar(date: LocalDate): LunarDate {
        var offset = (date.toEpochDay() - BASE_DATE.toEpochDay()).toInt()
        if (offset < 0) return LunarDate(MIN_YEAR, 1, 1, false)

        var year = MIN_YEAR
        var temp: Int
        while (year <= MAX_YEAR) {
            temp = yearDays(year)
            if (offset < temp) break
            offset -= temp
            year++
        }
        if (year > MAX_YEAR) year = MAX_YEAR

        val leap = leapMonth(year)
        var isLeap = false
        temp = 0
        var i = 1
        while (i < 13 && offset > 0) {
            if (leap > 0 && i == leap + 1 && !isLeap) {
                i--
                isLeap = true
                temp = leapDays(year)
            } else {
                temp = monthDays(year, i)
            }
            if (isLeap && i == leap + 1) isLeap = false
            offset -= temp
            i++
        }
        if (offset == 0 && leap > 0 && i == leap + 1) {
            if (isLeap) {
                isLeap = false
            } else {
                isLeap = true
                i--
            }
        }
        if (offset < 0) {
            offset += temp
            i--
        }
        val month = i.coerceIn(1, 12)
        return LunarDate(year, month, offset + 1, isLeap)
    }

    /** 农历 -> 公历（day 超出该月天数时自动取该月最后一天） */
    fun lunarToSolar(year: Int, month: Int, day: Int, isLeap: Boolean): LocalDate {
        val y = year.coerceIn(MIN_YEAR, MAX_YEAR)
        val m = month.coerceIn(1, 12)
        var offset = 0
        for (i in MIN_YEAR until y) offset += yearDays(i)
        val leap = leapMonth(y)
        for (mm in 1 until m) {
            offset += monthDays(y, mm)
            if (leap > 0 && mm == leap) offset += leapDays(y)
        }
        // 闰月排在对应正常月之后
        if (isLeap && leap == m) offset += monthDays(y, m)

        // 闰月的天数上限要用闰月天数
        val maxDay = if (isLeap && leap == m) leapDays(y) else monthDays(y, m)
        val d = day.coerceIn(1, maxDay)
        return BASE_DATE.plusDays((offset + d - 1).toLong())
    }

    fun lunarToSolar(ld: LunarDate): LocalDate = lunarToSolar(ld.year, ld.month, ld.day, ld.isLeap)

    // ---------- 中文名称 ----------

    private val CN_NUM = arrayOf("零", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十")
    private val MONTH_NAMES = arrayOf("", "正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊")
    private val DAY_NAMES = arrayOf(
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )

    fun dayName(day: Int): String = DAY_NAMES.getOrElse(day - 1) { "初一" }

    fun monthName(month: Int, isLeap: Boolean): String =
        (if (isLeap) "闰" else "") + MONTH_NAMES.getOrElse(month) { "" } + "月"

    /** 例：农历八月初三 / 农历闰六月初三 */
    fun lunarText(date: LocalDate): String {
        val ld = solarToLunar(date)
        return "农历" + monthName(ld.month, ld.isLeap) + dayName(ld.day)
    }

    fun lunarShortText(date: LocalDate): String {
        val ld = solarToLunar(date)
        return monthName(ld.month, ld.isLeap) + dayName(ld.day)
    }

    /** 农历年份中文 */
    fun yearName(year: Int): String {
        val s = year.toString()
        val sb = StringBuilder()
        for (ch in s) {
            sb.append(CN_NUM[ch - '0'])
        }
        return sb.toString()
    }

    /** 生肖 */
    fun zodiac(year: Int): String {
        val animals = arrayOf("鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪")
        return animals[(year - 1900) % 12]
    }

    /** 指定农历年月的天数 */
    fun daysInLunarMonth(year: Int, month: Int, isLeap: Boolean): Int {
        return if (isLeap && leapMonth(year) == month) leapDays(year) else monthDays(year, month)
    }

    /** 上一个月（自动处理闰月） */
    fun prevMonth(ld: LunarDate): LunarDate {
        return if (ld.isLeap) {
            LunarDate(ld.year, ld.month, ld.day, false)
        } else if (ld.month == 1) {
            val y = ld.year - 1
            val lm = leapMonth(y)
            LunarDate(y, 12, ld.day, lm == 12)
        } else {
            val m = ld.month - 1
            LunarDate(ld.year, m, ld.day, leapMonth(ld.year) == m)
        }
    }

    /** 下一个月（自动处理闰月） */
    fun nextMonth(ld: LunarDate): LunarDate {
        val lm = leapMonth(ld.year)
        return if (!ld.isLeap && lm == ld.month) {
            LunarDate(ld.year, ld.month, ld.day, true)
        } else if (ld.month == 12) {
            LunarDate(ld.year + 1, 1, ld.day, false)
        } else {
            LunarDate(ld.year, ld.month + 1, ld.day, false)
        }
    }
}
