package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.DailyCheck
import com.example.data.model.Habit
import com.example.data.repository.HabitRepository
import com.example.domain.MonthlyProgressCalculator
import com.example.domain.MonthlyProgressSummary
import com.example.domain.StreakCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    val today: LocalDate = LocalDate.now()
    val todayString: String = today.toString() // "yyyy-MM-dd"

    private val repository: HabitRepository

    init {
        val db = AppDatabase.getInstance(application)
        repository = HabitRepository(db.habitDao(), db.dailyCheckDao())
        viewModelScope.launch {
            if (repository.countHabits() == 0) {
                seedInitialData()
            }
        }
    }

    private suspend fun seedInitialData() {
        val habitNames = listOf(
            "Workout",
            "Fist",
            "Work",
            "Prayer",
            "To-Do",
            "Sober",
            "Clean",
            "Learn"
        )
        val startDate = today.minusDays(6).toString()
        for (name in habitNames) {
            val habitId = repository.addHabit(name, startDate)
            // Seed a few past checks so progress and streaks have visible data
            when (name) {
                "Workout" -> {
                    // 6-day streak ending today
                    for (i in 0..5) {
                        repository.setCheck(habitId, today.minusDays(i.toLong()).toString(), true)
                    }
                }
                "Fist" -> {
                    // Today incomplete (streak 0), yesterday complete
                    repository.setCheck(habitId, today.minusDays(1).toString(), true)
                }
                "Work", "Prayer", "Sober", "Clean", "Learn" -> {
                    // 2-day streak ending today
                    repository.setCheck(habitId, today.toString(), true)
                    repository.setCheck(habitId, today.minusDays(1).toString(), true)
                }
                "To-Do" -> {
                    // 4-day streak ending today
                    for (i in 0..3) {
                        repository.setCheck(habitId, today.minusDays(i.toLong()).toString(), true)
                    }
                }
            }
        }
    }

    val activeHabits: StateFlow<List<Habit>> = repository.activeHabits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allHabits: StateFlow<List<Habit>> = repository.allHabits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val allChecksFlow = repository.getAllChecks()

    val todayChecks: StateFlow<Map<Long, Boolean>> = repository.getChecksForDate(todayString)
        .combine(activeHabits) { checks, habits ->
            val map = mutableMapOf<Long, Boolean>()
            for (habit in habits) {
                map[habit.id] = false
            }
            for (check in checks) {
                map[check.habitId] = check.completed
            }
            map.toMap()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Current streaks for all active habits
    val habitStreaks: StateFlow<Map<Long, Int>> = combine(activeHabits, allChecksFlow) { habits, checks ->
        // Group checks by habitId
        val checksByHabit = checks.groupBy { it.habitId }
        val streaks = mutableMapOf<Long, Int>()

        for (habit in habits) {
            val habitChecks = checksByHabit[habit.id] ?: emptyList()
            val completedDates = habitChecks
                .filter { it.completed }
                .mapNotNull { runCatching { LocalDate.parse(it.date) }.getOrNull() }
                .toSet()

            val createdAt = runCatching { LocalDate.parse(habit.createdAt) }.getOrDefault(today)
            val streak = StreakCalculator.calculateStreak(
                createdAt = createdAt,
                completedDates = completedDates,
                today = today
            )
            streaks[habit.id] = streak
        }
        streaks.toMap()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // Monthly progress state
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    // Monthly checks map: Pair(habitId, "yyyy-MM-dd") -> Boolean
    val monthChecksMap: StateFlow<Map<Pair<Long, String>, Boolean>> = allChecksFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        ).combine(_selectedMonth) { checks, _ ->
            checks.associate { (it.habitId to it.date) to it.completed }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    // Summary for currently selected month
    val monthlySummary: StateFlow<MonthlyProgressSummary> = combine(
        allHabits,
        allChecksFlow,
        _selectedMonth
    ) { habits, checks, month ->
        val checksMap = checks.associate { (it.habitId to it.date) to it.completed }
        val relevantHabits = MonthlyProgressCalculator.filterHabitsForMonth(habits, month)

        MonthlyProgressCalculator.calculateSummary(
            habits = relevantHabits,
            targetMonth = month,
            isCompleted = { habitId, date ->
                checksMap[habitId to date.toString()] == true
            },
            today = today
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MonthlyProgressSummary(YearMonth.now(), 0, 0, 0)
    )

    fun previousMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun selectMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
    }

    fun toggleTodayCheck(habitId: Long) {
        viewModelScope.launch {
            repository.toggleCheck(habitId, todayString)
        }
    }

    fun toggleCheck(habitId: Long, date: LocalDate) {
        // Future dates cannot be checked
        if (date.isAfter(today)) return

        viewModelScope.launch {
            repository.toggleCheck(habitId, date.toString())
        }
    }

    fun addHabit(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.addHabit(trimmed, todayString)
        }
    }

    fun renameHabit(habitId: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.renameHabit(habitId, trimmed)
        }
    }

    fun archiveHabit(habitId: Long) {
        viewModelScope.launch {
            repository.archiveHabit(habitId, todayString)
        }
    }

    fun moveHabitUp(habitId: Long) {
        val currentList = activeHabits.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == habitId }
        if (index > 0) {
            val item = currentList.removeAt(index)
            currentList.add(index - 1, item)
            viewModelScope.launch {
                repository.updateHabitOrder(currentList)
            }
        }
    }

    fun moveHabitDown(habitId: Long) {
        val currentList = activeHabits.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == habitId }
        if (index >= 0 && index < currentList.size - 1) {
            val item = currentList.removeAt(index)
            currentList.add(index + 1, item)
            viewModelScope.launch {
                repository.updateHabitOrder(currentList)
            }
        }
    }

    fun seedSampleHabitsIfEmpty() {
        viewModelScope.launch {
            if (repository.countHabits() == 0) {
                val samples = listOf(
                    "Workout",
                    "Fist",
                    "Work",
                    "Prayer",
                    "To-Do",
                    "Sober",
                    "Clean",
                    "Learn"
                )
                for (name in samples) {
                    repository.addHabit(name, todayString)
                }
            }
        }
    }

    fun formatHeaderDate(): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.getDefault())
        return today.format(formatter)
    }

    fun formatMonthTitle(yearMonth: YearMonth): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        return yearMonth.format(formatter)
    }
}
