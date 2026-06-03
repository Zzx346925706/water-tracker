package com.drink.watertracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drink.watertracker.ui.MainViewModel
import com.drink.watertracker.ui.components.ShinchanBackground
import com.drink.watertracker.ui.theme.ShinchanTheme

data class Badge(
    val id: String,
    val emoji: String,
    val name: String,
    val desc: String,
    val unlocked: Boolean,
    val progress: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val total by viewModel.todayTotal.collectAsState()
    val goal by viewModel.dailyGoal.collectAsState()
    val bgUri by viewModel.backgroundUri.collectAsState()
    val bgBlur by viewModel.backgroundBlur.collectAsState()
    val dayTheme = ShinchanTheme.todayTheme()

    // 从 ViewModel 获取成就数据
    val achievements by viewModel.achievements.collectAsState()

    ShinchanBackground(backgroundUri = bgUri, backgroundBlur = bgBlur) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("🏆 成就徽章", color = Color.White, fontWeight = FontWeight.Bold)
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
                    .padding(horizontal = 16.dp)
            ) {
                // 统计摘要
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
                        val unlocked = achievements.count { it.unlocked }
                        StatBadge("🏅", "已解锁", "$unlocked / ${achievements.size}")
                        StatBadge("🔥", "连续达标", "${viewModel.streakDays}天")
                        StatBadge("💧", "累计饮水", "${viewModel.totalMl / 1000}L")
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 徽章网格
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(achievements) { badge ->
                        BadgeCard(badge, dayTheme)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 24.sp)
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
private fun BadgeCard(badge: Badge, dayTheme: com.drink.watertracker.ui.theme.DayTheme) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (badge.unlocked) Color.White.copy(alpha = 0.95f)
            else Color.White.copy(alpha = 0.5f)
        ),
        modifier = Modifier.then(
            if (!badge.unlocked) Modifier.alpha(0.6f) else Modifier
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 徽章图标
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.unlocked) Brush.radialGradient(
                            colors = listOf(dayTheme.primaryLight, dayTheme.primary)
                        )
                        else Brush.radialGradient(
                            colors = listOf(Color.Gray.copy(alpha = 0.2f), Color.Gray.copy(alpha = 0.3f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    badge.emoji,
                    fontSize = 26.sp,
                    modifier = Modifier.then(
                        if (!badge.unlocked) Modifier.alpha(0.4f) else Modifier
                    )
                )
            }

            Spacer(Modifier.height(6.dp))

            Text(
                badge.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (badge.unlocked) Color(0xFF333333) else Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                badge.desc,
                fontSize = 9.sp,
                color = if (badge.unlocked) Color(0xFF666666) else Color.Gray.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 12.sp
            )

            if (badge.unlocked && badge.progress.isNotEmpty()) {
                Text(
                    badge.progress,
                    fontSize = 10.sp,
                    color = dayTheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            if (!badge.unlocked) {
                Text(
                    "🔒",
                    fontSize = 10.sp
                )
            }
        }
    }
}
