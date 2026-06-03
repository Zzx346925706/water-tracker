package com.drink.watertracker.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.drink.watertracker.R
import com.drink.watertracker.data.WaterDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

object WaterWidgetHelper {

    private val dailyEmojis = listOf("🧡", "❤️", "💚", "💙", "💖", "💛", "💜")
    private val dailyNames = listOf("元气橙", "热情红", "清新绿", "天空蓝", "可爱粉", "活力黄", "梦幻紫")

    fun updateAll(context: Context) {
        update2x2(context)
        update4x2(context)
    }

    private fun getData(context: Context): Triple<Int, Int, Int> {
        val dao = WaterDatabase.getDatabase(context).waterDao()
        val today = LocalDate.now().toString()
        val total = runBlocking { dao.getTotalByDate(today).first() }
        val goal = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getInt("daily_goal", 2000)
        val progress = if (goal > 0) (total.toFloat() / goal).coerceIn(0f, 1f) else 0f
        val percent = (progress * 100).toInt()
        return Triple(total, goal, percent)
    }

    private fun getDayInfo(): Pair<String, String> {
        val dayOfWeek = LocalDate.now().dayOfWeek.value
        return Pair(dailyEmojis[(dayOfWeek - 1) % 7], dailyNames[(dayOfWeek - 1) % 7])
    }

    // 2x2
    private fun update2x2(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, WaterWidgetReceiver2x2::class.java))
        if (ids.isEmpty()) return

        val (total, goal, percent) = getData(context)
        val (emoji, dayName) = getDayInfo()

        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_water_2x2)
            views.setTextViewText(R.id.widget_emoji, emoji)
            views.setTextViewText(R.id.widget_title, "喝水 · $dayName")
            views.setTextViewText(R.id.widget_stats, "${total}ml / ${goal}ml")
            views.setProgressBar(R.id.widget_progress, 100, percent, false)
            views.setOnClickPendingIntent(R.id.btn_150, createAddIntent(context, 150))
            views.setOnClickPendingIntent(R.id.btn_250, createAddIntent(context, 250))
            views.setOnClickPendingIntent(R.id.btn_350, createAddIntent(context, 350))
            views.setOnClickPendingIntent(R.id.btn_500, createAddIntent(context, 500))
            manager.updateAppWidget(id, views)
        }
    }

    // 4x2
    private fun update4x2(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, WaterWidgetReceiver4x2::class.java))
        if (ids.isEmpty()) return

        val (total, goal, percent) = getData(context)
        val (emoji, dayName) = getDayInfo()

        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_water_4x2)
            views.setTextViewText(R.id.widget_emoji, emoji)
            views.setTextViewText(R.id.widget_title, "喝水助手 · $dayName")
            views.setTextViewText(R.id.widget_stats, "${total}ml / ${goal}ml")
            views.setTextViewText(R.id.widget_percent, "${percent}%")
            views.setProgressBar(R.id.widget_progress, 100, percent, false)
            views.setOnClickPendingIntent(R.id.btn_150, createAddIntent(context, 150))
            views.setOnClickPendingIntent(R.id.btn_250, createAddIntent(context, 250))
            views.setOnClickPendingIntent(R.id.btn_350, createAddIntent(context, 350))
            views.setOnClickPendingIntent(R.id.btn_500, createAddIntent(context, 500))
            manager.updateAppWidget(id, views)
        }
    }

    private fun createAddIntent(context: Context, amount: Int): android.app.PendingIntent {
        val intent = Intent(context, WidgetActionReceiver::class.java).apply {
            action = "com.drink.watertracker.ADD_WATER"
            putExtra("amount", amount)
        }
        return android.app.PendingIntent.getBroadcast(
            context,
            amount,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
    }
}
