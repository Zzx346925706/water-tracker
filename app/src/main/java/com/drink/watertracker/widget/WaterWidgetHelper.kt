package com.drink.watertracker.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.drink.watertracker.R
import com.drink.watertracker.data.WaterDatabase
import com.drink.watertracker.ui.theme.DayTheme
import com.drink.watertracker.ui.theme.ShinchanTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

object WaterWidgetHelper {

    private val dailyEmojis = listOf("🧡", "❤️", "💚", "💙", "💖", "💛", "💜")
    private val dailyNames = listOf("元气橙", "热情红", "清新绿", "天空蓝", "可爱粉", "活力黄", "梦幻紫")

    // 每日主题背景 Drawable（带圆角）
    private val dailyBgDrawables = intArrayOf(
        R.drawable.widget_bg_mon,  // 周一 橙
        R.drawable.widget_bg_tue,  // 周二 红
        R.drawable.widget_bg_wed,  // 周三 绿
        R.drawable.widget_bg_thu,  // 周四 蓝
        R.drawable.widget_bg_fri,  // 周五 粉
        R.drawable.widget_bg_sat,  // 周六 黄
        R.drawable.widget_bg_sun   // 周日 紫
    )

    // 1x1 每日主题背景 Drawable（圆角24dp）
    private val dailyBgDrawables1x1 = intArrayOf(
        R.drawable.widget_bg_1x1_mon,
        R.drawable.widget_bg_1x1_tue,
        R.drawable.widget_bg_1x1_wed,
        R.drawable.widget_bg_1x1_thu,
        R.drawable.widget_bg_1x1_fri,
        R.drawable.widget_bg_1x1_sat,
        R.drawable.widget_bg_1x1_sun
    )

    // 每日主题背景色（用于 setBackgroundColor + setCornerRadius）
    private val dailyBgColors by lazy {
        intArrayOf(
            android.graphics.Color.parseColor("#FFFFECB3"),  // 周一 橙
            android.graphics.Color.parseColor("#FFFFCDD2"),  // 周二 红
            android.graphics.Color.parseColor("#FFC8E6C9"),  // 周三 绿
            android.graphics.Color.parseColor("#FFBBDEFB"),  // 周四 蓝
            android.graphics.Color.parseColor("#FFF8BBD0"),  // 周五 粉
            android.graphics.Color.parseColor("#FFFFF9C4"),  // 周六 黄
            android.graphics.Color.parseColor("#FFD1C4E9")   // 周日 紫
        )
    }

    // 1x1 每日主题背景色
    private val dailyBgColors1x1 by lazy {
        dailyBgColors // 同色系
    }

    // 每日按钮背景 Drawable（带圆角）
    private val dailyBtnDrawables = intArrayOf(
        R.drawable.btn_widget_mon,
        R.drawable.btn_widget_tue,
        R.drawable.btn_widget_wed,
        R.drawable.btn_widget_thu,
        R.drawable.btn_widget_fri,
        R.drawable.btn_widget_sat,
        R.drawable.btn_widget_sun
    )

    // 每日主题色 int 值（用于文字颜色）
    private val dailyPrimaryColors by lazy {
        listOf(
            android.graphics.Color.parseColor("#FFFF6F00"),
            android.graphics.Color.parseColor("#FFE53935"),
            android.graphics.Color.parseColor("#FF43A047"),
            android.graphics.Color.parseColor("#FF1E88E5"),
            android.graphics.Color.parseColor("#FFD81B60"),
            android.graphics.Color.parseColor("#FFFDD835"),
            android.graphics.Color.parseColor("#FF7B1FA2")
        )
    }

    // 每日浅色 int 值（用于统计文字）
    private val dailyLightColors by lazy {
        listOf(
            android.graphics.Color.parseColor("#CCFF6F00"),
            android.graphics.Color.parseColor("#CCE53935"),
            android.graphics.Color.parseColor("#CC43A047"),
            android.graphics.Color.parseColor("#CC1E88E5"),
            android.graphics.Color.parseColor("#CCD81B60"),
            android.graphics.Color.parseColor("#CCFDD835"),
            android.graphics.Color.parseColor("#CC7B1FA2")
        )
    }

    fun updateAll(context: Context) {
        update1x1(context)
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

    /** 获取今天的星期索引 (0=周一, 6=周日) */
    private fun todayIndex(): Int {
        return (LocalDate.now().dayOfWeek.value - 1) % 7
    }

    private fun getDayInfo(): Triple<String, String, Int> {
        val idx = todayIndex()
        return Triple(dailyEmojis[idx], dailyNames[idx], idx)
    }

    // 2x2
    private fun update2x2(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, WaterWidgetReceiver2x2::class.java))
        if (ids.isEmpty()) return

        val (total, goal, percent) = getData(context)
        val (emoji, dayName, dayIdx) = getDayInfo()

        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_water_2x2)
            views.setTextViewText(R.id.widget_emoji, emoji)
            views.setTextViewText(R.id.widget_title, "喝水 · $dayName")
            views.setTextViewText(R.id.widget_stats, "${total}ml / ${goal}ml")
            views.setProgressBar(R.id.widget_progress, 100, percent, false)

            // 动态主题背景 — Android 12+ 用 setCornerRadius 保证圆角
            applyWidgetBackground(views, dailyBgColors[dayIdx], context)

            // 动态标题颜色
            views.setTextColor(R.id.widget_title, dailyPrimaryColors[dayIdx])
            views.setTextColor(R.id.widget_stats, dailyLightColors[dayIdx])

            // 动态按钮背景（保留圆角）
            views.setInt(R.id.btn_150, "setBackgroundResource", dailyBtnDrawables[dayIdx])
            views.setInt(R.id.btn_250, "setBackgroundResource", dailyBtnDrawables[dayIdx])
            views.setInt(R.id.btn_350, "setBackgroundResource", dailyBtnDrawables[dayIdx])
            views.setInt(R.id.btn_500, "setBackgroundResource", dailyBtnDrawables[dayIdx])

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
        val (emoji, dayName, dayIdx) = getDayInfo()

        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_water_4x2)
            views.setTextViewText(R.id.widget_emoji, emoji)
            views.setTextViewText(R.id.widget_title, "喝水助手 · $dayName")
            views.setTextViewText(R.id.widget_stats, "${total}ml / ${goal}ml")
            views.setTextViewText(R.id.widget_percent, "${percent}%")
            views.setProgressBar(R.id.widget_progress, 100, percent, false)

            // 动态主题背景 — Android 12+ 用 setCornerRadius 保证圆角
            applyWidgetBackground(views, dailyBgColors[dayIdx], context)

            // 动态文字颜色
            views.setTextColor(R.id.widget_title, dailyPrimaryColors[dayIdx])
            views.setTextColor(R.id.widget_percent, dailyPrimaryColors[dayIdx])
            views.setTextColor(R.id.widget_stats, dailyLightColors[dayIdx])

            // 动态按钮背景（保留圆角）
            views.setInt(R.id.btn_150, "setBackgroundResource", dailyBtnDrawables[dayIdx])
            views.setInt(R.id.btn_250, "setBackgroundResource", dailyBtnDrawables[dayIdx])
            views.setInt(R.id.btn_350, "setBackgroundResource", dailyBtnDrawables[dayIdx])
            views.setInt(R.id.btn_500, "setBackgroundResource", dailyBtnDrawables[dayIdx])

            views.setOnClickPendingIntent(R.id.btn_150, createAddIntent(context, 150))
            views.setOnClickPendingIntent(R.id.btn_250, createAddIntent(context, 250))
            views.setOnClickPendingIntent(R.id.btn_350, createAddIntent(context, 350))
            views.setOnClickPendingIntent(R.id.btn_500, createAddIntent(context, 500))
            manager.updateAppWidget(id, views)
        }
    }

    // 1x1
    private fun update1x1(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val ids = manager.getAppWidgetIds(ComponentName(context, WaterWidgetReceiver1x1::class.java))
        if (ids.isEmpty()) return

        val (_, _, percent) = getData(context)
        val (emoji, _, dayIdx) = getDayInfo()

        for (id in ids) {
            val views = RemoteViews(context.packageName, R.layout.widget_water_1x1)
            views.setTextViewText(R.id.widget_emoji, emoji)
            views.setTextViewText(R.id.widget_percent, "${percent}%")

            // 动态主题背景 — Android 12+ 用 setCornerRadius 保证圆角
            applyWidgetBackground(views, dailyBgColors1x1[dayIdx], context)
            // 动态文字颜色
            views.setTextColor(R.id.widget_percent, dailyPrimaryColors[dayIdx])

            views.setOnClickPendingIntent(R.id.btn_add, createAddIntent(context, 250))
            manager.updateAppWidget(id, views)
        }
    }

    /**
     * 设置小部件背景并保证圆角。
     * 在 ImageView 上绘制带圆角的 Bitmap，兼容 MIUI 桌面。
     */
    private fun applyWidgetBackground(
        views: RemoteViews,
        bgColor: Int,
        context: Context
    ) {
        val dm = context.resources.displayMetrics
        val w = 800
        val h = 800
        val radius = 60f

        val bitmap = android.graphics.Bitmap.createBitmap(w, h, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
        paint.color = bgColor
        canvas.drawRoundRect(0f, 0f, w.toFloat(), h.toFloat(), radius, radius, paint)

        views.setImageViewBitmap(R.id.widget_bg_image, bitmap)
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
