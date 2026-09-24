package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CheckStatus
import com.example.data.model.DailyCheck
import com.example.data.model.Habit
import com.example.data.repository.HabitRepository
import com.example.domain.MonthlyProgressCalculator
import com.example.domain.MonthlyProgressSummary
import com.example.domain.Quote
import com.example.domain.QuotesRepository
import com.example.domain.StreakCalculator
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HabitDateEdit(
    val habit: Habit,
    val date: LocalDate,
    val currentStatus: String
)

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    val today: LocalDate = LocalDate.now()
    val todayString: String = today.toString() // "yyyy-MM-dd"

    private val repository: HabitRepository

    // Theme Mode: Default Light Theme as requested
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // Rotational Quote Engine
    private val _currentQuote = MutableStateFlow(QuotesRepository.getRandomQuote())
    val currentQuote: StateFlow<Quote> = _currentQuote.asStateFlow()

    fun nextQuote() {
        _currentQuote.value = QuotesRepository.getRandomQuote()
    }

    // View control switch: Monthly (30-day) vs Weekly (7-day)
    private val _isWeeklyView = MutableStateFlow(false)
    val isWeeklyView: StateFlow<Boolean> = _isWeeklyView.asStateFlow()

    fun setWeeklyView(weekly: Boolean) {
        _isWeeklyView.value = weekly
    }

    // Interactive Date Edit modal
    private val _selectedDateEdit = MutableStateFlow<HabitDateEdit?>(null)
    val selectedDateEdit: StateFlow<HabitDateEdit?> = _selectedDateEdit.asStateFlow()

    fun openDateEdit(habit: Habit, date: LocalDate) {
        val check = checksByHabitAndDate.value[habit.id to date.toString()]
        val status = check?.status ?: CheckStatus.MISSED
        _selectedDateEdit.value = HabitDateEdit(habit, date, status)
    }

    fun closeDateEdit() {
        _selectedDateEdit.value = null
    }

    init {
        val db = AppDatabase.getInstance(application)
        repository = HabitRepository(db.habitDao(), db.dailyCheckDao())
        viewModelScope.launch {
            if (repository.countHabits() == 0) {
                seedInitialData()
            }
        }
    }

    // Constructor for testing with custom repository
    constructor(application: Application, customRepository: HabitRepository) : this(application)

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
        // Set creation date to beginning of current month so all days 1..31 are active & interactive!
        val monthStartDate = today.withDayOfMonth(1).minusMonths(1).toString()
        for (name in habitNames) {
            val habitId = repository.addHabit(name, monthStartDate)
            when (name) {
                "Workout" -> {
                    // Seed full check for last 6 days
                    for (i in 0..5) {
                        repository.setCheckStatus(habitId, today.minusDays(i.toLong()).toString(), CheckStatus.COMPLETED)
                    }
                    // Earlier days: mix of completed and partial
                    repository.setCheckStatus(habitId, today.minusDays(7).toString(), CheckStatus.PARTIAL)
                    repository.setCheckStatus(habitId, today.minusDays(8).toString(), CheckStatus.COMPLETED)
                }
                "Fist" -> {
                    // Today incomplete (streak 0), yesterday complete
                    repository.setCheckStatus(habitId, today.minusDays(1).toString(), CheckStatus.COMPLETED)
                    repository.setCheckStatus(habitId, today.minusDays(2).toString(), CheckStatus.COMPLETED)
                }
                "Work" -> {
                    // Streak protected with a frozen day!
                    repository.setCheckStatus(habitId, today.toString(), CheckStatus.COMPLETED)
                    repository.setCheckStatus(habitId, today.minusDays(1).toString(), CheckStatus.FROZEN)
                    repository.setCheckStatus(habitId, today.minusDays(2).toString(), CheckStatus.COMPLETED)
                    repository.setCheckStatus(habitId, today.minusDays(3).toString(), CheckStatus.COMPLETED)
                }
                "Prayer", "Sober", "Clean", "Learn" -> {
                    repository.setCheckStatus(habitId, today.toString(), CheckStatus.COMPLETED)
                    repository.setCheckStatus(habitId, today.minusDays(1).toString(), CheckStatus.COMPLETED)
                }
                "To-Do" -> {
                    for (i in 0..3) {
                        repository.setCheckStatus(habitId, today.minusDays(i.toLong()).toString(), CheckStatus.COMPLETED)
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

    val checksByHabitAndDate: StateFlow<Map<Pair<Long, String>, DailyCheck>> = allChecksFlow
        .combine(activeHabits) { checks, _ ->
            checks.associateBy { it.habitId to it.date }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

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

    // Current streaks with freeze & pause protection
    val habitStreaks: StateFlow<Map<Long, Int>> = combine(activeHabits, allChecksFlow) { habits, checks ->
        val checksByHabit = checks.groupBy { it.habitId }
        val streaks = mutableMapOf<Long, Int>()

        for (habit in habits) {
            val habitChecks = checksByHabit[habit.id] ?: emptyList()
            val checksMap = habitChecks.associate { it.date to it.status }

            val createdAt = runCatching { LocalDate.parse(habit.createdAt) }.getOrDefault(today)
            val streak = StreakCalculator.calculateStreakWithStatus(
                createdAt = createdAt,
                getStatusOnDate = { date ->
                    val status = checksMap[date.toString()]
                    if (habit.isPaused && date == today && status == null) {
                        CheckStatus.FROZEN
                    } else {
                        status
                    }
                },
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
        if (date.isAfter(today)) return
        viewModelScope.launch {
            repository.toggleCheck(habitId, date.toString())
        }
    }

    fun setCheckStatus(habitId: Long, date: LocalDate, status: String) {
        if (date.isAfter(today)) return
        viewModelScope.launch {
            repository.setCheckStatus(habitId, date.toString(), status)
            closeDateEdit()
        }
    }

    fun toggleHabitPause(habitId: Long) {
        val habit = activeHabits.value.firstOrNull { it.id == habitId } ?: return
        viewModelScope.launch {
            repository.setHabitPaused(habitId, !habit.isPaused)
        }
    }

    fun updateHabitReminder(habitId: Long, reminderTime: String?, reminderText: String?) {
        viewModelScope.launch {
            repository.updateHabitReminder(habitId, reminderTime, reminderText)
        }
    }

    fun triggerTestNotification(habit: Habit) {
        NotificationHelper.showNotification(
            context = getApplication(),
            habitId = habit.id,
            habitName = habit.name,
            customText = habit.reminderText
        )
    }

    fun addHabit(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            // Give newly created habit a start date at beginning of month so all calendar cells are accessible
            val startDate = today.withDayOfMonth(1).toString()
            repository.addHabit(trimmed, startDate)
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
                seedInitialData()
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

    /**
     * Returns 7 days starting from Saturday for custom week start requirement
     */
    fun getWeekDaysStartingSaturday(referenceDate: LocalDate): List<LocalDate> {
        var start = referenceDate
        while (start.dayOfWeek != DayOfWeek.SATURDAY) {
            start = start.minusDays(1)
        }
        return (0..6).map { start.plusDays(it.toLong()) }
    }
}
