package com.example.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.local.AppDatabase
import com.example.data.model.ChatMessageEntity
import com.example.data.model.MessageType
import com.example.data.model.RepeatFrequency
import com.example.engine.SchedulerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val scheduleId = intent.getLongExtra("schedule_id", -1L)
        val personaId = intent.getLongExtra("persona_id", -1L)
        if (scheduleId == -1L || personaId == -1L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val dao = db.personaDao()
                val schedule = dao.getScheduledMessageByIdSync(scheduleId)
                val persona = dao.getPersonaByIdSync(personaId)

                if (schedule != null && schedule.isEnabled && persona != null) {
                    // 1. Post message to chat history
                    val msg = ChatMessageEntity(
                        personaId = personaId,
                        sender = "BOT",
                        messageType = schedule.messageType,
                        content = schedule.content,
                        mediaUri = schedule.mediaUri,
                        timestamp = System.currentTimeMillis(),
                        scheduledMessageId = scheduleId
                    )
                    dao.insertMessage(msg)

                    // 2. Show local notification
                    showNotification(context, persona.name, schedule.content)

                    // 3. Reschedule if recurring
                    val repeat = try {
                        RepeatFrequency.valueOf(schedule.repeatFrequency)
                    } catch (_: Exception) {
                        RepeatFrequency.DAILY
                    }

                    if (repeat != RepeatFrequency.ONCE) {
                        val scheduler = SchedulerManager(context)
                        scheduler.scheduleMessage(schedule)
                    } else {
                        // Mark as disabled once executed
                        dao.updateScheduledMessage(schedule.copy(isEnabled = false))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context, botName: String, content: String) {
        val channelId = "personachat_messages"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Persona Messages",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Incoming simulated persona messages"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        val pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, flags)

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentTitle(botName)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
