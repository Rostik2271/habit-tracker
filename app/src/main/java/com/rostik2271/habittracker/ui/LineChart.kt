package com.rostik2271.habittracker.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rostik2271.habittracker.util.getStreakOnDate
import java.time.LocalDate
import kotlin.math.max

@Composable
fun HabitStreakLineChart(
    habitColor: Int,
    completedDates: Set<LocalDate>,
    daysCount: Int = 30,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val startDate = today.minusDays((daysCount - 1).toLong())

    val dataPoints = (0 until daysCount).map { i ->
        val date = startDate.plusDays(i.toLong())
        val streak = getStreakOnDate(completedDates, date)
        date to streak
    }

    val maxStreak = max(1, dataPoints.maxOfOrNull { it.second } ?: 1)
    val habitColorCompose = Color(habitColor)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Серия за $daysCount дней",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            val chartWidth = size.width
            val chartHeight = size.height - 20.dp.toPx()
            val stepX = chartWidth / (daysCount - 1).toFloat()

            val gridColor = Color(0xFF555555)

            for (g in 0..3) {
                val y = chartHeight * g / 3f
                drawLine(
                    color = gridColor.copy(alpha = 0.3f),
                    start = Offset(0f, y),
                    end = Offset(chartWidth, y),
                    strokeWidth = 1f
                )
            }

            val path = Path()
            dataPoints.forEachIndexed { index, (_, streak) ->
                val x = index * stepX
                val y = chartHeight - (streak.toFloat() / maxStreak.toFloat()) * chartHeight

                if (index == 0) {
                    path.moveTo(x, y)
                } else {
                    path.lineTo(x, y)
                }
            }

            drawPath(
                path = path,
                color = habitColorCompose,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            dataPoints.forEachIndexed { index, (_, streak) ->
                val x = index * stepX
                val y = chartHeight - (streak.toFloat() / maxStreak.toFloat()) * chartHeight

                if (streak > 0) {
                    drawCircle(
                        color = habitColorCompose,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Высота линии = длина серии на этот день",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
