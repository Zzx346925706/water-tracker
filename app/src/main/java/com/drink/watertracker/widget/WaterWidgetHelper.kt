package com.drink.watertracker.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.drink.watertracker.R
import com.drink.watertracker.data.WaterDatabase
import com.drink.watertracker.data.WaterRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

object WaterWidgetHelper {

    fun updateAll(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, WaterWidgetReceiver::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val dao = WaterDatabase.getDatabase(context).waterDao()
        val today = LocalDate.now().toString()

        val total = runBlocking { dao.getTotalByDate(today).first() }
        val goal = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getInt("daily_goal", 2000)
        val progress = if (goal > 0) (total.toFloat() / goal).coerceIn(0f, 1f) else 0f
        val percent = (progress * 100).toInt()

        val views = RemoteViews(context.packageName, R.layout.widget_water)
        views.setTextViewText(R.id.widget_title, "💧 今日喝水")
        views.setTextViewText(R.id.widget_stats, "${total}ml / ${goal}ml  ($percent%)")
        views.setProgressBar(R.id.widget_progress, 100, percent, false)

        // Quick add button intents
        views.setOnClickPendingIntent(
            R.id.btn_150,
            createAddIntent(context, 150)
        )
        views.setOnClickPendingIntent(
            R.id.btn_250,
            createAddIntent(context, 250)
        )
        views.setOnClickPendingIntent(
            R.id.btn_500,
            createAddIntent(context, 500)
        )

        appWidgetManager.updateAppWidget(appWidgetId, views)
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
