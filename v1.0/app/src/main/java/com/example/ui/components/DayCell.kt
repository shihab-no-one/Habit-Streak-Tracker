package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

enum class DayCellStatus {
    FUTURE,        // Date is in future (cannot be checked)
    NOT_EXISTED,   // Date is before habit creation or after archival
    COMPLETED,     // Active date and completed (✓)
    NOT_COMPLETED  // Active date and not completed (□)
}

@Composable
fun DayCell(
    date: LocalDate,
    status: DayCellStatus,
    isToday: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(6.dp)
    val checkGreen = Color(0xFF10B981)

    val (bgColor, borderColor, isClickable) = when (status) {
        DayCellStatus.FUTURE -> Triple(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            Color.Transparent,
            false
        )
        DayCellStatus.NOT_EXISTED -> Triple(
            Color.Transparent,
            Color.Transparent,
            false
        )
        DayCellStatus.COMPLETED -> Triple(
            checkGreen,
            checkGreen,
            true
        )
        DayCellStatus.NOT_COMPLETED -> Triple(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            true
        )
    }

    Box(
        modifier = modifier
            .size(34.dp)
            .testTag("day_cell_${date}")
            .clip(shape)
            .background(bgColor)
            .border(
                width = if (isToday && status == DayCellStatus.NOT_COMPLETED) 1.5.dp else 1.dp,
                color = borderColor,
                shape = shape
            )
            .then(
                if (isClickable) {
                    Modifier.clickable(onClick = onToggle)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            DayCellStatus.COMPLETED -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            DayCellStatus.NOT_COMPLETED -> {
                // Subtle square indicator for not completed
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            RoundedCornerShape(3.dp)
                        )
                )
            }
            DayCellStatus.NOT_EXISTED -> {
                Text(
                    text = "—",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                    fontSize = 12.sp
                )
            }
            DayCellStatus.FUTURE -> {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}
