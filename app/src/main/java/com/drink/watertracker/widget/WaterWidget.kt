package com.drink.watertracker.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.*
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.appwidget.cornerRadius
import com.drink.watertracker.data.WaterDatabase
import com.drink.watertracker.data.WaterRecord
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class WaterWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = WaterDatabase.getDatabase(context).waterDao()
        val today = LocalDate.now().toString()
        val total = dao.getTotalByDate(today).first()
        val goal = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getInt("daily_goal", 2000)

        provideGlance {
            WaterWidgetContent(total = total, goal = goal)
        }
    }

    companion object {
        suspend fun update(context: Context, glanceId: GlanceId) {
            val dao = WaterDatabase.getDatabase(context).waterDao()
            val today = LocalDate.now().toString()
            val total = dao.getTotalByDate(today).first()
            val goal = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
                .getInt("daily_goal", 2000)

            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[intPreferencesKey("total")] = total
                prefs[intPreferencesKey("goal")] = goal
            }
            WaterWidget().update(context, glanceId)
        }
    }
}

@Composable
private fun WaterWidgetContent(total: Int, goal: Int) {
    val progress = if (goal > 0) (total.toFloat() / goal).coerceIn(0f, 1f) else 0f
    val percent = (progress * 100).toInt()

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(16.dp)
            .background(ImageProvider(com.drink.watertracker.R.drawable.widget_bg))
            .padding(12.dp)
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title
            Text(
                "💧 今日喝水",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Progress bar
            Box(modifier = GlanceModifier.fillMaxWidth().height(12.dp)) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .cornerRadius(6.dp)
                        .background(GlanceTheme.colors.surfaceVariant)
                )
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth(fraction = progress)
                        .height(12.dp)
                        .cornerRadius(6.dp)
                        .background(GlanceTheme.colors.primary)
                )
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Stats
            Text(
                "${total}ml / ${goal}ml  ($percent%)",
                style = TextStyle(
                    fontSize = 13.sp,
                    color = GlanceTheme.colors.onSurface
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            // Quick add buttons
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                QuickButton("150ml", 150)
                Spacer(modifier = GlanceModifier.width(8.dp))
                QuickButton("250ml", 250)
                Spacer(modifier = GlanceModifier.width(8.dp))
                QuickButton("500ml", 500)
            }
        }
    }
}

@Composable
private fun QuickButton(label: String, amount: Int) {
    Box(
        modifier = GlanceModifier
            .cornerRadius(8.dp)
            .background(GlanceTheme.colors.primaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(
                actionRunCallback<AddWaterCallback>(
                    parameters = actionParametersOf(
                        ActionParameters.Key<Int>("amount") to amount
                    )
                )
            )
    ) {
        Text(
            label,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onPrimaryContainer
            )
        )
    }
}

class AddWaterCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val amount = parameters[ActionParameters.Key<Int>("amount")] ?: 250
        val dao = WaterDatabase.getDatabase(context).waterDao()
        dao.insert(
            WaterRecord(
                amount = amount,
                timestamp = System.currentTimeMillis(),
                date = LocalDate.now().toString()
            )
        )
        // Refresh widget
        WaterWidget.update(context, glanceId)
    }
}
