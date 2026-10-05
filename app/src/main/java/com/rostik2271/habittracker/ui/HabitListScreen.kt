package com.rostik2271.habittracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rostik2271.habittracker.data.Completion
import com.rostik2271.habittracker.data.Habit
import com.rostik2271.habittracker.util.calculateCurrentStreak
import com.rostik2271.habittracker.util.completionsToDates
import com.rostik2271.habittracker.viewmodel.HabitViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

val habitColorPalette = listOf(
    0xFF2196F3.toInt(),
    0xFF4CAF50.toInt(),
    0xFFFFEB3B.toInt(),
    0xFFFF9800.toInt(),
    0xFFF44336.toInt(),
    0xFF9C27B0.toInt(),
    0xFFE91E63.toInt(),
    0xFF00BCD4.toInt()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    viewModel: HabitViewModel,
    onHabitClick: (Long) -> Unit,
    onMonthClick: () -> Unit
) {
    val habits by viewModel.allHabits.collectAsState(initial = emptyList())
    val allCompletions by viewModel.allCompletions.collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var weekStart by remember { mutableStateOf(getWeekStart(LocalDate.now())) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("HabitTracker", fontWeight = FontWeight.Bold)
                        Text(
                            LocalDate.now().format(
                                DateTimeFormatter.ofPattern("d MMMM yyyy, EEEE")
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onMonthClick) {
                        Icon(Icons.Default.DateRange, contentDescription = "Месяц")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Добавить")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (habits.isEmpty()) {
                EmptyState()
            } else {
                HabitListWithChart(
                    habits = habits,
                    allCompletions = allCompletions,
                    viewModel = viewModel,
                    weekStart = weekStart,
                    onWeekChange = { weekStart = it },
                    onHabitClick = onHabitClick
                )
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, color ->
                viewModel.addHabitWithColor(name, color)
                showAddDialog = false
            }
        )
    }
}

fun getWeekStart(date: LocalDate): LocalDate {
    val dayOfWeek = date.dayOfWeek.value
    return date.minusDays((dayOfWeek - 1).toLong())
}

@Composable
fun HabitListWithChart(
    habits: List<Habit>,
    allCompletions: List<Completion>,
    viewModel: HabitViewModel,
    weekStart: LocalDate,
    onWeekChange: (LocalDate) -> Unit,
    onHabitClick: (Long) -> Unit
) {
    val today = LocalDate.now()

    val weekDates = (0..6).map { weekStart.plusDays(it.toLong()) }

    val completionsByHabit: Map<Long, Set<LocalDate>> = habits.associate { habit ->
        habit.id to completionsToDates(
            allCompletions.filter { it.habitId == habit.id }
        )
    }

    val streaks: Map<Long, Int> = habits.associate { habit ->
        val dates = completionsByHabit[habit.id] ?: emptySet()
        habit.id to calculateCurrentStreak(dates, today)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        WeekPanel(
            weekDates = weekDates,
            completionsByHabit = completionsByHabit,
            onPrevious = { onWeekChange(weekStart.minusWeeks(1)) },
            onNext = { onWeekChange(weekStart.plusWeeks(1)) }
        )

        HabitStreakBarChart(
            habits = habits,
            streaks = streaks,
            modifier = Modifier.fillMaxWidth()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(habits, key = { it.id }) { habit ->
                HabitItem(
                    habit = habit,
                    viewModel = viewModel,
                    onClick = { onHabitClick(habit.id) }
                )
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun WeekPanel(
    weekDates: List<LocalDate>,
    completionsByHabit: Map<Long, Set<LocalDate>>,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val today = LocalDate.now()
    val dayLabels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Назад")
                }
                Text(
                    text = "${weekDates.first().format(DateTimeFormatter.ofPattern("d MMM"))} — ${weekDates.last().format(DateTimeFormatter.ofPattern("d MMM"))}",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onNext) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Вперёд")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                weekDates.forEachIndexed { index, date ->
                    val isToday = date == today
                    val anyCompleted = completionsByHabit.values.any { it.contains(date) }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = dayLabels[index],
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = date.dayOfMonth.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isToday)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    if (anyCompleted)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        Color.Transparent
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (anyCompleted)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HabitItem(
    habit: Habit,
    viewModel: HabitViewModel,
    onClick: () -> Unit
) {
    val today = LocalDate.now()
    val completions by viewModel
        .getCompletionsForHabit(habit.id)
        .collectAsState(initial = emptyList())

    val isCompletedToday = completions.any { it.date == today.toString() }
    val habitColor = Color(habit.color)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompletedToday)
                habitColor.copy(alpha = 0.35f)
            else
                habitColor.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.toggleCompletion(habit.id) }) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCompletedToday) habitColor
                            else Color.Transparent
                        )
                        .border(
                            width = 2.dp,
                            color = habitColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompletedToday) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Выполнено",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Пока нет привычек",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Нажми + чтобы добавить первую",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(habitColorPalette[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая привычка") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название привычки") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Цвет привычки",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(habitColorPalette) { color ->
                        val isSelected = color == selectedColor
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(color))
                                .border(
                                    width = if (isSelected) 3.dp else 0.dp,
                                    color = if (isSelected)
                                        MaterialTheme.colorScheme.onSurface
                                    else
                                        Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) onConfirm(name, selectedColor)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}
