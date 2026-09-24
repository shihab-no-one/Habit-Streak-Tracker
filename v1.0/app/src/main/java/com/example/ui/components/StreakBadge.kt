package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.StreakCalculator
import com.example.ui.theme.StreakGreenDarkBg
import com.example.ui.theme.StreakGreenDarkText
import com.example.ui.theme.StreakGreenLightBg
import com.example.ui.theme.StreakGreenLightText
import com.example.ui.theme.StreakPurpleDarkBg
import com.example.ui.theme.StreakPurpleDarkText
import com.example.ui.theme.StreakPurpleLightBg
import com.example.ui.theme.StreakPurpleLightText
import com.example.ui.theme.StreakRedDarkBg
import com.example.ui.theme.StreakRedDarkText
import com.example.ui.theme.StreakRedLightBg
import com.example.ui.theme.StreakRedLightText
import com.example.ui.theme.StreakYellowDarkBg
import com.example.ui.theme.StreakYellowDarkText
import com.example.ui.theme.StreakYellowLightBg
import com.example.ui.theme.StreakYellowLightText

@Composable
fun StreakBadge(
    streak: Int,
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme()
) {
    val category = StreakCalculator.getCategory(streak)

    val (bgColor, textColor, borderColor) = when (category) {
        StreakCalculator.StreakCategory.ZERO -> {
            if (darkTheme) {
                Triple(StreakRedDarkBg, StreakRedDarkText, StreakRedDarkText.copy(alpha = 0.3f))
            } else {
                Triple(StreakRedLightBg, StreakRedLightText, StreakRedLightText.copy(alpha = 0.2f))
            }
        }
        StreakCalculator.StreakCategory.LOW -> {
            if (darkTheme) {
                Triple(StreakYellowDarkBg, StreakYellowDarkText, StreakYellowDarkText.copy(alpha = 0.3f))
            } else {
                Triple(StreakYellowLightBg, StreakYellowLightText, StreakYellowLightText.copy(alpha = 0.2f))
            }
        }
        StreakCalculator.StreakCategory.MEDIUM -> {
            if (darkTheme) {
                Triple(StreakPurpleDarkBg, StreakPurpleDarkText, StreakPurpleDarkText.copy(alpha = 0.3f))
            } else {
                Triple(StreakPurpleLightBg, StreakPurpleLightText, StreakPurpleLightText.copy(alpha = 0.2f))
            }
        }
        StreakCalculator.StreakCategory.HIGH -> {
            if (darkTheme) {
                Triple(StreakGreenDarkBg, StreakGreenDarkText, StreakGreenDarkText.copy(alpha = 0.3f))
            } else {
                Triple(StreakGreenLightBg, StreakGreenLightText, StreakGreenLightText.copy(alpha = 0.2f))
            }
        }
    }

    Box(
        modifier = modifier
            .testTag("streak_badge_$streak")
            .defaultMinSize(minWidth = 52.dp, minHeight = 34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$streak",
            color = textColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
