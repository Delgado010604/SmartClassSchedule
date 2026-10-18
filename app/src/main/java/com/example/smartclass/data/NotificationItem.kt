package com.example.smartclass.data

sealed class NotificationItem {
    data class ScheduleItem(val schedule: Schedule) : NotificationItem()
    data class TaskItem(val task: Task) : NotificationItem()
}
