package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Habit
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthGrid(
    habits: List<Habit>,
    targetMonth: YearMonth,
    today: LocalDate,
    isCompleted: (habitId: Long, date: LocalDate) -> Boolean,
    onToggleDate: (habitId: Long, date: LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val daysInMonth = targetMonth.lengthOfMonth()
    val scrollState = rememberScrollState()

    val habitColumnWidth = 110.dp
    val cellSpacing = 6.dp

    Column(modifier = modifier.fillMaxWidth()) {
        // Sticky Header with Dates
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Corner header label
            Box(
                modifier = Modifier
                    .width(habitColumnWidth)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "Habit",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Scrollable Days Header
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (day in 1..daysInMonth) {
                    val date = targetMonth.atDay(day)
                    val isCurrentDay = date == today
                    val dayOfWeek = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2)

                    Column(
                        modifier = Modifier
                            .width(34.dp)
                            .padding(horizontal = 1.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$day",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isCurrentDay) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isCurrentDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = dayOfWeek,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                color = if (isCurrentDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(modifier = Modifier.width(cellSpacing))
                }
            }
        }

        // Habit Rows
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            items(habits, key = { it.id }) { habit ->
                val createdAt = runCatching { LocalDate.parse(habit.createdAt) }.getOrDefault(today)
                val archivedAt = habit.archivedAt?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Fixed Habit Name column
                        Box(
                            modifier = Modifier
                                .width(habitColumnWidth)
                                .padding(horizontal = 10.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = habit.name,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Horizontally scrollable day cells (synced with header)
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .horizontalScroll(scrollState),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (day in 1..daysInMonth) {
                                val date = targetMonth.atDay(day)
                                val isCurrentDay = date == today
                                val isFuture = date.isAfter(today)
                                val isBeforeCreation = date.isBefore(createdAt)
                                val isAfterArchival = archivedAt != null && date.isAfter(archivedAt)

                                val status = when {
                                    isFuture -> DayCellStatus.FUTURE
                                    isBeforeCreation || isAfterArchival -> DayCellStatus.NOT_EXISTED
                                    isCompleted(habit.id, date) -> DayCellStatus.COMPLETED
                                    else -> DayCellStatus.NOT_COMPLETED
                                }

                                DayCell(
                                    date = date,
                                    status = status,
                                    isToday = isCurrentDay,
                                    onToggle = { onToggleDate(habit.id, date) }
                                )

                                Spacer(modifier = Modifier.width(cellSpacing))
                            }
                        }
                    }
                }
            }
        }
    }
}
