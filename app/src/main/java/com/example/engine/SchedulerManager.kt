package com.example.engine

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.data.model.RepeatFrequency
import com.example.data.model.ScheduledMessageEntity
import com.example.receiver.AlarmReceiver
import java.util.Calendar

class SchedulerManager(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    fun scheduleMessage(schedule: ScheduledMessageEntity) {
        if (!schedule.isEnabled) {
            cancelSchedule(schedule.id)
            return
        }

        val triggerTime = computeNextTriggerTime(schedule)
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("schedule_id", schedule.id)
            putExtra("persona_id", schedule.personaId)
        }

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.toInt(),
            intent,
            flags
        )

        try {
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            }
        } catch (_: SecurityException) {
            // If exact alarm permission is restricted, fallback to inexact set
            alarmManager?.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelSchedule(scheduleId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            scheduleId.toInt(),
            intent,
            flags
        )
        alarmManager?.cancel(pendingIntent)
    }

    fun computeNextTriggerTime(schedule: ScheduledMessageEntity): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.hour)
            set(Calendar.MINUTE, schedule.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val repeat = try {
            RepeatFrequency.valueOf(schedule.repeatFrequency)
        } catch (_: Exception) {
            RepeatFrequency.DAILY
        }

        when (repeat) {
            RepeatFrequency.ONCE -> {
                if (target.before(now)) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            RepeatFrequency.DAILY -> {
                if (target.before(now)) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
            RepeatFrequency.WEEKLY -> {
                if (target.before(now)) {
                    target.add(Calendar.WEEK_OF_YEAR, 1)
                }
            }
            RepeatFrequency.CUSTOM_WEEKDAYS -> {
                // Find next matching day in daysOfWeekMask (bit 1=Sun, 2=Mon... 64=Sat)
                while (target.before(now) || !isDaySelected(target.get(Calendar.DAY_OF_WEEK), schedule.daysOfWeekMask)) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                }
            }
        }

        return target.timeInMillis
    }

    private fun isDaySelected(calendarDay: Int, mask: Int): Boolean {
        // calendarDay: Sun=1, Mon=2 ... Sat=7
        val bit = 1 shl (calendarDay - 1)
        return (mask and bit) != 0
    }
}
