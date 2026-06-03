package com.drink.watertracker.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent

class WaterWidgetReceiver1x1 : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            WaterWidgetHelper.updateAll(context)
        }
    }

    override fun onEnabled(context: Context) {
        WaterWidgetHelper.updateAll(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.drink.watertracker.ADD_WATER") {
            WaterWidgetHelper.updateAll(context)
        }
    }
}
