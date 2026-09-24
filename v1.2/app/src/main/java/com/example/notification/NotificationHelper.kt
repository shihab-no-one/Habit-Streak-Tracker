package com.example.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.rollup.habitstreak.R
import java.util.Calendar

object NotificationHelper {
    const val CHANNEL_ID = "habit_reminders_channel"
    const val CHANNEL_NAME = "Habit Reminders"

    const val ACTION_SNOOZE = "com.example.habitstreak.ACTION_SNOOZE"
    const val ACTION_MARK_DONE = "com.example.habitstreak.ACTION_MARK_DONE"
    const val ACTION_TRIGGER_REMINDER = "com.example.habitstreak.ACTION_TRIGGER_REMINDER"

    const val EXTRA_HABIT_ID = "extra_habit_id"
    const val EXTRA_HABIT_NAME = "extra_habit_name"
    const val EXTRA_CUSTOM_TEXT = "extra_custom_text"
    const val EXTRA_SNOOZE_MINUTES = "extra_snooze_minutes"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminders and smart snooze for habit tracking"
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        context: Context,
        habitId: Long,
        habitName: String,
        customText: String?
    ) {
        createNotificationChannel(context)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = habitId.toInt()

        // Content intent to open the app
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Helper for snooze actions
        fun createSnoozeAction(minutes: Int): NotificationCompat.Action {
            val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = ACTION_SNOOZE
                putExtra(EXTRA_HABIT_ID, habitId)
                putExtra(EXTRA_HABIT_NAME, habitName)
                putExtra(EXTRA_CUSTOM_TEXT, customText)
                putExtra(EXTRA_SNOOZE_MINUTES, minutes)
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                (habitId * 1000 + minutes).toInt(),
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            return NotificationCompat.Action.Builder(
                0,
                "Snooze ${minutes}m",
                snoozePendingIntent
            ).build()
        }

        // Mark Done action
        val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(EXTRA_HABIT_ID, habitId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            (habitId * 1000 + 99).toInt(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val doneAction = NotificationCompat.Action.Builder(
            0,
            "Done ✓",
            donePendingIntent
        ).build()

        val displayText = if (!customText.isNullOrBlank()) {
            customText
        } else {
            "Time for your daily consistency: $habitName!"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(habitName)
            .setContentText(displayText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(displayText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(contentPendingIntent)
            .setAutoCancel(true)
            .addAction(doneAction)
            .addAction(createSnoozeAction(15))
            .addAction(createSnoozeAction(30))
            .addAction(createSnoozeAction(60))

        notificationManager.notify(notificationId, builder.build())
    }

    fun scheduleAlarm(
        context: Context,
        habitId: Long,
        habitName: String,
        customText: String?,
        triggerTimeMillis: Long
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_TRIGGER_REMINDER
            putExtra(EXTRA_HABIT_ID, habitId)
            putExtra(EXTRA_HABIT_NAME, habitName)
            putExtra(EXTRA_CUSTOM_TEXT, customText)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            }
        } catch (_: SecurityException) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
        }
    }
}
