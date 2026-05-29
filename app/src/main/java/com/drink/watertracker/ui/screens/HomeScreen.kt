package com.drink.watertracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drink.watertracker.ui.MainViewModel
import com.drink.watertracker.ui.components.WaterProgress
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit
) {
    val total by viewModel.todayTotal.collectAsState()
    val goal by viewModel.dailyGoal.collectAsState()
    val records by viewModel.todayRecords.collectAsState()
    var showCustomDialog by remember { mutableStateOf(false) }

    val quickAmounts = listOf(150, 250, 350, 500)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("💧 喝水记录") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "设置")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                WaterProgress(current = total, goal = goal)
                Spacer(Modifier.height(24.dp))
            }

            // Quick add buttons
            item {
                Text(
                    "快速记录",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickAmounts.forEach { amount ->
                        FilledTonalButton(
                            onClick = { viewModel.addWater(amount) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("${amount}ml", fontSize = 13.sp)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showCustomDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("自定义")
                    }
                    OutlinedButton(
                        onClick = { viewModel.addWater(200) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.LocalCafe, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("一杯水")
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            // Today's records
            item {
                Text(
                    "今日记录",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }

            if (records.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Text(
                            "今天还没有喝水记录，快来喝一杯吧 💪",
                            modifier = Modifier.padding(24.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(records, key = { it.id }) { record ->
                var showDelete by remember { mutableStateOf(false) }
                val time = remember(record.timestamp) {
                    Instant.ofEpochMilli(record.timestamp)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("HH:mm"))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WaterDrop,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "${record.amount}ml",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                time,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { showDelete = true }) {
                            Icon(
                                Icons.Default.Delete,
                                "删除",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                if (showDelete) {
                    AlertDialog(
                        onDismissRequest = { showDelete = false },
                        title = { Text("删除记录") },
                        text = { Text("确定删除这条 ${record.amount}ml 的记录吗？") },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.deleteRecord(record)
                                showDelete = false
                            }) { Text("删除") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDelete = false }) { Text("取消") }
                        }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // Custom amount dialog
    if (showCustomDialog) {
        var customAmount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("自定义饮水量") },
            text = {
                OutlinedTextField(
                    value = customAmount,
                    onValueChange = { customAmount = it.filter { c -> c.isDigit() } },
                    label = { Text("毫升 (ml)") },
                    singleLine = true,
                    suffix = { Text("ml") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    customAmount.toIntOrNull()?.let {
                        if (it > 0) viewModel.addWater(it)
                    }
                    showCustomDialog = false
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) { Text("取消") }
            }
        )
    }
}
