package com.drink.watertracker.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _dailyGoal = MutableStateFlow(prefs.getInt("daily_goal", 2000))
    private val _reminderInterval = MutableStateFlow(prefs.getInt("reminder_interval", 60))
    private val _reminderEnabled = MutableStateFlow(prefs.getBoolean("reminder_enabled", true))
    private val _reminderMessage = MutableStateFlow(
        prefs.getString("reminder_message", "") ?: ""
    )

    val dailyGoal: Flow<Int> = _dailyGoal.asStateFlow()
    val reminderInterval: Flow<Int> = _reminderInterval.asStateFlow()
    val reminderEnabled: Flow<Boolean> = _reminderEnabled.asStateFlow()
    val reminderMessage: Flow<String> = _reminderMessage.asStateFlow()

    fun setDailyGoal(goal: Int) {
        prefs.edit().putInt("daily_goal", goal).apply()
        _dailyGoal.value = goal
    }

    fun setReminderInterval(minutes: Int) {
        prefs.edit().putInt("reminder_interval", minutes).apply()
        _reminderInterval.value = minutes
    }

    fun setReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("reminder_enabled", enabled).apply()
        _reminderEnabled.value = enabled
    }

    fun setReminderMessage(message: String) {
        prefs.edit().putString("reminder_message", message).apply()
        _reminderMessage.value = message
    }
}
