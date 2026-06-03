package com.drink.watertracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drink.watertracker.data.DailyTotal
import com.drink.watertracker.ui.MainViewModel
import com.drink.watertracker.ui.components.ShinchanBackground
import com.drink.watertracker.ui.theme.ShinchanTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val monthlyHistory by viewModel.monthlyHistory.collectAsState()
    val goal by viewModel.dailyGoal.collectAsState()
    val dayTheme = ShinchanTheme.todayTheme()

    ShinchanBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("📊 历史记录", color = Color.White, fontWeight = FontWeight.Bold)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // 统计卡片
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem("📅", "记录天数", "${monthlyHistory.size}天")
                        StatItem("💧", "总饮水量", "${monthlyHistory.sumOf { it.total }}ml")
                        StatItem("📈", "日均饮水", "${if (monthlyHistory.isNotEmpty()) monthlyHistory.sumOf { it.total } / monthlyHistory.size else 0}ml")
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 柱状图
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "近30天饮水趋势",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))

                        if (monthlyHistory.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "暂无数据，开始记录吧！",
                                    color = Color.Gray
                                )
                            }
                        } else {
                            // 图表
                            BarChart(
                                data = monthlyHistory,
                                goal = goal,
                                primaryColor = dayTheme.primary,
                                lightColor = dayTheme.primaryLight,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                            )

                            Spacer(Modifier.height(8.dp))

                            // 图例
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Canvas(modifier = Modifier.size(12.dp)) {
                                    drawCircle(color = dayTheme.primary)
                                }
                                Spacer(Modifier.width(4.dp))
                                Text("饮水量", fontSize = 12.sp, color = Color.Gray)
                                Spacer(Modifier.width(16.dp))
                                Canvas(modifier = Modifier.size(12.dp)) {
                                    drawLine(
                                        color = Color.Red.copy(alpha = 0.5f),
                                        start = Offset(0f, size.height / 2),
                                        end = Offset(size.width, size.height / 2),
                                        strokeWidth = 2f
                                    )
                                }
                                Spacer(Modifier.width(4.dp))
                                Text("目标线", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 每日详情列表
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "每日详情",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(8.dp))

                        if (monthlyHistory.isEmpty()) {
                            Text("暂无数据", color = Color.Gray)
                        } else {
                            monthlyHistory.reversed().forEach { daily ->
                                DailyRow(daily, goal, dayTheme.primary)
                                if (daily != monthlyHistory.first()) {
                                    Divider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        color = Color.Gray.copy(alpha = 0.2f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 24.sp)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}

@Composable
private fun DailyRow(daily: DailyTotal, goal: Int, primaryColor: Color) {
    val date = try {
        LocalDate.parse(daily.date)
    } catch (e: Exception) { null }

    val dateStr = date?.format(DateTimeFormatter.ofPattern("MM/dd")) ?: daily.date
    val dayOfWeek = date?.dayOfWeek?.value ?: 0
    val dayName = when (dayOfWeek) {
        1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"
        5 -> "周五"; 6 -> "周六"; 7 -> "周日"; else -> ""
    }
    val percent = if (goal > 0) (daily.total * 100f / goal).toInt().coerceAtMost(100) else 0
    val emoji = when {
        percent >= 100 -> "✅"
        percent >= 75 -> "😊"
        percent >= 50 -> "😐"
        percent > 0 -> "😔"
        else -> "🌵"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "$dateStr $dayName",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            // 进度条
            LinearProgressIndicator(
                progress = (percent / 100f).coerceIn(0f, 1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .padding(top = 2.dp),
                color = primaryColor,
                trackColor = primaryColor.copy(alpha = 0.2f),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "${daily.total}ml",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = primaryColor,
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End
        )
        Text(
            "$percent%",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun BarChart(
    data: List<DailyTotal>,
    goal: Int,
    primaryColor: Color,
    lightColor: Color,
    modifier: Modifier = Modifier
) {
    val maxValue = (maxOf(data.maxOf { it.total }, goal) * 1.2f).toInt()

    Canvas(modifier = modifier.horizontalScroll(rememberScrollState())) {
        val chartWidth = maxOf(size.width, data.size * 36f)
        val chartHeight = size.height - 30f
        val barWidth = 20f
        val gap = (chartWidth - 16f) / data.size

        // 目标线
        val goalY = chartHeight * (1f - goal.toFloat() / maxValue)
        drawLine(
            color = Color.Red.copy(alpha = 0.4f),
            start = Offset(0f, goalY),
            end = Offset(chartWidth, goalY),
            strokeWidth = 2f,
            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
        )

        // 柱子
        data.forEachIndexed { index, daily ->
            val barHeight = (daily.total.toFloat() / maxValue) * chartHeight
            val x = 8f + index * gap + (gap - barWidth) / 2
            val y = chartHeight - barHeight

            // 柱子
            drawRoundRect(
                color = if (daily.total >= goal) primaryColor else lightColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // 日期标签
            val day = daily.date.substring(5) // MM-dd
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    day.substring(5), // dd
                    x + barWidth / 2,
                    size.height - 2f,
                    android.graphics.Paint().apply {
                        textSize = 20f
                        color = android.graphics.Color.GRAY
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}
