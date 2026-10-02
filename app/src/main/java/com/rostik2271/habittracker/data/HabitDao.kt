package com.rostik2271.habittracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit?

    @Insert
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM completions WHERE habitId = :habitId")
    fun getCompletionsForHabit(habitId: Long): Flow<List<Completion>>

    @Query("SELECT * FROM completions WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getCompletion(habitId: Long, date: String): Completion?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCompletion(completion: Completion): Long

    @Query("DELETE FROM completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletion(habitId: Long, date: String)
}
