package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.DailyCheck
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyCheckDao {
    @Query("SELECT * FROM daily_checks WHERE date = :date")
    fun getChecksForDate(date: String): Flow<List<DailyCheck>>

    @Query("SELECT * FROM daily_checks WHERE date = :date")
    suspend fun getChecksForDateSync(date: String): List<DailyCheck>

    @Query("SELECT * FROM daily_checks WHERE habitId = :habitId")
    fun getChecksForHabit(habitId: Long): Flow<List<DailyCheck>>

    @Query("SELECT * FROM daily_checks WHERE date LIKE :yearMonthPrefix || '%'")
    fun getChecksForMonth(yearMonthPrefix: String): Flow<List<DailyCheck>>

    @Query("SELECT * FROM daily_checks")
    fun getAllChecks(): Flow<List<DailyCheck>>

    @Query("SELECT * FROM daily_checks WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getCheck(habitId: Long, date: String): DailyCheck?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateCheck(check: DailyCheck)

    @Query("DELETE FROM daily_checks WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCheck(habitId: Long, date: String)
}
