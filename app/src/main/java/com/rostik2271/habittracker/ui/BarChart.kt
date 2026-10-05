package com.rostik2271.habittracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rostik2271.habittracker.data.Habit
import kotlin.math.max

@Composable
fun HabitStreakBarChart(
    habits: List<Habit>,
    streaks: Map<Long, Int>,
    modifier: Modifier = Modifier
) {
    if (habits.isEmpty()) return

    val maxStreak = max(1, streaks.values.maxOrNull() ?: 1)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Серии привычек",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            val chartWidth = size.width
            val chartHeight = size.height - 30.dp.toPx()

            val barCount = habits.size
            val barSpacing = 16.dp.toPx()
            val totalSpacing = barSpacing * (barCount + 1)
            val barWidth = (chartWidth - totalSpacing) / barCount

            val onSurfaceColor = Color(0xFFE0E0E0)
            val baselineY = chartHeight

            drawLine(
                color = onSurfaceColor.copy(alpha = 0.3f),
                start = Offset(0f, baselineY),
                end = Offset(chartWidth, baselineY),
                strokeWidth = 2f
            )

            habits.forEachIndexed { index, habit ->
                val streak = streaks[habit.id] ?: 0
                val barHeight = if (maxStreak > 0) {
                    (streak.toFloat() / maxStreak.toFloat()) * chartHeight
                } else 0f

                val x = barSpacing + index * (barWidth + barSpacing)
                val y = baselineY - barHeight

                if (barHeight > 0f) {
                    drawRect(
                        color = Color(habit.color),
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight)
                    )
                }

                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 28f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                drawContext.canvas.nativeCanvas.drawText(
                    streak.toString(),
                    x + barWidth / 2,
                    y - 8f,
                    paint
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            habits.forEach { habit ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = habit.name.take(8),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
