package com.drink.watertracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drink.watertracker.ui.MainViewModel
import com.drink.watertracker.ui.screens.HomeScreen
import com.drink.watertracker.ui.screens.SettingsScreen
import com.drink.watertracker.ui.theme.WaterTrackerTheme
import com.drink.watertracker.worker.ReminderScheduler

class MainActivity : ComponentActivity() {

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            WaterTrackerTheme {
                val vm: MainViewModel = viewModel()
                var showSettings by remember { mutableStateOf(false) }

                AnimatedContent(
                    targetState = showSettings,
                    transitionSpec = {
                        if (targetState) {
                            slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith
                                    slideOutHorizontally { it } + fadeOut()
                        }
                    },
                    label = "nav"
                ) { settings ->
                    if (settings) {
                        SettingsScreen(vm, onBack = { showSettings = false })
                    } else {
                        HomeScreen(vm, onNavigateToSettings = { showSettings = true })
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure reminder is scheduled
        val enabled = getSharedPreferences("settings", MODE_PRIVATE)
            .getBoolean("reminder_enabled", true)
        if (enabled) {
            val interval = getSharedPreferences("settings", MODE_PRIVATE)
                .getInt("reminder_interval", 60)
            ReminderScheduler.schedule(interval)
        }
    }
}
