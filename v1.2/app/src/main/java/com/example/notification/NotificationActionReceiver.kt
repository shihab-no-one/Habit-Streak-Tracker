package com.example.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.db.AppDatabase
import com.example.data.model.CheckStatus
import com.example.data.model.DailyCheck
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra(NotificationHelper.EXTRA_HABIT_ID, -1L)
        val habitName = intent.getStringExtra(NotificationHelper.EXTRA_HABIT_NAME) ?: "Habit Reminder"
        val customText = intent.getStringExtra(NotificationHelper.EXTRA_CUSTOM_TEXT)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

        when (intent.action) {
            NotificationHelper.ACTION_TRIGGER_REMINDER -> {
                if (habitId > 0) {
                    NotificationHelper.showNotification(context, habitId, habitName, customText)
                }
            }

            NotificationHelper.ACTION_SNOOZE -> {
                val minutes = intent.getIntExtra(NotificationHelper.EXTRA_SNOOZE_MINUTES, 15)
                if (habitId > 0) {
                    notificationManager?.cancel(habitId.toInt())
                    val snoozeTime = System.currentTimeMillis() + (minutes * 60 * 1000L)
                    NotificationHelper.scheduleAlarm(context, habitId, habitName, customText, snoozeTime)
                }
            }

            NotificationHelper.ACTION_MARK_DONE -> {
                if (habitId > 0) {
                    notificationManager?.cancel(habitId.toInt())
                    val today = LocalDate.now().toString()
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = AppDatabase.getInstance(context)
                        val existing = db.dailyCheckDao().getCheck(habitId, today)
                        val check = DailyCheck(
                            id = existing?.id ?: 0,
                            habitId = habitId,
                            date = today,
                            completed = true,
                            status = CheckStatus.COMPLETED
                        )
                        db.dailyCheckDao().insertOrUpdateCheck(check)
                    }
                }
            }
        }
    }
}
