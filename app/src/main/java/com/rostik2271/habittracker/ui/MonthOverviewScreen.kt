package com.rostik2271.habittracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rostik2271.habittracker.data.Completion
import com.rostik2271.habittracker.data.Habit
import com.rostik2271.habittracker.util.calculateCurrentStreak
import com.rostik2271.habittracker.util.completionsToDates
import com.rostik2271.habittracker.viewmodel.HabitViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthOverviewScreen(
    viewModel: HabitViewModel,
    onBack: () -> Unit
) {
    val habits by viewModel.allHabits.collectAsState(initial = emptyList())
    val allCompletions by viewModel.allCompletions.collectAsState(initial = emptyList())

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Обзор месяца", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MonthSwitcher(
                currentMonth = currentMonth,
                onPrevious = { currentMonth = currentMonth.minusMonths(1) },
                onNext = { currentMonth = currentMonth.plusMonths(1) }
            )

            val monthStart = currentMonth.atDay(1)
            val monthEnd = currentMonth.atEndOfMonth()

            val streaks: Map<Long, Int> = habits.associate { habit ->
                val dates = completionsToDates(
                    allCompletions.filter { it.habitId == habit.id }
                )
                habit.id to calculateCurrentStreak(dates)
            }

            HabitStreakBarChart(
                habits = habits,
                streaks = streaks,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Выполнено за ${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("ru"))}:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(habits, key = { it.id }) { habit ->
                    MonthHabitRow(
                        habit = habit,
                        allCompletions = allCompletions,
                        monthStart = monthStart,
                        monthEnd = monthEnd
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun MonthSwitcher(
    currentMonth: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Назад")
        }
        Text(
            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("ru"))} ${currentMonth.year}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Вперёд")
        }
    }
}

@Composable
fun MonthHabitRow(
    habit: Habit,
    allCompletions: List<Completion>,
    monthStart: LocalDate,
    monthEnd: LocalDate
) {
    val habitColor = Color(habit.color)

    val monthCompletions = allCompletions.filter {
        it.habitId == habit.id &&
                it.date >= monthStart.toString() &&
                it.date <= monthEnd.toString()
    }

    val totalDaysInMonth = monthEnd.dayOfMonth
    val completedCount = monthCompletions.size
    val percent = if (totalDaysInMonth > 0) {
        (completedCount * 100) / totalDaysInMonth
    } else 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = habitColor.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(habitColor)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$completedCount из $totalDaysInMonth дней",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = habitColor
            )
        }
    }
}
