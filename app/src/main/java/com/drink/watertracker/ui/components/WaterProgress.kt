package com.drink.watertracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drink.watertracker.ui.theme.ShinchanTheme

@Composable
fun WaterProgress(
    current: Int,
    goal: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (goal > 0) (current.toFloat() / goal).coerceIn(0f, 1f) else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(600),
        label = "progress"
    )

    val dayTheme = ShinchanTheme.todayTheme()
    val waterColor = dayTheme.primary
    val bgColor = dayTheme.surface

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = 16.dp.toPx()
            val padding = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)

            // 背景圆环
            drawArc(
                color = bgColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // 进度圆环
            drawArc(
                color = waterColor,
                startAngle = -90f,
                sweepAngle = animatedProgress * 360f,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            // 装饰小圆点
            if (animatedProgress > 0f) {
                val angle = (-90f + animatedProgress * 360f)
                val radians = Math.toRadians(angle.toDouble())
                val cx = padding + arcSize.width / 2 + (arcSize.width / 2) * cos(radians).toFloat()
                val cy = padding + arcSize.height / 2 + (arcSize.height / 2) * sin(radians).toFloat()
                drawCircle(
                    color = Color.White,
                    radius = 10f,
                    center = Offset(cx, cy)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${dayTheme.emoji}",
                fontSize = 32.sp
            )
            Text(
                text = "${current}ml",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "/ ${goal}ml",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = waterColor
            )
        }
    }
}

private fun cos(radians: Double): Double = kotlin.math.cos(radians)
private fun sin(radians: Double): Double = kotlin.math.sin(radians)
