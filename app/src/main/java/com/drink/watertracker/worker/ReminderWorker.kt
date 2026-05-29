package com.drink.watertracker.worker

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.drink.watertracker.MainActivity
import com.drink.watertracker.WaterApp
import com.drink.watertracker.widget.WaterWidgetHelper
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class ReminderWorker(
    ctx: Context,
    params: WorkerParameters
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val dao = (applicationContext as WaterApp).database.waterDao()
        val today = java.time.LocalDate.now().toString()
        val total = dao.getTotalByDate(today).first()
        val goal = applicationContext.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getInt("daily_goal", 2000)

        val remaining = (goal - total).coerceAtLeast(0)
        val text = if (remaining > 0) {
            "你今天已经喝了 ${total}ml，还差 ${remaining}ml 就达标啦！"
        } else {
            "恭喜！今天的喝水目标已完成 🎉"
        }

        val intent = Intent(applicationContext, MainActivity::class.java)
        val pending = PendingIntent.getActivity(
            applicationContext, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, WaterApp.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("💧 该喝水啦")
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()

        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(1001, notification)

        // Update widget
        try {
            WaterWidgetHelper.updateAll(applicationContext)
        } catch (_: Exception) {}

        return Result.success()
    }
}

object ReminderScheduler {
    private const val WORK_NAME = "water_reminder"
    private lateinit var appContext: Context

    fun init(context: Context) { appContext = context.applicationContext }

    fun schedule(intervalMinutes: Int) {
        if (!::appContext.isInitialized) return
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(
            intervalMinutes.toLong(), TimeUnit.MINUTES
        )
            .setInitialDelay(intervalMinutes.toLong(), TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun cancel() {
        if (!::appContext.isInitialized) return
        WorkManager.getInstance(appContext).cancelUniqueWork(WORK_NAME)
    }
}
