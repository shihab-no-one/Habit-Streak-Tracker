package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object CheckStatus {
    const val COMPLETED = "COMPLETED"
    const val PARTIAL = "PARTIAL"
    const val MISSED = "MISSED"
    const val FROZEN = "FROZEN"
}

@Entity(
    tableName = "daily_checks",
    indices = [
        Index(value = ["habitId", "date"], unique = true),
        Index(value = ["date"]),
        Index(value = ["habitId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Habit::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DailyCheck(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: String, // ISO date "yyyy-MM-dd"
    val completed: Boolean = true,
    val status: String = CheckStatus.COMPLETED
)
