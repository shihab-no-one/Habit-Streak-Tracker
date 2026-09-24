package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CheckStatus
import com.example.ui.theme.FreezeDarkBg
import com.example.ui.theme.FreezeDarkText
import com.example.ui.theme.FreezeLightBg
import com.example.ui.theme.FreezeLightText
import com.example.ui.theme.NordicDarkAccent
import com.example.ui.theme.NordicLightAccent
import com.example.ui.theme.PartialDarkBg
import com.example.ui.theme.PartialDarkText
import com.example.ui.theme.PartialLightBg
import com.example.ui.theme.PartialLightText
import com.example.ui.viewmodel.HabitDateEdit
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DateEditDialog(
    editData: HabitDateEdit,
    isDarkMode: Boolean,
    onStatusSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
    val formattedDate = editData.date.format(dateFormatter)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = editData.habit.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp).testTag("close_date_edit_dialog")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Update status for this date:",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Option 1: Completed
                StatusOptionRow(
                    title = "Completed",
                    subtitle = "Mark habit as fully completed (✓)",
                    icon = Icons.Default.Check,
                    isSelected = editData.currentStatus == CheckStatus.COMPLETED,
                    activeColor = if (isDarkMode) NordicDarkAccent else NordicLightAccent,
                    activeBg = if (isDarkMode) Color(0xFF042F2E) else Color(0xFFDCFCE7),
                    testTag = "status_option_completed",
                    onClick = { onStatusSelected(CheckStatus.COMPLETED) }
                )

                // Option 2: Partially Completed
                StatusOptionRow(
                    title = "Partially Completed",
                    subtitle = "Partial progress made (½)",
                    icon = Icons.Default.HourglassBottom,
                    isSelected = editData.currentStatus == CheckStatus.PARTIAL,
                    activeColor = if (isDarkMode) PartialDarkText else PartialLightText,
                    activeBg = if (isDarkMode) PartialDarkBg else PartialLightBg,
                    testTag = "status_option_partial",
                    onClick = { onStatusSelected(CheckStatus.PARTIAL) }
                )

                // Option 3: Missed
                StatusOptionRow(
                    title = "Missed",
                    subtitle = "Did not complete habit (○)",
                    icon = Icons.Default.Close,
                    isSelected = editData.currentStatus == CheckStatus.MISSED,
                    activeColor = MaterialTheme.colorScheme.error,
                    activeBg = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                    testTag = "status_option_missed",
                    onClick = { onStatusSelected(CheckStatus.MISSED) }
                )

                // Option 4: Frozen / Paused
                StatusOptionRow(
                    title = "Frozen / Paused",
                    subtitle = "Trip, illness, or break — preserves streak! (❄️)",
                    icon = Icons.Default.AcUnit,
                    isSelected = editData.currentStatus == CheckStatus.FROZEN,
                    activeColor = if (isDarkMode) FreezeDarkText else FreezeLightText,
                    activeBg = if (isDarkMode) FreezeDarkBg else FreezeLightBg,
                    testTag = "status_option_frozen",
                    onClick = { onStatusSelected(CheckStatus.FROZEN) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("date_edit_cancel_button")
            ) {
                Text("Cancel", color = MaterialTheme.colorScheme.primary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun StatusOptionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    activeBg: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, activeColor, RoundedCornerShape(12.dp))
                } else {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                }
            ),
        color = if (isSelected) activeBg.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
