package com.drink.watertracker.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.drink.watertracker.data.WaterDatabase
import com.drink.watertracker.data.WaterRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class WidgetActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.drink.watertracker.ADD_WATER") {
            val amount = intent.getIntExtra("amount", 250)
            val dao = WaterDatabase.getDatabase(context).waterDao()

            CoroutineScope(Dispatchers.IO).launch {
                dao.insert(
                    WaterRecord(
                        amount = amount,
                        timestamp = System.currentTimeMillis(),
                        date = LocalDate.now().toString()
                    )
                )
                WaterWidgetHelper.updateAll(context)
            }
        }
    }
}
