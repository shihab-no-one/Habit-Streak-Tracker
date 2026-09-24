package com.example.data.repository

import com.example.data.db.DailyCheckDao
import com.example.data.db.HabitDao
import com.example.data.model.DailyCheck
import com.example.data.model.Habit
import kotlinx.coroutines.flow.Flow

class HabitRepository(
    private val habitDao: HabitDao,
    private val dailyCheckDao: DailyCheckDao
) {
    val activeHabits: Flow<List<Habit>> = habitDao.getActiveHabits()
    val allHabits: Flow<List<Habit>> = habitDao.getAllHabits()

    fun getChecksForDate(date: String): Flow<List<DailyCheck>> =
        dailyCheckDao.getChecksForDate(date)

    fun getChecksForMonth(yearMonthPrefix: String): Flow<List<DailyCheck>> =
        dailyCheckDao.getChecksForMonth(yearMonthPrefix)

    fun getAllChecks(): Flow<List<DailyCheck>> =
        dailyCheckDao.getAllChecks()

    suspend fun addHabit(name: String, todayDate: String): Long {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return -1L
        val habit = Habit(
            name = trimmed,
            createdAt = todayDate,
            archivedAt = null,
            sortOrder = habitDao.getHabitCount()
        )
        return habitDao.insertHabit(habit)
    }

    suspend fun renameHabit(habitId: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        val existing = habitDao.getHabitById(habitId) ?: return
        habitDao.updateHabit(existing.copy(name = trimmed))
    }

    suspend fun archiveHabit(habitId: Long, archivedAtDate: String) {
        habitDao.archiveHabit(habitId, archivedAtDate)
    }

    suspend fun updateHabitOrder(reorderedHabits: List<Habit>) {
        val updated = reorderedHabits.mapIndexed { index, habit ->
            habit.copy(sortOrder = index)
        }
        habitDao.updateHabits(updated)
    }

    suspend fun toggleCheck(habitId: Long, date: String): Boolean {
        val current = dailyCheckDao.getCheck(habitId, date)
        val newCompleted = !(current?.completed ?: false)
        val updated = DailyCheck(
            id = current?.id ?: 0,
            habitId = habitId,
            date = date,
            completed = newCompleted
        )
        dailyCheckDao.insertOrUpdateCheck(updated)
        return newCompleted
    }

    suspend fun setCheck(habitId: Long, date: String, completed: Boolean) {
        val current = dailyCheckDao.getCheck(habitId, date)
        val check = DailyCheck(
            id = current?.id ?: 0,
            habitId = habitId,
            date = date,
            completed = completed
        )
        dailyCheckDao.insertOrUpdateCheck(check)
    }

    suspend fun countHabits(): Int = habitDao.getHabitCount()
}
