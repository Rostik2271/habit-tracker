package com.rostik2271.habittracker.util

import com.rostik2271.habittracker.data.Completion
import java.time.LocalDate

fun calculateCurrentStreak(completedDates: Set<LocalDate>, today: LocalDate = LocalDate.now()): Int {
    var streak = 0
    var day = today
    while (completedDates.contains(day)) {
        streak++
        day = day.minusDays(1)
    }
    if (streak == 0 && completedDates.contains(today.minusDays(1))) {
        day = today.minusDays(1)
        while (completedDates.contains(day)) {
            streak++
            day = day.minusDays(1)
        }
    }
    return streak
}

fun calculateBestStreak(completedDates: Set<LocalDate>): Int {
    if (completedDates.isEmpty()) return 0
    val sorted = completedDates.sorted()
    var best = 1
    var current = 1
    for (i in 1 until sorted.size) {
        if (sorted[i] == sorted[i - 1].plusDays(1)) {
            current++
            if (current > best) best = current
        } else {
            current = 1
        }
    }
    return best
}

fun getStreakOnDate(completedDates: Set<LocalDate>, date: LocalDate): Int {
    var streak = 0
    var day = date
    while (completedDates.contains(day)) {
        streak++
        day = day.minusDays(1)
    }
    return streak
}

fun completionsToDates(completions: List<Completion>): Set<LocalDate> =
    completions.map { LocalDate.parse(it.date) }.toSet()

fun pluralDays(n: Int): String {
    val mod10 = n % 10
    val mod100 = n % 100
    return when {
        mod100 in 11..14 -> "дней"
        mod10 == 1 -> "день"
        mod10 in 2..4 -> "дня"
        else -> "дней"
    }
}
