package com.drink.watertracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.drink.watertracker.ui.MainViewModel
import com.drink.watertracker.ui.components.ShinchanBackground
import com.drink.watertracker.ui.theme.ShinchanTheme
import com.drink.watertracker.worker.ReminderScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val goal by viewModel.dailyGoal.collectAsState()
    val interval by viewModel.reminderInterval.collectAsState()
    val enabled by viewModel.reminderEnabled.collectAsState()
    val bgUri by viewModel.backgroundUri.collectAsState()
    val bgBlur by viewModel.backgroundBlur.collectAsState()

    var goalText by remember(goal) { mutableStateOf(goal.toString()) }
    var intervalText by remember(interval) { mutableStateOf(interval.toString()) }
    var showBgPicker by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val dayTheme = ShinchanTheme.todayTheme()

    // 图片选择器
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedUri = it
            showBgPicker = true
        }
    }

    ShinchanBackground(backgroundUri = bgUri, backgroundBlur = bgBlur) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("⚙️ 设置", color = Color.White, fontWeight = FontWeight.Bold)
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
                // 每日目标
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🎯 每日目标", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = goalText,
                                onValueChange = { goalText = it.filter { c -> c.isDigit() } },
                                label = { Text("目标量") },
                                suffix = { Text("ml") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(12.dp))
                            Button(
                                onClick = {
                                    goalText.toIntOrNull()?.let {
                                        if (it > 0) viewModel.setDailyGoal(it)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = dayTheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) { Text("保存") }
                        }

                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(1000, 2000, 3000, 4000).forEach { g ->
                                FilterChip(
                                    selected = goal == g,
                                    onClick = {
                                        viewModel.setDailyGoal(g)
                                        goalText = g.toString()
                                    },
                                    label = {
                                        Box(
                                            modifier = Modifier.fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("${g / 1000}L", maxLines = 1)
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = dayTheme.primaryLight
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 自定义背景
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("🖼️ 自定义背景", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 默认背景
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(dayTheme.gradientStart)
                                    .border(
                                        width = if (bgUri.isEmpty()) 3.dp else 1.dp,
                                        color = if (bgUri.isEmpty()) dayTheme.primary else Color.Gray.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.setBackground("", 0f) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🎨", fontSize = 24.sp)
                                    Text("默认", fontSize = 10.sp, color = Color.White)
                                }
                                if (bgUri.isEmpty()) {
                                    Icon(
                                        Icons.Default.Check,
                                        "已选",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(16.dp)
                                            .background(dayTheme.primary, CircleShape)
                                            .padding(2.dp)
                                    )
                                }
                            }

                            // 自定义背景
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Gray.copy(alpha = 0.2f))
                                    .border(
                                        width = if (bgUri.isNotEmpty()) 3.dp else 1.dp,
                                        color = if (bgUri.isNotEmpty()) dayTheme.primary else Color.Gray.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { imagePicker.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                if (bgUri.isNotEmpty()) {
                                    Image(
                                        painter = rememberAsyncImagePainter(Uri.parse(bgUri)),
                                        contentDescription = "背景",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Icon(
                                        Icons.Default.Check,
                                        "已选",
                                        tint = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(16.dp)
                                            .background(dayTheme.primary, CircleShape)
                                            .padding(2.dp)
                                    )
                                } else {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("📷", fontSize = 24.sp)
                                        Text("相册", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        // 模糊开关
                        if (bgUri.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("模糊效果", style = MaterialTheme.typography.bodyLarge)
                                Switch(
                                    checked = bgBlur > 0f,
                                    onCheckedChange = {
                                        viewModel.setBackground(bgUri, if (it) 20f else 0f)
                                    },
                                    colors = SwitchDefaults.colors(checkedTrackColor = dayTheme.primary)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 提醒设置
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⏰ 喝水提醒", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Switch(
                                checked = enabled,
                                onCheckedChange = {
                                    viewModel.setReminderEnabled(it)
                                    if (it) ReminderScheduler.schedule(interval)
                                    else ReminderScheduler.cancel()
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = dayTheme.primary)
                            )
                        }

                        if (enabled) {
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("间隔", style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.width(8.dp))
                                OutlinedTextField(
                                    value = intervalText,
                                    onValueChange = { intervalText = it.filter { c -> c.isDigit() } },
                                    label = { Text("分钟") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        intervalText.toIntOrNull()?.let {
                                            if (it > 0) {
                                                viewModel.setReminderInterval(it)
                                                ReminderScheduler.schedule(it)
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = dayTheme.primary),
                                    shape = RoundedCornerShape(12.dp)
                                ) { Text("保存") }
                            }

                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(30, 60, 90, 120).forEach { m ->
                                    FilterChip(
                                        selected = interval == m,
                                        onClick = {
                                            viewModel.setReminderInterval(m)
                                            intervalText = m.toString()
                                            ReminderScheduler.schedule(m)
                                        },
                                        label = {
                                            Box(
                                                modifier = Modifier.fillMaxWidth(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("${m}分钟", fontSize = 11.sp, maxLines = 1)
                                            }
                                        },
                                        modifier = Modifier.weight(1f),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = dayTheme.primaryLight
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 关于
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("💖 关于", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎨", fontSize = 24.sp)
                                Text("每日主题", fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💧", fontSize = 24.sp)
                                Text("快速记录", fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("⏰", fontSize = 24.sp)
                                Text("智能提醒", fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📱", fontSize = 24.sp)
                                Text("桌面组件", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "每天不同颜色主题，陪你喝够水！",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                Text(
                    "Powered by 薛定谔的猫 🐱",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

                Text(
                    "V1.6.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // 背景预览弹窗
    if (showBgPicker && selectedUri != null) {
        BackgroundPickerDialog(
            uri = selectedUri!!,
            onDismiss = { showBgPicker = false },
            onConfirm = { uri, blur ->
                viewModel.setBackground(uri.toString(), blur)
                showBgPicker = false
            }
        )
    }
}

@Composable
private fun BackgroundPickerDialog(
    uri: Uri,
    onDismiss: () -> Unit,
    onConfirm: (Uri, Float) -> Unit
) {
    val dayTheme = ShinchanTheme.todayTheme()
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var blur by remember { mutableStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // 预览图片
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 60.dp, bottom = 140.dp)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(0.5f, 3f)
                            offsetX += pan.x
                            offsetY += pan.y
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "背景预览",
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                        .then(if (blur > 0f) Modifier.blur(blur.dp) else Modifier),
                    contentScale = ContentScale.Crop
                )
            }

            // 顶部栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "取消", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Text(
                    "调整背景",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onConfirm(uri, blur) }) {
                    Icon(Icons.Default.Check, "确认", tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }

            // 底部控制面板
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // 缩放
                    Text("缩放", fontWeight = FontWeight.Medium)
                    Slider(
                        value = scale,
                        onValueChange = { scale = it.coerceIn(0.5f, 3f) },
                        valueRange = 0.5f..3f,
                        colors = SliderDefaults.colors(
                            thumbColor = dayTheme.primary,
                            activeTrackColor = dayTheme.primary
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    // 模糊
                    Text("模糊", fontWeight = FontWeight.Medium)
                    Slider(
                        value = blur,
                        onValueChange = { blur = it.coerceIn(0f, 25f) },
                        valueRange = 0f..25f,
                        colors = SliderDefaults.colors(
                            thumbColor = dayTheme.primary,
                            activeTrackColor = dayTheme.primary
                        )
                    )

                    Spacer(Modifier.height(8.dp))

                    // 重置
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(
                            onClick = { scale = 1f; offsetX = 0f; offsetY = 0f },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("重置位置") }

                        OutlinedButton(
                            onClick = { blur = 20f },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("推荐模糊") }

                        Button(
                            onClick = { onConfirm(uri, blur) },
                            colors = ButtonDefaults.buttonColors(containerColor = dayTheme.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) { Text("使用此背景") }
                    }
                }
            }
        }
    }
}
