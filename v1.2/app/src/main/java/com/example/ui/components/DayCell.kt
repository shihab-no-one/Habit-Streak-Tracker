package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
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
import java.time.LocalDate

@Composable
fun DayCell(
    date: LocalDate,
    status: String?,
    isToday: Boolean,
    isDarkMode: Boolean,
    onCellClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)
    val isFuture = date.isAfter(LocalDate.now())

    val accentColor = if (isDarkMode) NordicDarkAccent else NordicLightAccent
    val freezeBg = if (isDarkMode) FreezeDarkBg else FreezeLightBg
    val freezeText = if (isDarkMode) FreezeDarkText else FreezeLightText
    val partialBg = if (isDarkMode) PartialDarkBg else PartialLightBg
    val partialText = if (isDarkMode) PartialDarkText else PartialLightText

    // Resolve visuals based on status
    val (bgColor, borderColor, isClickable) = when {
        isFuture -> Triple(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
            Color.Transparent,
            false
        )
        status == CheckStatus.COMPLETED -> Triple(
            accentColor,
            accentColor,
            true
        )
        status == CheckStatus.PARTIAL -> Triple(
            partialBg,
            partialText.copy(alpha = 0.6f),
            true
        )
        status == CheckStatus.FROZEN -> Triple(
            freezeBg,
            freezeText.copy(alpha = 0.6f),
            true
        )
        else -> Triple(
            // Missed or unmarked past date: fully interactive and ready to click!
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            if (isToday) accentColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
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
                width = if (isToday) 1.5.dp else 1.dp,
                color = borderColor,
                shape = shape
            )
            .then(
                if (isClickable) {
                    Modifier.clickable(onClick = onCellClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        when {
            isFuture -> {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
            status == CheckStatus.COMPLETED -> {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            status == CheckStatus.PARTIAL -> {
                Text(
                    text = "½",
                    color = partialText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            status == CheckStatus.FROZEN -> {
                Icon(
                    imageVector = Icons.Default.AcUnit,
                    contentDescription = "Frozen / Paused",
                    tint = freezeText,
                    modifier = Modifier.size(16.dp)
                )
            }
            else -> {
                // Empty circle/square for missed or unlogged past day
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(3.dp)
                        )
                )
            }
        }
    }
}
