package com.drink.watertracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.drink.watertracker.data.WaterDatabase
import com.drink.watertracker.worker.ReminderScheduler

class WaterApp : Application() {
    val database by lazy { WaterDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        ReminderScheduler.init(this)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "喝水提醒",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "提醒你按时喝水"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "water_reminder"
    }
}
