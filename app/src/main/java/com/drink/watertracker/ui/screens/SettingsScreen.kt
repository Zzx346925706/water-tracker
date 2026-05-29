package com.drink.watertracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drink.watertracker.ui.MainViewModel
import com.drink.watertracker.worker.ReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val goal by viewModel.dailyGoal.collectAsState()
    val interval by viewModel.reminderInterval.collectAsState()
    val enabled by viewModel.reminderEnabled.collectAsState()

    var goalText by remember(goal) { mutableStateOf(goal.toString()) }
    var intervalText by remember(interval) { mutableStateOf(interval.toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚙️ 设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Daily goal
            Text("每日目标", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it.filter { c -> c.isDigit() } },
                    label = { Text("目标量") },
                    suffix = { Text("ml") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = {
                    goalText.toIntOrNull()?.let {
                        if (it > 0) viewModel.setDailyGoal(it)
                    }
                }) { Text("保存") }
            }

            // Quick goal presets
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1500, 2000, 2500, 3000).forEach { g ->
                    FilterChip(
                        selected = goal == g,
                        onClick = {
                            viewModel.setDailyGoal(g)
                            goalText = g.toString()
                        },
                        label = { Text("${g / 1000}L") }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(24.dp))

            // Reminder
            Text("喝水提醒", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("开启提醒")
                Switch(
                    checked = enabled,
                    onCheckedChange = {
                        viewModel.setReminderEnabled(it)
                        if (it) ReminderScheduler.schedule(interval)
                        else ReminderScheduler.cancel()
                    }
                )
            }

            if (enabled) {
                Spacer(Modifier.height(12.dp))
                Text("提醒间隔")
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = intervalText,
                        onValueChange = { intervalText = it.filter { c -> c.isDigit() } },
                        label = { Text("间隔") },
                        suffix = { Text("分钟") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        intervalText.toIntOrNull()?.let {
                            if (it > 0) {
                                viewModel.setReminderInterval(it)
                                ReminderScheduler.schedule(it)
                            }
                        }
                    }) { Text("保存") }
                }

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(30, 60, 90, 120).forEach { m ->
                        FilterChip(
                            selected = interval == m,
                            onClick = {
                                viewModel.setReminderInterval(m)
                                intervalText = m.toString()
                                ReminderScheduler.schedule(m)
                            },
                            label = { Text("${m}分钟") }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(24.dp))

            // About
            Text("关于", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text(
                "💧 喝水助手 v1.0\n每天喝够水，身体更健康！",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
