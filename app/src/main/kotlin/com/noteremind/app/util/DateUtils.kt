package com.noteremind.app.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

object DateUtils {

    private val ZONE: ZoneId = ZoneId.systemDefault()

    const val UNIT_DAY = 0
    const val UNIT_WEEK = 1
    const val UNIT_MONTH = 2
    const val UNIT_YEAR = 3

    fun millisToDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(ZONE).toLocalDate()

    fun dateToMillis(date: LocalDate): Long =
        date.atStartOfDay(ZONE).toInstant().toEpochMilli()

    fun today(): LocalDate = LocalDate.now()

    fun todayMillis(): Long = dateToMillis(LocalDate.now())

    fun weekdayName(date: LocalDate): String {
        return when (date.dayOfWeek.value) {
            1 -> "星期一"
            2 -> "星期二"
            3 -> "星期三"
            4 -> "星期四"
            5 -> "星期五"
            6 -> "星期六"
            else -> "星期日"
        }
    }

    /** 2026年9月24日 */
    fun formatCN(date: LocalDate): String = "${date.year}年${date.monthValue}月${date.dayOfMonth}日"

    /** 2026-09-24 */
    fun formatDash(date: LocalDate): String =
        String.format("%04d-%02d-%02d", date.year, date.monthValue, date.dayOfMonth)

    /** 9月24日 */
    fun formatShort(date: LocalDate): String = "${date.monthValue}月${date.dayOfMonth}日"

    /** 顶部：今天是：2026年9月24日 星期四 */
    fun todayHeader(): String {
        val d = LocalDate.now()
        return "今天是：${formatCN(d)} ${weekdayName(d)}"
    }

    fun daysBetween(from: LocalDate, to: LocalDate): Long = ChronoUnit.DAYS.between(from, to)

    /** 灵活显示：3天 / 2个月5天 / 1年3个月 */
    fun flexDuration(days: Long): String {
        val d = if (days < 0) 0 else days
        return when {
            d == 0L -> "0天"
            d < 31 -> "${d}天"
            d < 365 -> {
                val m = (d / 30).toInt()
                val rest = (d % 30).toInt()
                if (rest == 0) "${m}个月" else "${m}个月${rest}天"
            }
            else -> {
                val y = (d / 365).toInt()
                val restDays = (d % 365).toInt()
                val m = restDays / 30
                if (m == 0) "${y}年" else "${y}年${m}个月"
            }
        }
    }

    /**
     * 大数字显示：返回 Pair(大数字文本, 单位后缀)
     * 小于一个月时返回 ("12","天")，否则返回 ("1年3个月","")
     */
    fun bigDuration(days: Long): Pair<String, String> {
        val d = if (days < 0) 0 else days
        return if (d < 31) Pair(d.toString(), "天") else Pair(flexDuration(d), "")
    }

    /** 剩余天数文案（v1.2：恒显天数，不自动换算年月） */
    fun remainText(remainDays: Long): String {
        return when {
            remainDays < 0 -> "已过期"
            remainDays == 0L -> "就是今天"
            else -> "剩余${remainDays}天"
        }
    }

    /**
     * 计算下一次到期（提醒/纪念日）日期。
     * @param originalMillis 原始日期（阳历时间戳）
     * @param lunar 原始日期是否按阴历录入
     * @param repeatValue 重复间隔，0 表示不重复（纪念日按每年循环）
     * @param repeatUnit 0天 1周 2月 3年
     */
    fun nextDate(
        originalMillis: Long,
        lunar: Boolean,
        repeatValue: Int,
        repeatUnit: Int,
        from: LocalDate = LocalDate.now()
    ): LocalDate {
        val origin = millisToDate(originalMillis)
        if (!origin.isBefore(from)) return origin

        if (!lunar) {
            if (repeatValue <= 0) return origin
            var d = origin
            var guard = 0
            while (d.isBefore(from) && guard++ < 20000) {
                d = when (repeatUnit) {
                    UNIT_DAY -> d.plusDays(repeatValue.toLong())
                    UNIT_WEEK -> d.plusWeeks(repeatValue.toLong())
                    UNIT_MONTH -> d.plusMonths(repeatValue.toLong())
                    else -> d.plusYears(repeatValue.toLong())
                }
            }
            return d
        }

        // 阴历：按天/周用公历步进；按月/年用农历月序号步进
        val l0 = LunarCalendar.solarToLunar(origin)
        if (repeatValue <= 0 || repeatUnit == UNIT_DAY || repeatUnit == UNIT_WEEK) {
            val stepDays = when {
                repeatValue <= 0 -> 365L * 5 // 不重复时按每年循环（纪念日场景）
                repeatUnit == UNIT_DAY -> repeatValue.toLong()
                else -> repeatValue * 7L
            }
            var d = origin
            var guard = 0
            while (d.isBefore(from) && guard++ < 20000) {
                d = if (repeatValue <= 0) nextLunarAnnual(l0, from) else d.plusDays(stepDays)
                if (repeatValue <= 0) break
            }
            return d
        }

        val step = if (repeatUnit == UNIT_MONTH) repeatValue else repeatValue * 12
        var index = l0.year * 12 + (l0.month - 1)
        var guard = 0
        while (guard++ < 4000) {
            val y = index / 12
            val m = (index % 12) + 1
            val day = l0.day.coerceAtMost(LunarCalendar.monthDays(y, m))
            val cand = LunarCalendar.lunarToSolar(y, m, day, false)
            if (!cand.isBefore(from)) return cand
            index += step
        }
        return origin
    }

    /** 阴历按年循环的下一个日期 */
    fun nextLunarAnnual(l0: LunarCalendar.LunarDate, from: LocalDate): LocalDate {
        var year = LunarCalendar.solarToLunar(from).year
        repeat(3) {
            val day = l0.day.coerceAtMost(LunarCalendar.monthDays(year, l0.month))
            val cand = LunarCalendar.lunarToSolar(year, l0.month, day, false)
            if (!cand.isBefore(from)) return cand
            year++
        }
        return LocalDate.now()
    }

    /** 阴历纪念日/生日在某一年的公历日期 */
    fun lunarAnnualInYear(originalMillis: Long, year: Int): LocalDate {
        val l0 = LunarCalendar.solarToLunar(millisToDate(originalMillis))
        val day = l0.day.coerceAtMost(LunarCalendar.monthDays(year, l0.month))
        return LunarCalendar.lunarToSolar(year, l0.month, day, false)
    }

    /** 距离下一次还有多少天（阴历按年循环） */
    fun daysUntilAnnual(
        originalMillis: Long,
        lunar: Boolean,
        from: LocalDate = LocalDate.now()
    ): Long {
        val origin = millisToDate(originalMillis)
        val next: LocalDate = if (!lunar) {
            var y = from.year
            repeat(3) {
                val cand = LocalDate.of(y, origin.monthValue, minOf(origin.dayOfMonth, LocalDate.of(y, origin.monthValue, 1).lengthOfMonth()))
                if (!cand.isBefore(from)) return daysBetween(from, cand)
                y++
            }
            from
        } else {
            val l0 = LunarCalendar.solarToLunar(origin)
            var y = LunarCalendar.solarToLunar(from).year
            repeat(3) {
                val day = l0.day.coerceAtMost(LunarCalendar.monthDays(y, l0.month))
                val cand = LunarCalendar.lunarToSolar(y, l0.month, day, false)
                if (!cand.isBefore(from)) return daysBetween(from, cand)
                y++
            }
            from
        }
        return daysBetween(from, next)
    }

    /** 计算某天时的年龄（按公历生日） */
    fun ageAt(birthdayMillis: Long?, date: LocalDate): Int? {
        if (birthdayMillis == null) return null
        val b = millisToDate(birthdayMillis)
        var age = date.year - b.year
        if (date.monthValue < b.monthValue ||
            (date.monthValue == b.monthValue && date.dayOfMonth < b.dayOfMonth)
        ) age--
        return if (age < 0) null else age
    }

    /** 重复文案：每2周重复 */
    fun repeatText(repeatValue: Int, repeatUnit: Int): String {
        if (repeatValue <= 0) return "不重复"
        val unit = when (repeatUnit) {
            UNIT_DAY -> "天"
            UNIT_WEEK -> "周"
            UNIT_MONTH -> "月"
            else -> "年"
        }
        return "每${repeatValue}${unit}重复"
    }

    fun unitName(unit: Int): String = when (unit) {
        UNIT_DAY -> "天"
        UNIT_WEEK -> "周"
        UNIT_MONTH -> "月"
        else -> "年"
    }
}
