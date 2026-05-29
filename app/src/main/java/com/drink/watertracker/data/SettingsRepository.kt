package com.drink.watertracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object PreferenceKeys {
    val DAILY_GOAL = intPreferencesKey("daily_goal")       // ml
    val REMINDER_INTERVAL = intPreferencesKey("reminder_interval") // minutes
    val REMINDER_ENABLED = booleanPreferencesKey("reminder_enabled")
}

class SettingsRepository(private val context: Context) {

    val dailyGoal: Flow<Int> = context.dataStore.data.map { it[PreferenceKeys.DAILY_GOAL] ?: 2000 }

    val reminderInterval: Flow<Int> = context.dataStore.data.map { it[PreferenceKeys.REMINDER_INTERVAL] ?: 60 }

    val reminderEnabled: Flow<Boolean> = context.dataStore.data.map { it[PreferenceKeys.REMINDER_ENABLED] ?: true }

    suspend fun setDailyGoal(goal: Int) {
        context.dataStore.edit { it[PreferenceKeys.DAILY_GOAL] = goal }
    }

    suspend fun setReminderInterval(minutes: Int) {
        context.dataStore.edit { it[PreferenceKeys.REMINDER_INTERVAL] = minutes }
    }

    suspend fun setReminderEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.REMINDER_ENABLED] = enabled }
    }
}
