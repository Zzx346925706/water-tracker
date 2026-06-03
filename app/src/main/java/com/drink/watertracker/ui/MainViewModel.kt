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

    fun addWater(amount: Int) {
        viewModelScope.launch {
            dao.insert(
                WaterRecord(
                    amount = amount,
                    timestamp = System.currentTimeMillis(),
                    date = today
                )
            )
            // 更新小部件
            WaterWidgetHelper.updateAll(getApplication())
        }
    }

    fun deleteRecord(record: WaterRecord) {
        viewModelScope.launch {
            dao.deleteById(record.id)
            WaterWidgetHelper.updateAll(getApplication())
        }
    }

    fun setDailyGoal(goal: Int) {
        settings.setDailyGoal(goal)
        // 同步更新小部件
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

    // 月度历史数据
    val monthlyHistory: StateFlow<List<DailyTotal>> = dao.getDailyTotals(
        LocalDate.now().minusDays(30).format(DateTimeFormatter.ISO_LOCAL_DATE)
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
