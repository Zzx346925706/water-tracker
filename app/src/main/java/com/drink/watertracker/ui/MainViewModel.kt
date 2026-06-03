package com.drink.watertracker.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.drink.watertracker.WaterApp
import com.drink.watertracker.data.DailyTotal
import com.drink.watertracker.data.SettingsRepository
import com.drink.watertracker.data.WaterRecord
import com.drink.watertracker.widget.WaterWidgetHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = (application as WaterApp).database.waterDao()
    private val settings = SettingsRepository(application)
    private val prefs = application.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    val todayRecords: StateFlow<List<WaterRecord>> = dao.getRecordsByDate(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTotal: StateFlow<Int> = dao.getTotalByDate(today)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val dailyGoal: StateFlow<Int> = settings.dailyGoal
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2000)

    val reminderInterval: StateFlow<Int> = settings.reminderInterval
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 60)

    val reminderEnabled: StateFlow<Boolean> = settings.reminderEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _backgroundUri = MutableStateFlow(
        prefs.getString("background_uri", "") ?: ""
    )
    val backgroundUri: StateFlow<String> = _backgroundUri

    private val _backgroundBlur = MutableStateFlow(
        prefs.getFloat("background_blur", 0f)
    )
    val backgroundBlur: StateFlow<Float> = _backgroundBlur

    // 月度历史数据
    val monthlyHistory: StateFlow<List<DailyTotal>> = dao.getDailyTotals(
        LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE)
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 成就相关
    var streakDays: Int = 0
        private set
    var totalMl: Long = 0L
        private set

    private val _achievements = MutableStateFlow<List<com.drink.watertracker.ui.screens.Badge>>(emptyList())
    val achievements: StateFlow<List<com.drink.watertracker.ui.screens.Badge>> = _achievements

    init {
        viewModelScope.launch {
            // 计算累计饮水量
            dao.getAllTotals().collect { totals ->
                totalMl = totals.sumOf { it.total.toLong() }
                updateAchievements(totals)
            }
        }
    }

    private fun updateAchievements(totals: List<DailyTotal>) {
        val goal = dailyGoal.value
        val todayTotal = todayTotal.value

        // 计算连续达标天数
        streakDays = calculateStreak(totals, goal)

        // 今日首次记录
        val hasFirstDrop = todayRecords.value.isNotEmpty()

        // 计算总天数
        val totalDays = totals.size

        val badges = mutableListOf<com.drink.watertracker.ui.screens.Badge>()

        // === 蜡笔小新主题徽章 ===

        // 1. 小新的第一口水 💧
        badges.add(com.drink.watertracker.ui.screens.Badge(
            id = "first_drop", emoji = "💧", name = "小新的第一口",
            desc = "记录第一次喝水", unlocked = hasFirstDrop
        ))

        // 2. 动感超人出击 🔥 (连续3天)
        badges.add(com.drink.watertracker.ui.screens.Badge(
            id = "streak_3", emoji = "🔥", name = "动感超人",
            desc = "连续达标3天", unlocked = streakDays >= 3,
            progress = if (streakDays < 3) "$streakDays/3天" else "已解锁"
        ))

        // 3. 向日葵班优等生 ⭐ (连续7天)
        badges.add(com.drink.watertracker.ui.screens.Badge(
            id = "streak_7", emoji = "⭐", name = "向日葵班",
            desc = "连续达标7天", unlocked = streakDays >= 7,
            progress = if (streakDays < 7) "$streakDays/7天" else "已解锁"
        ))

        // 4. 春日部防卫队 🛡️ (连续14天)
        badges.add(com.drink.watertracker.ui.screens.Badge(
            id = "streak_14", emoji = "🛡️", name = "春日部防卫队",
            desc = "连续达标14天", unlocked = streakDays >= 14,
            progress = if (streakDays < 14) "$streakDays/14天" else "已解锁"
        ))

        // 5. 小新的屁股之王 👑 (连续30天)
        badges.add(com.drink.watertracker.ui.screens.Badge(
            id = "streak_30", emoji = "👑", name = "屁股超人",
            desc = "连续达标30天", unlocked = streakDays >= 30,
            progress = if (streakDays < 30) "$streakDays/30天" else "已解锁"
        ))

        // 6. 小葵的奶瓶 🍼 (累计10L)
        badges.add(com.drink.watertracker.ui.screens.Badge(
            id = "total_10l", emoji = "🍼", name = "小葵的奶瓶",
            desc = "累计喝10L水", unlocked = totalMl >= 10000,
            progress = if (totalMl < 10000) "${totalMl / 1000}L/10L" else "已解锁"
        ))

        // 7. 美伢的购物袋 🛍️ (累计50L)
        badges.add(com.drink.watertracker.ui.screens.Badge(
            id = "total_50l", emoji = "🛍️", name = "美伢的购物袋",
            desc = "累计喝50L水", unlocked = totalMl >= 50000,
            progress = if (totalMl < 50000) "${totalMl / 1000}L/50L" else "已解锁"
        ))

        // 8. 广志的啤酒肚 🍺 (累计100L)
        badges.add(com.drink.watertracker.ui.screens.Badge(
            id = "total_100l", emoji = "🍺", name = "广志的啤酒肚",
            desc = "累计喝100L水", unlocked = totalMl >= 100000,
            progress = if (totalMl < 100000) "${totalMl / 1000}L/100L" else "已解锁"
        ))

        // 9. 小白的散步 🐾 (记录7天)
        badges.add(com.drink.watertracker.ui.screens.Badge(
            id = "days_7", emoji = "🐾", name = "小白的散步",
            desc = "累计记录7天", unlocked = totalDays >= 7,
            progress = if (totalDays < 7) "$totalDays/7天" else "已解锁"
        ))

        // 10. 鲜花装扮 🌸 (记录30天)
        badges.add(com.drink.watertracker.ui.screens.Badge(
            id = "days_30", emoji = "🌸", name = "鲜花装扮",
            desc = "累计记录30天", unlocked = totalDays >= 30,
            progress = if (totalDays < 30) "$totalDays/30天" else "已解锁"
        ))

        // 11. 今日超额 💯 (超过150%)
        badges.add(com.drink.watertracker.ui.screens.Badge(
            id = "over_150", emoji = "💯", name = "动感光波",
            desc = "今日超标150%", unlocked = goal > 0 && todayTotal >= goal * 1.5f
        ))

        // 12. 完美一天 ✅ (刚好100%)
        badges.add(com.drink.watertracker.ui.screens.Badge(
            id = "perfect", emoji = "✅", name = "完美一天",
            desc = "精准达标100%", unlocked = goal > 0 && todayTotal >= goal && todayTotal < goal * 1.1f
        ))

        // 13. 早起喝水 🌅 (8点前)
        val hour = java.time.LocalTime.now().hour
        val earlyRecords = todayRecords.value.filter {
            val recordHour = java.time.Instant.ofEpochMilli(it.timestamp)
                .atZone(java.time.ZoneId.systemDefault()).hour
            recordHour < 8
        }
        badges.add(com.drink.watertracker.ui.screens.Badge(
            id = "early_bird", emoji = "🌅", name = "早起的小新",
            desc = "8点前喝水", unlocked = earlyRecords.isNotEmpty()
        ))

        // 14. 夜猫子 🌙 (22点后)
        val nightRecords = todayRecords.value.filter {
            val recordHour = java.time.Instant.ofEpochMilli(it.timestamp)
                .atZone(java.time.ZoneId.systemDefault()).hour
            recordHour >= 22
        }
        badges.add(com.drink.watertracker.ui.screens.Badge(
            id = "night_owl", emoji = "🌙", name = "夜猫子小新",
            desc = "22点后喝水", unlocked = nightRecords.isNotEmpty()
        ))

        // 15. 水量大爆发 🌊 (单次500ml)
        val hasBigDrink = todayRecords.value.any { it.amount >= 500 }
        badges.add(com.drink.watertracker.ui.screens.Badge(
            id = "big_drink", emoji = "🌊", name = "水量大爆发",
            desc = "单次喝500ml", unlocked = hasBigDrink
        ))

        _achievements.value = badges
    }

    private fun calculateStreak(totals: List<DailyTotal>, goal: Int): Int {
        if (goal <= 0) return 0
        val sorted = totals.sortedByDescending { it.date }
        var streak = 0
        var checkDate = LocalDate.now()

        for (daily in sorted) {
            val date = try { LocalDate.parse(daily.date) } catch (e: Exception) { continue }
            if (date == checkDate || date == checkDate.minusDays(1)) {
                if (daily.total >= goal) {
                    streak++
                    checkDate = date.minusDays(1)
                } else if (date == LocalDate.now()) {
                    // 今天还没达标，检查昨天
                    checkDate = date.minusDays(1)
                    continue
                } else {
                    break
                }
            } else {
                break
            }
        }
        return streak
    }

    fun addWater(amount: Int) {
        viewModelScope.launch {
            dao.insert(
                WaterRecord(
                    amount = amount,
                    timestamp = System.currentTimeMillis(),
                    date = today
                )
            )
            WaterWidgetHelper.updateAll(getApplication())
            // 刷新成就
            dao.getAllTotals().first().let { updateAchievements(it) }
        }
    }

    fun deleteRecord(record: WaterRecord) {
        viewModelScope.launch {
            dao.deleteById(record.id)
            WaterWidgetHelper.updateAll(getApplication())
            dao.getAllTotals().first().let { updateAchievements(it) }
        }
    }

    fun setDailyGoal(goal: Int) {
        settings.setDailyGoal(goal)
        WaterWidgetHelper.updateAll(getApplication())
    }

    fun setReminderInterval(minutes: Int) {
        settings.setReminderInterval(minutes)
    }

    fun setReminderEnabled(enabled: Boolean) {
        settings.setReminderEnabled(enabled)
    }

    fun setBackground(uri: String, blur: Float) {
        prefs.edit()
            .putString("background_uri", uri)
            .putFloat("background_blur", blur)
            .apply()
        _backgroundUri.value = uri
        _backgroundBlur.value = blur
    }
}
