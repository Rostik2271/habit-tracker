package com.rostik2271.habittracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rostik2271.habittracker.data.AppDatabase
import com.rostik2271.habittracker.data.Completion
import com.rostik2271.habittracker.data.Habit
import com.rostik2271.habittracker.repository.HabitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HabitRepository

    init {
        val dao = AppDatabase.getDatabase(application).habitDao()
        repository = HabitRepository(dao)
    }

    val allHabits: Flow<List<Habit>> = repository.getAllHabits()

    fun getCompletionsForHabit(habitId: Long): Flow<List<Completion>> =
        repository.getCompletionsForHabit(habitId)

    fun addHabitWithColor(name: String, color: Int) {
        viewModelScope.launch {
            repository.insertHabit(Habit(name = name.trim(), color = color))
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun toggleCompletion(habitId: Long, date: LocalDate = LocalDate.now()) {
        viewModelScope.launch {
            val dateStr = date.toString()
            val existing = repository.getCompletion(habitId, dateStr)
            if (existing != null) {
                repository.deleteCompletion(habitId, dateStr)
            } else {
                repository.insertCompletion(
                    Completion(habitId = habitId, date = dateStr)
                )
            }
        }
    }
}
