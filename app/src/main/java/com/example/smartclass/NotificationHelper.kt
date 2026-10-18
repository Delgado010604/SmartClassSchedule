package com.example.smartclass

import android.app.*
import android.content.*
import android.os.Build
import androidx.core.app.*
import com.example.smartclass.data.Schedule
import com.example.smartclass.data.Task

object NotificationHelper {

    private const val SCHEDULE_CHANNEL_ID = "schedule_notifications"
    private const val TASK_CHANNEL_ID = "task_notifications"

    fun show(ctx: Context, s: Schedule) {

        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(SCHEDULE_CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        SCHEDULE_CHANNEL_ID,
                        "Class Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                )
            }
        }

        val notif = NotificationCompat.Builder(ctx, SCHEDULE_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("Upcoming class: ${s.subject}")
            .setContentText("Room: ${s.room}")
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_SOUND)
            .build()

        manager.notify(s.id.toInt(), notif)
    }

    fun show(ctx: Context, t: Task) {

        val manager = ctx.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (manager.getNotificationChannel(TASK_CHANNEL_ID) == null) {
                manager.createNotificationChannel(
                    NotificationChannel(
                        TASK_CHANNEL_ID,
                        "Task Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                )
            }
        }

        val notif = NotificationCompat.Builder(ctx, TASK_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle("Upcoming ${t.taskType}: ${t.description}")
            .setContentText("Due soon")
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_SOUND)
            .build()

        // Use a unique ID for task notifications to avoid conflicts with schedule notifications
        manager.notify((t.id + 10000).toInt(), notif)
    }
}
