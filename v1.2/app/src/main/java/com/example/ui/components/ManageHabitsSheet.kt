package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Habit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageHabitsSheet(
    habits: List<Habit>,
    onDismiss: () -> Unit,
    onAddHabitClick: () -> Unit,
    onRenameHabit: (habitId: Long, newName: String) -> Unit,
    onArchiveHabit: (habitId: Long) -> Unit,
    onTogglePause: (habitId: Long) -> Unit,
    onSaveReminder: (habitId: Long, time: String?, text: String?) -> Unit,
    onTestNotification: (Habit) -> Unit,
    onMoveUp: (habitId: Long) -> Unit,
    onMoveDown: (habitId: Long) -> Unit,
    sheetState: SheetState
) {
    var habitToRename by remember { mutableStateOf<Habit?>(null) }
    var habitToArchive by remember { mutableStateOf<Habit?>(null) }
    var habitForReminder by remember { mutableStateOf<Habit?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Manage Habits",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    )
                    Text(
                        text = "Freeze, reminders, reorder, or archive",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("manage_sheet_close")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )

            // Add button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = onAddHabitClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("manage_add_habit_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Habit")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (habits.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No checklist items yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(habits, key = { _, h -> h.id }) { index, habit ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("manage_item_${habit.id}"),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Up/Down Reorder Buttons
                                    Column(verticalArrangement = Arrangement.Center) {
                                        IconButton(
                                            onClick = { onMoveUp(habit.id) },
                                            enabled = index > 0,
                                            modifier = Modifier
                                                .size(26.dp)
                                                .testTag("move_up_${habit.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowUpward,
                                                contentDescription = "Move Up",
                                                modifier = Modifier.size(16.dp),
                                                tint = if (index > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                            )
                                        }
                                        IconButton(
                                            onClick = { onMoveDown(habit.id) },
                                            enabled = index < habits.size - 1,
                                            modifier = Modifier
                                                .size(26.dp)
                                                .testTag("move_down_${habit.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ArrowDownward,
                                                contentDescription = "Move Down",
                                                modifier = Modifier.size(16.dp),
                                                tint = if (index < habits.size - 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Habit Name & badges
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = habit.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 16.sp
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (habit.isPaused) {
                                                Text(
                                                    text = "Paused ❄️ (Streak Protected)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            if (!habit.reminderTime.isNullOrBlank()) {
                                                Text(
                                                    text = "Alarm: ${habit.reminderTime}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }

                                    // Freeze / Pause Button
                                    IconButton(
                                        onClick = { onTogglePause(habit.id) },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("pause_button_${habit.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (habit.isPaused) Icons.Default.PlayArrow else Icons.Default.AcUnit,
                                            contentDescription = if (habit.isPaused) "Resume" else "Freeze/Pause",
                                            tint = if (habit.isPaused) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Reminder Button
                                    IconButton(
                                        onClick = { habitForReminder = habit },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("reminder_button_${habit.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Alarm,
                                            contentDescription = "Set Reminder",
                                            tint = if (!habit.reminderTime.isNullOrBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Rename Button
                                    IconButton(
                                        onClick = { habitToRename = habit },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("rename_button_${habit.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Rename",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    // Archive / Remove Button
                                    IconButton(
                                        onClick = { habitToArchive = habit },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("archive_button_${habit.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Remove",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Reminder Dialog
    habitForReminder?.let { habit ->
        HabitReminderDialog(
            habit = habit,
            onDismiss = { habitForReminder = null },
            onSaveReminder = { time, text ->
                onSaveReminder(habit.id, time, text)
                habitForReminder = null
            },
            onTestNotification = {
                onTestNotification(habit)
            }
        )
    }

    // Rename Dialog
    habitToRename?.let { habit ->
        EditHabitDialog(
            initialName = habit.name,
            onDismiss = { habitToRename = null },
            onConfirm = { newName ->
                onRenameHabit(habit.id, newName)
                habitToRename = null
            }
        )
    }

    // Archive Confirmation Dialog
    habitToArchive?.let { habit ->
        AlertDialog(
            onDismissRequest = { habitToArchive = null },
            title = { Text("Remove \"${habit.name}\"?") },
            text = {
                Text("This item will no longer appear on Today or Streaks. Historical check records and monthly progress will remain intact in the database.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onArchiveHabit(habit.id)
                        habitToArchive = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Remove")
                }
            },
            dismissButton = {
                TextButton(onClick = { habitToArchive = null }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}
