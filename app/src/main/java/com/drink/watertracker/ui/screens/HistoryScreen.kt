package com.drink.watertracker.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drink.watertracker.data.DailyTotal
import com.drink.watertracker.ui.MainViewModel
import com.drink.watertracker.ui.components.ShinchanBackground
import com.drink.watertracker.ui.theme.ShinchanTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val monthlyHistory by viewModel.monthlyHistory.collectAsState()
    val weeklyHistory by viewModel.weeklyHistory.collectAsState()
    val goal by viewModel.dailyGoal.collectAsState()
    val dayTheme = ShinchanTheme.todayTheme()

    val tabTitles = listOf("📊 日统计", "📈 周趋势", "📉 月趋势")
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()

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
            ) {
                // Tab 选择器
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            },
                            text = {
                                Text(
                                    title,
                                    fontSize = 13.sp,
                                    fontWeight = if (pagerState.currentPage == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (pagerState.currentPage == index) Color.White else Color.White.copy(alpha = 0.6f)
                                )
                            }
                        )
                    }
                }

                // 统计概览卡片
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
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

                Spacer(Modifier.height(8.dp))

                // Pager 内容
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (page) {
                        0 -> DailyTab(monthlyHistory, goal, dayTheme)
                        1 -> WeeklyTab(weeklyHistory, goal, dayTheme)
                        2 -> MonthlyTab(monthlyHistory, goal, dayTheme)
                    }
                }
            }
        }
    }
}

// ==================== 日统计 Tab ====================
@Composable
private fun DailyTab(
    monthlyHistory: List<DailyTotal>,
    goal: Int,
    dayTheme: com.drink.watertracker.ui.theme.DayTheme
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
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
                    EmptyChart()
                } else {
                    BarChart(
                        data = monthlyHistory,
                        goal = goal,
                        primaryColor = dayTheme.primary,
                        lightColor = dayTheme.primaryLight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )
                    ChartLegend(dayTheme.primary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 每日详情
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

        Spacer(Modifier.height(16.dp))
    }
}

// ==================== 周趋势 Tab ====================
@Composable
private fun WeeklyTab(
    weeklyHistory: List<DailyTotal>,
    goal: Int,
    dayTheme: com.drink.watertracker.ui.theme.DayTheme
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 周趋势折线图
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "本周饮水趋势",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))

                val weekTotal = weeklyHistory.sumOf { it.total }
                val weekAvg = if (weeklyHistory.isNotEmpty()) weekTotal / weeklyHistory.size else 0
                val goalDays = weeklyHistory.count { it.total >= goal }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeekStatChip("💧", "本周总量", "${weekTotal}ml", dayTheme.primary)
                    WeekStatChip("📊", "日均", "${weekAvg}ml", dayTheme.primaryLight)
                    WeekStatChip("🎯", "达标", "${goalDays}/${weeklyHistory.size}天", dayTheme.gradientEnd)
                }

                Spacer(Modifier.height(12.dp))

                if (weeklyHistory.isEmpty()) {
                    EmptyChart()
                } else {
                    LineChart(
                        data = weeklyHistory,
                        goal = goal,
                        primaryColor = dayTheme.primary,
                        lightColor = dayTheme.primaryLight,
                        gradientStart = dayTheme.gradientStart,
                        showArea = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    ChartLegend(dayTheme.primary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 本周每日卡片
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "本周每日",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))

                // 填充7天数据
                val today = LocalDate.now()
                val weekData = (0..6).map { offset ->
                    val date = today.minusDays((6 - offset).toLong())
                    val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    val record = weeklyHistory.find { it.date == dateStr }
                    Triple(date, record?.total ?: 0, offset)
                }

                weekData.forEach { (date, total, _) ->
                    val percent = if (goal > 0) (total * 100f / goal).toInt().coerceAtMost(100) else 0
                    val dayName = when (date.dayOfWeek.value) {
                        1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"
                        5 -> "周五"; 6 -> "周六"; 7 -> "周日"; else -> ""
                    }
                    val dateLabel = date.format(DateTimeFormatter.ofPattern("MM/dd"))
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
                                "$dateLabel $dayName",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            LinearProgressIndicator(
                                progress = (percent / 100f).coerceIn(0f, 1f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .padding(top = 2.dp),
                                color = dayTheme.primary,
                                trackColor = dayTheme.primary.copy(alpha = 0.2f),
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${total}ml",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = dayTheme.primary,
                            modifier = Modifier.width(60.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ==================== 月趋势 Tab ====================
@Composable
private fun MonthlyTab(
    monthlyHistory: List<DailyTotal>,
    goal: Int,
    dayTheme: com.drink.watertracker.ui.theme.DayTheme
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 月趋势折线图
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
                Spacer(Modifier.height(4.dp))

                val monthTotal = monthlyHistory.sumOf { it.total }
                val monthAvg = if (monthlyHistory.isNotEmpty()) monthTotal / monthlyHistory.size else 0
                val goalDays = monthlyHistory.count { it.total >= goal }
                val maxDay = monthlyHistory.maxByOrNull { it.total }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeekStatChip("💧", "月总量", "${monthTotal / 1000}L", dayTheme.primary)
                    WeekStatChip("📊", "日均", "${monthAvg}ml", dayTheme.primaryLight)
                    WeekStatChip("🎯", "达标", "${goalDays}天", dayTheme.gradientEnd)
                }

                if (maxDay != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "🏆 最佳一天：${maxDay.date.substring(5)} 喝了 ${maxDay.total}ml",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(12.dp))

                if (monthlyHistory.isEmpty()) {
                    EmptyChart()
                } else {
                    LineChart(
                        data = monthlyHistory,
                        goal = goal,
                        primaryColor = dayTheme.primary,
                        lightColor = dayTheme.primaryLight,
                        gradientStart = dayTheme.gradientStart,
                        showArea = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    ChartLegend(dayTheme.primary)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 月度热力图
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.9f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "月度热力图",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "颜色越深 = 喝得越多",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(8.dp))

                HeatmapGrid(
                    data = monthlyHistory,
                    goal = goal,
                    primaryColor = dayTheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // 每日详情
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

        Spacer(Modifier.height(16.dp))
    }
}

// ==================== 通用组件 ====================

@Composable
private fun WeekStatChip(emoji: String, label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(emoji, fontSize = 16.sp)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, fontSize = 10.sp, color = Color.Gray)
    }
}

@Composable
private fun ChartLegend(primaryColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = primaryColor)
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

@Composable
private fun EmptyChart() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("暂无数据，开始记录吧！", color = Color.Gray)
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

// ==================== 图表组件 ====================

@Composable
private fun LineChart(
    data: List<DailyTotal>,
    goal: Int,
    primaryColor: Color,
    lightColor: Color,
    gradientStart: Color,
    showArea: Boolean,
    modifier: Modifier = Modifier
) {
    val maxValue = (maxOf(data.maxOf { it.total }, goal) * 1.2f).toInt()

    Canvas(modifier = modifier.horizontalScroll(rememberScrollState())) {
        val chartWidth = maxOf(size.width, data.size * 60f)
        val chartHeight = size.height - 30f
        val gap = (chartWidth - 16f) / (data.size.coerceAtLeast(1))
        val points = data.mapIndexed { index, daily ->
            val x = 8f + index * gap + gap / 2
            val y = chartHeight * (1f - daily.total.toFloat() / maxValue)
            Offset(x, y)
        }

        // 目标线
        val goalY = chartHeight * (1f - goal.toFloat() / maxValue)
        drawLine(
            color = Color.Red.copy(alpha = 0.4f),
            start = Offset(0f, goalY),
            end = Offset(chartWidth, goalY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
        )

        if (points.size >= 2) {
            // 面积填充
            if (showArea) {
                val areaPath = Path().apply {
                    moveTo(points.first().x, chartHeight)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, chartHeight)
                    close()
                }
                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.3f),
                            primaryColor.copy(alpha = 0.05f)
                        ),
                        startY = 0f,
                        endY = chartHeight
                    )
                )
            }

            // 平滑曲线
            val linePath = Path().apply {
                moveTo(points.first().x, points.first().y)
                for (i in 1 until points.size) {
                    val prev = points[i - 1]
                    val curr = points[i]
                    val cpX = (prev.x + curr.x) / 2
                    cubicTo(cpX, prev.y, cpX, curr.y, curr.x, curr.y)
                }
            }
            drawPath(
                path = linePath,
                color = primaryColor,
                style = Stroke(width = 3f)
            )

            // 数据点
            points.forEach { point ->
                drawCircle(color = Color.White, radius = 6f, center = point)
                drawCircle(color = primaryColor, radius = 4f, center = point)
            }
        }

        // X 轴标签
        data.forEachIndexed { index, daily ->
            val x = 8f + index * gap + gap / 2
            val label = daily.date.substring(5) // MM-dd
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    label,
                    x,
                    size.height - 2f,
                    android.graphics.Paint().apply {
                        textSize = 18f
                        color = android.graphics.Color.GRAY
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

@Composable
private fun HeatmapGrid(
    data: List<DailyTotal>,
    goal: Int,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val maxMl = data.maxOfOrNull { it.total } ?: goal

    // 生成最近35天（5周）的网格
    val totalCells = 35
    val cellSize = 40f
    val cellGap = 4f

    Column(modifier = modifier) {
        // 星期标签
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("一", "二", "三", "四", "五", "六", "日").forEach {
                Text(it, fontSize = 10.sp, color = Color.Gray, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
            }
        }

        Spacer(Modifier.height(4.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(((cellSize + cellGap) * 5 / 2.5f).dp)
        ) {
            val canvasWidth = size.width
            val actualCellSize = (canvasWidth - 6 * cellGap) / 7f
            val actualCellGap = cellGap

            for (i in 0 until totalCells) {
                val date = today.minusDays((totalCells - 1 - i).toLong())
                val dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val record = data.find { it.date == dateStr }
                val total = record?.total ?: 0

                val col = i % 7
                val row = i / 7

                val x = col * (actualCellSize + actualCellGap)
                val y = row * (actualCellSize + actualCellGap)

                val intensity = if (maxMl > 0) (total.toFloat() / maxMl).coerceIn(0f, 1f) else 0f
                val cellColor = when {
                    total == 0 -> Color(0xFFE8E8E8)
                    total >= goal -> primaryColor
                    else -> primaryColor.copy(alpha = 0.2f + intensity * 0.6f)
                }

                drawRoundRect(
                    color = cellColor,
                    topLeft = Offset(x, y),
                    size = Size(actualCellSize, actualCellSize),
                    cornerRadius = CornerRadius(6f, 6f)
                )

                // 日期数字
                val dayNum = date.dayOfMonth.toString()
                drawContext.canvas.nativeCanvas.apply {
                    drawText(
                        dayNum,
                        x + actualCellSize / 2,
                        y + actualCellSize / 2 + 5f,
                        android.graphics.Paint().apply {
                            textSize = 16f
                            color = if (total >= goal) android.graphics.Color.WHITE
                            else android.graphics.Color.DKGRAY
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
            }
        }

        // 图例
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("少", fontSize = 10.sp, color = Color.Gray)
            Spacer(Modifier.width(4.dp))
            listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { intensity ->
                Canvas(modifier = Modifier.size(14.dp)) {
                    drawRoundRect(
                        color = if (intensity == 0f) Color(0xFFE8E8E8)
                        else primaryColor.copy(alpha = 0.2f + intensity * 0.8f),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                }
                Spacer(Modifier.width(2.dp))
            }
            Spacer(Modifier.width(4.dp))
            Text("多", fontSize = 10.sp, color = Color.Gray)
        }
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
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 5f))
        )

        // 柱子
        data.forEachIndexed { index, daily ->
            val barHeight = (daily.total.toFloat() / maxValue) * chartHeight
            val x = 8f + index * gap + (gap - barWidth) / 2
            val y = chartHeight - barHeight

            drawRoundRect(
                color = if (daily.total >= goal) primaryColor else lightColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // 日期标签
            val day = daily.date.substring(5)
            drawContext.canvas.nativeCanvas.apply {
                drawText(
                    day.substring(5),
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
