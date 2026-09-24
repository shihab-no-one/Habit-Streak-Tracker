package com.example.domain

import com.example.data.model.CheckStatus
import java.time.LocalDate

object StreakCalculator {

    /**
     * Calculates the current streak ending at [today].
     *
     * Rules:
     * - A streak is the number of consecutive completed days ending at [today].
     * - If today is FROZEN, streak is protected and preserved from prior days.
     * - If today's item is MISSED, current streak is 0.
     * - Moving backwards:
     *   - COMPLETED / PARTIAL: streak++
     *   - FROZEN: preserves streak, continues checking prior days without resetting to 0.
     *   - MISSED: tracking stops.
     * - Days prior to [createdAt] are not counted as missed days.
     * - Future dates after [today] are strictly ignored and never count.
     */
    fun calculateStreakWithStatus(
        createdAt: LocalDate,
        getStatusOnDate: (LocalDate) -> String?,
        today: LocalDate = LocalDate.now()
    ): Int {
        if (today.isBefore(createdAt)) {
            return 0
        }

        var currentDate = today
        var streak = 0
        var foundActiveDay = false

        // First handle today if it's frozen - walk backwards until we find non-frozen day
        while (!currentDate.isBefore(createdAt)) {
            val status = getStatusOnDate(currentDate)
            when (status) {
                CheckStatus.COMPLETED, CheckStatus.PARTIAL -> {
                    streak++
                    foundActiveDay = true
                    currentDate = currentDate.minusDays(1)
                    break
                }
                CheckStatus.FROZEN -> {
                    // Frozen day preserves streak, look backwards
                    currentDate = currentDate.minusDays(1)
                }
                else -> {
                    // Missed today
                    return 0
                }
            }
        }

        // Continue walking backwards
        while (!currentDate.isBefore(createdAt)) {
            val status = getStatusOnDate(currentDate)
            when (status) {
                CheckStatus.COMPLETED, CheckStatus.PARTIAL -> {
                    streak++
                    currentDate = currentDate.minusDays(1)
                }
                CheckStatus.FROZEN -> {
                    // Preserves streak across frozen period without breaking
                    currentDate = currentDate.minusDays(1)
                }
                else -> {
                    // Encountered a missed day, stop counting
                    break
                }
            }
        }

        return streak
    }

    /**
     * Calculates the current streak using boolean completion check.
     */
    fun calculateStreak(
        createdAt: LocalDate,
        isCompletedOnDate: (LocalDate) -> Boolean,
        today: LocalDate = LocalDate.now()
    ): Int {
        // If today is before creation date, streak is 0
        if (today.isBefore(createdAt)) {
            return 0
        }

        // Rule: If today is not completed, streak is immediately 0
        if (!isCompletedOnDate(today)) {
            return 0
        }

        // Today is completed, start with 1 and move backward
        var streak = 1
        var currentDate = today.minusDays(1)

        while (!currentDate.isBefore(createdAt)) {
            if (isCompletedOnDate(currentDate)) {
                streak++
                currentDate = currentDate.minusDays(1)
            } else {
                // Encountered a missed day, stop counting
                break
            }
        }

        return streak
    }

    /**
     * Overload taking a Set of completed dates.
     */
    fun calculateStreak(
        createdAt: LocalDate,
        completedDates: Set<LocalDate>,
        today: LocalDate = LocalDate.now()
    ): Int {
        return calculateStreak(
            createdAt = createdAt,
            isCompletedOnDate = { completedDates.contains(it) },
            today = today
        )
    }

    /**
     * Overload taking String dates in ISO "yyyy-MM-dd" format.
     */
    fun calculateStreak(
        createdAtString: String,
        completedDateStrings: Set<String>,
        todayString: String = LocalDate.now().toString()
    ): Int {
        val created = runCatching { LocalDate.parse(createdAtString) }.getOrDefault(LocalDate.now())
        val today = runCatching { LocalDate.parse(todayString) }.getOrDefault(LocalDate.now())
        return calculateStreak(
            createdAt = created,
            isCompletedOnDate = { completedDateStrings.contains(it.toString()) },
            today = today
        )
    }

    enum class StreakCategory {
        ZERO,       // 0 -> Red
        LOW,        // 1-2 -> Yellow
        MEDIUM,     // 3-5 -> Purple
        HIGH        // 6+ -> Green
    }

    fun getCategory(streak: Int): StreakCategory {
        return when {
            streak <= 0 -> StreakCategory.ZERO
            streak in 1..2 -> StreakCategory.LOW
            streak in 3..5 -> StreakCategory.MEDIUM
            else -> StreakCategory.HIGH
        }
    }
}
