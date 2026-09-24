package com.example

import com.example.data.model.Habit
import com.example.domain.MonthlyProgressCalculator
import com.example.domain.StreakCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class StreakCalculatorTest {

    private val today = LocalDate.of(2026, 9, 22)

    @Test
    fun `CASE 1 - three checks, one missed, two checks ending today should result in 2`() {
        // Sep 17: ✓, Sep 18: ✓, Sep 19: ✓, Sep 20: ✗, Sep 21: ✓, Sep 22: ✓
        val createdAt = today.minusDays(5)
        val checks = mapOf(
            today.minusDays(5) to true,
            today.minusDays(4) to true,
            today.minusDays(3) to true,
            today.minusDays(2) to false,
            today.minusDays(1) to true,
            today to true
        )

        val result = StreakCalculator.calculateStreak(
            createdAt = createdAt,
            isCompletedOnDate = { checks[it] ?: false },
            today = today
        )

        assertEquals(2, result)
    }

    @Test
    fun `CASE 2 - five consecutive checks ending today should result in 5`() {
        // Sep 18..22 all ✓
        val createdAt = today.minusDays(4)
        val checks = mapOf(
            today.minusDays(4) to true,
            today.minusDays(3) to true,
            today.minusDays(2) to true,
            today.minusDays(1) to true,
            today to true
        )

        val result = StreakCalculator.calculateStreak(
            createdAt = createdAt,
            isCompletedOnDate = { checks[it] ?: false },
            today = today
        )

        assertEquals(5, result)
    }

    @Test
    fun `CASE 3 - one missed, two checks ending today should result in 2`() {
        // Sep 20: ✗, Sep 21: ✓, Sep 22: ✓
        val createdAt = today.minusDays(2)
        val checks = mapOf(
            today.minusDays(2) to false,
            today.minusDays(1) to true,
            today to true
        )

        val result = StreakCalculator.calculateStreak(
            createdAt = createdAt,
            isCompletedOnDate = { checks[it] ?: false },
            today = today
        )

        assertEquals(2, result)
    }

    @Test
    fun `CASE 4 - today missed and yesterday checked should result in 0`() {
        // Sep 21: ✓, Sep 22: ✗
        val createdAt = today.minusDays(5)
        val checks = mapOf(
            today.minusDays(1) to true,
            today to false
        )

        val result = StreakCalculator.calculateStreak(
            createdAt = createdAt,
            isCompletedOnDate = { checks[it] ?: false },
            today = today
        )

        assertEquals(0, result)
    }

    @Test
    fun `CASE 5 - habit created today and checked today should result in 1`() {
        val createdAt = today
        val checks = mapOf(
            today to true
        )

        val result = StreakCalculator.calculateStreak(
            createdAt = createdAt,
            isCompletedOnDate = { checks[it] ?: false },
            today = today
        )

        assertEquals(1, result)
    }

    @Test
    fun `CASE 6 - habit created today and not checked today should result in 0`() {
        val createdAt = today
        val checks = mapOf(
            today to false
        )

        val result = StreakCalculator.calculateStreak(
            createdAt = createdAt,
            isCompletedOnDate = { checks[it] ?: false },
            today = today
        )

        assertEquals(0, result)
    }

    @Test
    fun `CASE 7 - future dates containing FALSE must NOT reduce today's streak`() {
        // Sep 21: ✓, Sep 22 (today): ✓, Sep 23 (future): ✗, Sep 24 (future): ✗
        val createdAt = today.minusDays(1)
        val checks = mapOf(
            today.minusDays(1) to true,
            today to true,
            today.plusDays(1) to false,
            today.plusDays(2) to false
        )

        val result = StreakCalculator.calculateStreak(
            createdAt = createdAt,
            isCompletedOnDate = { checks[it] ?: false },
            today = today
        )

        assertEquals(2, result)
    }

    @Test
    fun `CASE 8 - archived habit preserves its historical monthly data`() {
        // Habit created in August, archived on Sep 15
        val habit = Habit(
            id = 1L,
            name = "Workout",
            createdAt = "2026-08-01",
            archivedAt = "2026-09-15",
            sortOrder = 0
        )

        val habits = listOf(habit)

        // Historical month: August 2026
        val augustMonth = YearMonth.of(2026, 8)
        val augustHabits = MonthlyProgressCalculator.filterHabitsForMonth(habits, augustMonth)
        assertTrue("Archived habit should be included in August history", augustHabits.contains(habit))

        // Current month: September 2026
        val septemberMonth = YearMonth.of(2026, 9)
        val septemberHabits = MonthlyProgressCalculator.filterHabitsForMonth(habits, septemberMonth)
        assertTrue("Archived habit should still be included in September history for the days it was active", septemberHabits.contains(habit))

        // Check calculation for September: habit active Sep 1..15
        val summary = MonthlyProgressCalculator.calculateSummary(
            habits = septemberHabits,
            targetMonth = septemberMonth,
            isCompleted = { _, date ->
                // Check all days from 1 to 10 completed
                date.dayOfMonth in 1..10
            },
            today = today // Sep 22
        )

        // Days 1..15 are active (15 days total possible for this habit)
        assertEquals(15, summary.possibleChecks)
        assertEquals(10, summary.completedChecks)
        assertEquals(67, summary.percentage) // 10 / 15 * 100 = 66.66% -> 67%
    }
}
