package com.rostik2271.habittracker.repository

import com.rostik2271.habittracker.data.Completion
import com.rostik2271.habittracker.data.Habit
import com.rostik2271.habittracker.data.HabitDao
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {

    fun getAllHabits(): Flow<List<Habit>> = habitDao.getAllHabits()

    suspend fun getHabitById(id: Long): Habit? = habitDao.getHabitById(id)

    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    fun getCompletionsForHabit(habitId: Long): Flow<List<Completion>> =
        habitDao.getCompletionsForHabit(habitId)

    suspend fun getCompletion(habitId: Long, date: String): Completion? =
        habitDao.getCompletion(habitId, date)

    suspend fun insertCompletion(completion: Completion): Long =
        habitDao.insertCompletion(completion)

    suspend fun deleteCompletion(habitId: Long, date: String) =
        habitDao.deleteCompletion(habitId, date)
}
