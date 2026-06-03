package com.drink.watertracker.ui.components

import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.drink.watertracker.ui.theme.DayTheme
import com.drink.watertracker.ui.theme.ShinchanTheme
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ShinchanBackground(
    modifier: Modifier = Modifier,
    backgroundUri: String = "",
    backgroundBlur: Float = 0f,
    content: @Composable () -> Unit
) {
    val dayTheme = ShinchanTheme.todayTheme()

    // 动画：漂浮的装饰元素
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "float"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // 背景层
        if (backgroundUri.isNotEmpty()) {
            // 自定义背景图片
            Image(
                painter = rememberAsyncImagePainter(Uri.parse(backgroundUri)),
                contentDescription = "背景",
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (backgroundBlur > 0f) Modifier.blur(backgroundBlur.dp) else Modifier),
                contentScale = ContentScale.Crop
            )
            // 半透明遮罩
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
        } else {
            // 默认渐变背景
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                dayTheme.gradientStart,
                                dayTheme.gradientEnd,
                                dayTheme.background
                            )
                        )
                    )
            ) {
                // 装饰性背景元素
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawShinchanDecorations(dayTheme, floatOffset)
                }
            }
        }

        content()
    }
}

private fun DrawScope.drawShinchanDecorations(theme: DayTheme, animOffset: Float) {
    val w = size.width
    val h = size.height

    // 漂浮的星星 ✨
    val starPositions = listOf(
        Offset(w * 0.1f, h * 0.15f),
        Offset(w * 0.85f, h * 0.1f),
        Offset(w * 0.7f, h * 0.3f),
        Offset(w * 0.2f, h * 0.7f),
        Offset(w * 0.9f, h * 0.6f),
        Offset(w * 0.5f, h * 0.85f),
    )

    starPositions.forEachIndexed { index, base ->
        val phase = animOffset + index * 60f
        val yOffset = sin(Math.toRadians(phase.toDouble())).toFloat() * 15f
        val xOffset = cos(Math.toRadians(phase * 0.7).toDouble()).toFloat() * 10f
        val alpha = 0.15f + 0.1f * sin(Math.toRadians(phase * 0.5).toDouble()).toFloat()

        drawCircle(
            color = Color.White.copy(alpha = alpha),
            radius = 20f + index * 5f,
            center = base + Offset(xOffset, yOffset)
        )
    }

    // 圆形装饰 🎨
    val circlePositions = listOf(
        Offset(w * 0.05f, h * 0.4f),
        Offset(w * 0.95f, h * 0.25f),
        Offset(w * 0.3f, h * 0.9f),
        Offset(w * 0.75f, h * 0.75f),
    )

    circlePositions.forEachIndexed { index, base ->
        val phase = animOffset * 0.5f + index * 90f
        val yOffset = cos(Math.toRadians(phase.toDouble())).toFloat() * 20f
        val alpha = 0.08f + 0.05f * cos(Math.toRadians(phase * 0.3).toDouble()).toFloat()

        drawCircle(
            color = theme.primary.copy(alpha = alpha),
            radius = 40f + index * 15f,
            center = base + Offset(0f, yOffset)
        )
    }

    // 波浪线装饰
    val waveY = h * 0.92f
    for (i in 0..20) {
        val x = w * i / 20f
        val y = waveY + sin(Math.toRadians((animOffset * 2 + i * 30).toDouble())).toFloat() * 8f
        drawCircle(
            color = theme.primaryLight.copy(alpha = 0.2f),
            radius = 6f,
            center = Offset(x, y)
        )
    }
}

// 水滴动画组件
@Composable
fun AnimatedWaterDrop(
    modifier: Modifier = Modifier,
    color: Color = ShinchanTheme.ShincanBlue
) {
    val infiniteTransition = rememberInfiniteTransition(label = "drop")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Canvas(modifier = modifier.size(24.dp)) {
        val r = size.minDimension / 2 * scale
        // 水滴形状
        drawCircle(
            color = color.copy(alpha = 0.3f),
            radius = r,
            center = center
        )
        drawCircle(
            color = color.copy(alpha = 0.6f),
            radius = r * 0.6f,
            center = center
        )
    }
}
