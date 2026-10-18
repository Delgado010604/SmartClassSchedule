package com.example.smartclass

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.smartclass.data.Task
import java.util.Calendar

object AlarmHelper {

    fun scheduleAlarms(context: Context, scheduleId: Long, weekdays: Set<Int>, hour: Int, minute: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return
        }

        weekdays.forEach { dayOfWeek ->
            // 15-minute reminder
            val cal15Min = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
                add(Calendar.MINUTE, -15)

                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }
            scheduleExactAlarm(context, alarmManager, cal15Min, scheduleId, dayOfWeek, "schedule", 1)

            // On-time reminder
            val calOnTime = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)

                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.WEEK_OF_YEAR, 1)
                }
            }
            scheduleExactAlarm(context, alarmManager, calOnTime, scheduleId, dayOfWeek, "schedule", 2)
        }
    }

    fun cancelScheduleAlarm(context: Context, scheduleId: Long, weekdays: Set<Int>) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        weekdays.forEach { dayOfWeek ->
            cancelAlarm(context, alarmManager, generateScheduleAlarmRequestCode(scheduleId, dayOfWeek, 1))
            cancelAlarm(context, alarmManager, generateScheduleAlarmRequestCode(scheduleId, dayOfWeek, 2))
        }
    }

    fun scheduleTaskAlarm(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return
        }

        // 15-minute reminder
        val cal15Min = Calendar.getInstance().apply {
            timeInMillis = task.dueDate
            add(Calendar.MINUTE, -15)
        }
        scheduleExactAlarm(context, alarmManager, cal15Min, task.id, 0, "task", 1)

        // On-time reminder
        val calOnTime = Calendar.getInstance().apply {
            timeInMillis = task.dueDate
        }
        scheduleExactAlarm(context, alarmManager, calOnTime, task.id, 0, "task", 2)
    }

    fun cancelTaskAlarm(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        cancelAlarm(context, alarmManager, generateTaskAlarmRequestCode(task.id, 1))
        cancelAlarm(context, alarmManager, generateTaskAlarmRequestCode(task.id, 2))
    }

    private fun scheduleExactAlarm(context: Context, alarmManager: AlarmManager, cal: Calendar, id: Long, dayOfWeek: Int, type: String, requestCodeSuffix: Int) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("type", type)
        }

        val requestCode = if (type == "schedule") {
            generateScheduleAlarmRequestCode(id, dayOfWeek, requestCodeSuffix)
        } else {
            generateTaskAlarmRequestCode(id, requestCodeSuffix)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pendingIntent)
        }
    }

    private fun cancelAlarm(context: Context, alarmManager: AlarmManager, requestCode: Int) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        alarmManager.cancel(pendingIntent)
    }

    private fun generateScheduleAlarmRequestCode(scheduleId: Long, dayOfWeek: Int, suffix: Int): Int {
        return (scheduleId * 100 + dayOfWeek * 10 + suffix).toInt()
    }

    private fun generateTaskAlarmRequestCode(taskId: Long, suffix: Int): Int {
        return (taskId * 10 + suffix + 10000).toInt()
    }
}
