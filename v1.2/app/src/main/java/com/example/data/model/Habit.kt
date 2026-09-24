package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: String, // ISO date "yyyy-MM-dd"
    val archivedAt: String? = null, // null if active, ISO date "yyyy-MM-dd" if archived
    val sortOrder: Int = 0,
    val isPaused: Boolean = false,
    val reminderTime: String? = null,
    val reminderText: String? = null
) {
    val isArchived: Boolean
        get() = archivedAt != null
}
