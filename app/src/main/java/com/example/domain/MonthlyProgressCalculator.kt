package com.example.domain

import com.example.data.model.Habit
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

data class MonthlyProgressSummary(
    val yearMonth: YearMonth,
    val completedChecks: Int,
    val possibleChecks: Int,
    val percentage: Int
)

data class HabitMonthProgress(
    val habit: Habit,
    val completedCount: Int,
    val possibleCount: Int,
    val percentage: Int
)

object MonthlyProgressCalculator {

    /**
     * Calculates monthly summary statistics across all relevant habits for [targetMonth].
     *
     * @param habits List of habits that existed or were active during [targetMonth].
     * @param isCompleted Function checking if a given habitId was completed on date.
     * @param today Reference date for capping future days (defaults to LocalDate.now()).
     */
    fun calculateSummary(
        habits: List<Habit>,
        targetMonth: YearMonth,
        isCompleted: (habitId: Long, date: LocalDate) -> Boolean,
        today: LocalDate = LocalDate.now()
    ): MonthlyProgressSummary {
        val daysInMonth = targetMonth.lengthOfMonth()
        var completedChecks = 0
        var possibleChecks = 0

        for (day in 1..daysInMonth) {
            val date = targetMonth.atDay(day)

            // Future dates must never count toward progress
            if (date.isAfter(today)) {
                continue
            }

            for (habit in habits) {
                val createdAt = runCatching { LocalDate.parse(habit.createdAt) }.getOrDefault(date)
                val archivedAt = habit.archivedAt?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

                // Check if habit is active on this date:
                // 1. Must be on or after creation date
                // 2. If archived, must be on or before archived date
                val isActiveOnDate = !date.isBefore(createdAt) && (archivedAt == null || !date.isAfter(archivedAt))

                if (isActiveOnDate) {
                    possibleChecks++
                    if (isCompleted(habit.id, date)) {
                        completedChecks++
                    }
                }
            }
        }

        val percentage = if (possibleChecks > 0) {
            ((completedChecks.toDouble() / possibleChecks) * 100).roundToInt()
        } else {
            0
        }

        return MonthlyProgressSummary(
            yearMonth = targetMonth,
            completedChecks = completedChecks,
            possibleChecks = possibleChecks,
            percentage = percentage
        )
    }

    /**
     * Filters habits that were active at any point during [targetMonth]
     * so historical months preserve archived habits as per Case 8.
     */
    fun filterHabitsForMonth(habits: List<Habit>, targetMonth: YearMonth): List<Habit> {
        val startOfMonth = targetMonth.atDay(1)
        val endOfMonth = targetMonth.atEndOfMonth()

        return habits.filter { habit ->
            val createdAt = runCatching { LocalDate.parse(habit.createdAt) }.getOrDefault(startOfMonth)
            val archivedAt = habit.archivedAt?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

            // Created before or during the month
            val createdInTime = !createdAt.isAfter(endOfMonth)
            // Not archived before the month began
            val notArchivedBefore = archivedAt == null || !archivedAt.isBefore(startOfMonth)

            createdInTime && notArchivedBefore
        }
    }
}
