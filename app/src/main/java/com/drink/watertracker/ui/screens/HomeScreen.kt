package com.drink.watertracker.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drink.watertracker.ui.MainViewModel
import com.drink.watertracker.ui.components.ShinchanBackground
import com.drink.watertracker.ui.components.WaterProgress
import com.drink.watertracker.ui.theme.ShinchanTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val total by viewModel.todayTotal.collectAsState()
    val goal by viewModel.dailyGoal.collectAsState()
    val records by viewModel.todayRecords.collectAsState()
    val bgUri by viewModel.backgroundUri.collectAsState()
    val bgBlur by viewModel.backgroundBlur.collectAsState()
    var showCustomDialog by remember { mutableStateOf(false) }

    val dayTheme = ShinchanTheme.todayTheme()
    val quickAmounts = listOf(150, 250, 350, 500)

    ShinchanBackground(backgroundUri = bgUri, backgroundBlur = bgBlur) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "喝水助手",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    actions = {
                        IconButton(onClick = onNavigateToHistory) {
                            Icon(Icons.Default.DateRange, "历史", tint = Color.White)
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, "设置", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 今日主题
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.85f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(dayTheme.emoji, fontSize = 36.sp)
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "今日主题",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = dayTheme.primary
                                )
                                Text(
                                    getDayName(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = dayTheme.primary
                                )
                            }
                        }
                    }
                }

                // 进度环
                item {
                    Spacer(Modifier.height(8.dp))
                    WaterProgress(current = total, goal = goal)
                    Spacer(Modifier.height(16.dp))
                }

                // 快速记录 - 网格布局
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.85f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "🥤 快速记录",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(12.dp))

                            // 第一行：150 250
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickAmountButton(
                                    amount = 150,
                                    label = "💧 小杯",
                                    onClick = { viewModel.addWater(150) },
                                    color = dayTheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                QuickAmountButton(
                                    amount = 250,
                                    label = "💧 中杯",
                                    onClick = { viewModel.addWater(250) },
                                    color = dayTheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(Modifier.height(8.dp))

                            // 第二行：350 500
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                QuickAmountButton(
                                    amount = 350,
                                    label = "🥤 大杯",
                                    onClick = { viewModel.addWater(350) },
                                    color = dayTheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                QuickAmountButton(
                                    amount = 500,
                                    label = "🫗 超大",
                                    onClick = { viewModel.addWater(500) },
                                    color = dayTheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(Modifier.height(8.dp))

                            // 第三行：自定义 一杯水
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { showCustomDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("自定义")
                                }
                                OutlinedButton(
                                    onClick = { viewModel.addWater(200) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("🥛", fontSize = 18.sp)
                                    Spacer(Modifier.width(4.dp))
                                    Text("一杯水")
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // 今日记录
                item {
                    Text(
                        "📝 今日记录",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                }

                if (records.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.85f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🌵", fontSize = 48.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "今天还没有喝水记录",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = dayTheme.primary
                                )
                                Text(
                                    "快来喝一杯吧！",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.9f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(dayTheme.primaryLight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("💧", fontSize = 20.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${record.amount}ml",
                                    fontWeight = FontWeight.Bold,
                                    color = dayTheme.primary
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
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    if (showDelete) {
                        AlertDialog(
                            onDismissRequest = { showDelete = false },
                            title = { Text("🗑️ 删除记录") },
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

                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "V1.5.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }

    // 自定义对话框
    if (showCustomDialog) {
        var customAmount by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("✏️ 自定义饮水量") },
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

@Composable
private fun QuickAmountButton(
    amount: Int,
    label: String,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 14.sp)
            Text("${amount}ml", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

private fun getDayName(): String {
    val day = java.time.LocalDate.now().dayOfWeek.value
    return when (day) {
        1 -> "周一 · 元气橙"
        2 -> "周二 · 热情红"
        3 -> "周三 · 清新绿"
        4 -> "周四 · 天空蓝"
        5 -> "周五 · 可爱粉"
        6 -> "周六 · 活力黄"
        7 -> "周日 · 梦幻紫"
        else -> "今天"
    }
}
