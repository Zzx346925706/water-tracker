package com.drink.watertracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.drink.watertracker.WaterApp
import com.drink.watertracker.data.SettingsRepository
import com.drink.watertracker.data.WaterRecord
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = (application as WaterApp).database.waterDao()
    private val settings = SettingsRepository(application)

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

    fun addWater(amount: Int) {
        viewModelScope.launch {
            dao.insert(
                WaterRecord(
                    amount = amount,
                    timestamp = System.currentTimeMillis(),
                    date = today
                )
            )
        }
    }

    fun deleteRecord(record: WaterRecord) {
        viewModelScope.launch {
            dao.deleteById(record.id)
        }
    }

    fun setDailyGoal(goal: Int) {
        settings.setDailyGoal(goal)
    }

    fun setReminderInterval(minutes: Int) {
        settings.setReminderInterval(minutes)
    }

    fun setReminderEnabled(enabled: Boolean) {
        settings.setReminderEnabled(enabled)
    }
}
