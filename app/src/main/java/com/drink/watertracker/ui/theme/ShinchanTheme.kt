package com.drink.watertracker.ui.theme

import androidx.compose.ui.graphics.Color
import java.time.LocalDate

// 蜡笔小新主题色板 - 每天一个不同的配色
object ShinchanTheme {

    // 7套每日主题色（周一到周日）
    private val dailyThemes = listOf(
        // 周一 - 温暖橙色（小新的T恤色）
        DayTheme(
            primary = Color(0xFFFF6F00),
            primaryLight = Color(0xFFFFB74D),
            background = Color(0xFFFFF8E1),
            surface = Color(0xFFFFECB3),
            gradientStart = Color(0xFFFFCC80),
            gradientEnd = Color(0xFFFF8A65),
            emoji = "🧡"
        ),
        // 周二 - 活力红色（小新的帽子）
        DayTheme(
            primary = Color(0xFFE53935),
            primaryLight = Color(0xFFEF9A9A),
            background = Color(0xFFFFEBEE),
            surface = Color(0xFFFFCDD2),
            gradientStart = Color(0xFFEF9A9A),
            gradientEnd = Color(0xFFFF8A80),
            emoji = "❤️"
        ),
        // 周三 - 清新绿色（动感超人）
        DayTheme(
            primary = Color(0xFF43A047),
            primaryLight = Color(0xFFA5D6A7),
            background = Color(0xFFE8F5E9),
            surface = Color(0xFFC8E6C9),
            gradientStart = Color(0xFFA5D6A7),
            gradientEnd = Color(0xFF81C784),
            emoji = "💚"
        ),
        // 周四 - 天空蓝色（小新最爱的泳装）
        DayTheme(
            primary = Color(0xFF1E88E5),
            primaryLight = Color(0xFF90CAF9),
            background = Color(0xFFE3F2FD),
            surface = Color(0xFFBBDEFB),
            gradientStart = Color(0xFF90CAF9),
            gradientEnd = Color(0xFF64B5F6),
            emoji = "💙"
        ),
        // 周五 - 可爱粉色（小葵的礼物）
        DayTheme(
            primary = Color(0xFFD81B60),
            primaryLight = Color(0xFFF48FB1),
            background = Color(0xFFFCE4EC),
            surface = Color(0xFFF8BBD0),
            gradientStart = Color(0xFFF48FB1),
            gradientEnd = Color(0xFFE91E63),
            emoji = "💖"
        ),
        // 周六 - 活力黄色（酢乙女爱）
        DayTheme(
            primary = Color(0xFFFDD835),
            primaryLight = Color(0xFFFFF176),
            background = Color(0xFFFFFDE7),
            surface = Color(0xFFFFF9C4),
            gradientStart = Color(0xFFFFF176),
            gradientEnd = Color(0xFFFFD54F),
            emoji = "💛"
        ),
        // 周日 - 梦幻紫色（美伢的浪漫）
        DayTheme(
            primary = Color(0xFF7B1FA2),
            primaryLight = Color(0xFFCE93D8),
            background = Color(0xFFF3E5F5),
            surface = Color(0xFFE1BEE7),
            gradientStart = Color(0xFFCE93D8),
            gradientEnd = Color(0xFFBA68C8),
            emoji = "💜"
        )
    )

    // 蜡笔小新经典配色
    val ShincanYellow = Color(0xFFFFD600)  // 小新皮肤色
    val ShincanRed = Color(0xFFE53935)     // 帽子/衣服
    val ShincanBrown = Color(0xFF795548)   // 头发
    val ShincanBlue = Color(0xFF1565C0)    // 裤子
    val ShincanWhite = Color(0xFFFFFDE7)   // 背景白
    val ShincanPink = Color(0xFFF48FB1)    // 脸颊
    val ShincanGreen = Color(0xFF66BB6A)   // 动感超人

    // 获取今天的主题
    fun todayTheme(): DayTheme {
        val dayOfWeek = LocalDate.now().dayOfWeek.value  // 1=Monday, 7=Sunday
        return dailyThemes[(dayOfWeek - 1) % 7]
    }

    // 根据日期获取主题（用于小部件）
    fun themeForDate(date: LocalDate): DayTheme {
        val dayOfWeek = date.dayOfWeek.value
        return dailyThemes[(dayOfWeek - 1) % 7]
    }
}

data class DayTheme(
    val primary: Color,
    val primaryLight: Color,
    val background: Color,
    val surface: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val emoji: String
)
