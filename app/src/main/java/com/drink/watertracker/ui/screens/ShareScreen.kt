package com.drink.watertracker.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drink.watertracker.ui.MainViewModel
import com.drink.watertracker.ui.components.ShinchanBackground
import com.drink.watertracker.ui.theme.ShinchanTheme
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val total by viewModel.todayTotal.collectAsState()
    val goal by viewModel.dailyGoal.collectAsState()
    val bgUri by viewModel.backgroundUri.collectAsState()
    val bgBlur by viewModel.backgroundBlur.collectAsState()
    val dayTheme = ShinchanTheme.todayTheme()
    val percent = if (goal > 0) (total * 100f / goal).coerceAtMost(100f) else 0f
    val today = LocalDate.now()
    val dateStr = today.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))
    val dayName = when (today.dayOfWeek.value) {
        1 -> "周一"; 2 -> "周二"; 3 -> "周三"; 4 -> "周四"
        5 -> "周五"; 6 -> "周六"; 7 -> "周日"; else -> ""
    }

    var savedUri by remember { mutableStateOf<Uri?>(null) }
    var showSaved by remember { mutableStateOf(false) }

    ShinchanBackground(backgroundUri = bgUri, backgroundBlur = bgBlur) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("📤 分享今日", color = Color.White, fontWeight = FontWeight.Bold)
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
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 分享卡片预览
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 标题
                        Text(
                            "💧 今日饮水报告",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = dayTheme.primary
                        )

                        Spacer(Modifier.height(4.dp))
                        Text(
                            "$dateStr $dayName",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )

                        Spacer(Modifier.height(20.dp))

                        // 进度圆环
                        Box(
                            modifier = Modifier.size(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val strokeWidth = 16f
                                val radius = (size.minDimension - strokeWidth) / 2
                                val center = Offset(size.width / 2, size.height / 2)

                                // 背景圆环
                                drawCircle(
                                    color = Color(0xFFEEEEEE),
                                    radius = radius,
                                    center = center,
                                    style = Stroke(strokeWidth)
                                )

                                // 进度圆弧
                                drawArc(
                                    color = dayTheme.primary,
                                    startAngle = -90f,
                                    sweepAngle = 360f * (percent / 100f),
                                    useCenter = false,
                                    topLeft = Offset(center.x - radius, center.y - radius),
                                    size = Size(radius * 2, radius * 2),
                                    style = Stroke(
                                        width = strokeWidth,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                                    )
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "${percent.toInt()}%",
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = dayTheme.primary
                                )
                                Text(
                                    "${total}ml",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // 详情
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ShareStat("🎯", "目标", "${goal}ml")
                            ShareStat("💧", "已喝", "${total}ml")
                            ShareStat("📊", "剩余", "${(goal - total).coerceAtLeast(0)}ml")
                        }

                        Spacer(Modifier.height(16.dp))

                        // 评语
                        val comment = when {
                            percent >= 100 -> "🎉 太棒了！今天的喝水目标已达成！"
                            percent >= 75 -> "😊 快达标了，再喝一点就成功！"
                            percent >= 50 -> "💪 完成一半了，继续加油！"
                            percent >= 25 -> "🌱 良好的开始，保持节奏！"
                            else -> "🌵 记得多喝水哦，身体需要水分！"
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = dayTheme.primaryLight.copy(alpha = 0.3f)
                            )
                        ) {
                            Text(
                                comment,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                fontSize = 14.sp,
                                color = Color(0xFF555555),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // 底部
                        Text(
                            "— 喝水助手 —",
                            fontSize = 11.sp,
                            color = Color.Gray.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 保存图片
                    Button(
                        onClick = {
                            val bitmap = generateShareBitmap(context, total, goal, percent, dateStr, dayName, dayTheme.primary, dayTheme.primaryLight)
                            savedUri = saveBitmap(context, bitmap)
                            showSaved = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = dayTheme.primary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("💾 保存图片", modifier = Modifier.padding(vertical = 4.dp))
                    }

                    // 分享
                    Button(
                        onClick = {
                            val bitmap = generateShareBitmap(context, total, goal, percent, dateStr, dayName, dayTheme.primary, dayTheme.primaryLight)
                            val uri = saveBitmap(context, bitmap)
                            uri?.let { shareImage(context, it) }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Share, "分享", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("分享", modifier = Modifier.padding(vertical = 4.dp))
                    }
                }

                if (showSaved) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "✅ 图片已保存到相册",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ShareStat(emoji: String, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

private fun generateShareBitmap(
    context: Context, total: Int, goal: Int, percent: Float,
    dateStr: String, dayName: String, primaryColor: Color, lightColor: Color
): Bitmap {
    val width = 1080
    val height = 1920
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 背景渐变
    val bgPaint = Paint().apply {
        shader = LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            intArrayOf(
                android.graphics.Color.parseColor("#FFFF6F00"),
                android.graphics.Color.parseColor("#FFFF8F00"),
                android.graphics.Color.parseColor("#FFFFAB40")
            ),
            null, Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    val whitePaint = Paint().apply { color = android.graphics.Color.WHITE; isAntiAlias = true }
    val textPaint = Paint().apply {
        color = android.graphics.Color.WHITE; isAntiAlias = true; textSize = 64f
        typeface = android.graphics.Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
    }
    val subPaint = Paint().apply {
        color = android.graphics.Color.WHITE; isAntiAlias = true; textSize = 40f
        textAlign = Paint.Align.CENTER
    }
    val grayPaint = Paint().apply {
        color = android.graphics.Color.argb(180, 255, 255, 255); isAntiAlias = true; textSize = 36f
        textAlign = Paint.Align.CENTER
    }

    // 标题
    canvas.drawText("💧 今日饮水报告", width / 2f, 200f, textPaint)
    canvas.drawText("$dateStr $dayName", width / 2f, 260f, grayPaint)

    // 进度圆环
    val cx = width / 2f
    val cy = 550f
    val radius = 220f
    val ringPaint = Paint().apply {
        isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 28f
        color = android.graphics.Color.argb(60, 255, 255, 255)
    }
    canvas.drawCircle(cx, cy, radius, ringPaint)

    val progressPaint = Paint().apply {
        isAntiAlias = true; style = Paint.Style.STROKE; strokeWidth = 28f
        strokeCap = Paint.Cap.ROUND; color = android.graphics.Color.WHITE
    }
    val sweep = 360f * (percent / 100f)
    canvas.drawArc(cx - radius, cy - radius, cx + radius, cy + radius, -90f, sweep, false, progressPaint)

    // 百分比
    val pctPaint = Paint().apply {
        color = android.graphics.Color.WHITE; isAntiAlias = true; textSize = 100f
        typeface = android.graphics.Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
    }
    canvas.drawText("${percent.toInt()}%", cx, cy + 30f, pctPaint)
    canvas.drawText("${total}ml", cx, cy + 80f, subPaint)

    // 统计
    val statY = 880f
    val statPaint = Paint().apply {
        color = android.graphics.Color.WHITE; isAntiAlias = true; textSize = 48f
        typeface = android.graphics.Typeface.DEFAULT_BOLD; textAlign = Paint.Align.CENTER
    }
    canvas.drawText("🎯 目标", width * 0.2f, statY, grayPaint)
    canvas.drawText("${goal}ml", width * 0.2f, statY + 55f, statPaint)
    canvas.drawText("💧 已喝", width * 0.5f, statY, grayPaint)
    canvas.drawText("${total}ml", width * 0.5f, statY + 55f, statPaint)
    canvas.drawText("📊 剩余", width * 0.8f, statY, grayPaint)
    canvas.drawText("${(goal - total).coerceAtLeast(0)}ml", width * 0.8f, statY + 55f, statPaint)

    // 评语
    val comment = when {
        percent >= 100 -> "🎉 太棒了！今天的喝水目标已达成！"
        percent >= 75 -> "😊 快达标了，再喝一点就成功！"
        percent >= 50 -> "💪 完成一半了，继续加油！"
        percent >= 25 -> "🌱 良好的开始，保持节奏！"
        else -> "🌵 记得多喝水哦，身体需要水分！"
    }
    val commentBg = Paint().apply {
        color = android.graphics.Color.argb(40, 255, 255, 255); isAntiAlias = true
    }
    canvas.drawRoundRect(width * 0.1f, 1050f, width * 0.9f, 1130f, 24f, 24f, commentBg)
    canvas.drawText(comment, width / 2f, 1105f, subPaint)

    // 底部
    canvas.drawText("— 喝水助手 —", width / 2f, height - 150f, grayPaint)
    canvas.drawText("Powered by 薛定谔的猫 🐱", width / 2f, height - 100f, grayPaint)

    return bitmap
}

private fun saveBitmap(context: Context, bitmap: Bitmap): Uri? {
    val filename = "water_share_${System.currentTimeMillis()}.png"
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/喝水助手")
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        uri?.let {
            context.contentResolver.openOutputStream(it)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
        }
        uri
    } else {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "喝水助手")
        dir.mkdirs()
        val file = File(dir, filename)
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        Uri.fromFile(file)
    }
}

private fun shareImage(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "image/png"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "分享今日饮水"))
}
