package com.example.smartclass

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.smartclass.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val pendingResult = goAsync()
        val id = intent?.getLongExtra("id", -1)
        val type = intent?.getStringExtra("type")

        if (id == null || id == -1L || type == null) {
            pendingResult.finish()
            return
        }

        val scheduleDao = AppDatabase.getDatabase(context).scheduleDao()
        val taskDao = AppDatabase.getDatabase(context).taskDao()

        CoroutineScope(Dispatchers.IO).launch { // Use Dispatchers.IO for database access
            if (type == "schedule") {
                val schedule = scheduleDao.getById(id)
                if (schedule != null) {
                    withContext(Dispatchers.Main) {
                        NotificationHelper.show(context, schedule)
                    }
                }
            } else if (type == "task") {
                val task = taskDao.getById(id)
                if (task != null && !task.isCompleted) {
                    withContext(Dispatchers.Main) {
                        NotificationHelper.show(context, task)
                    }
                }
            }
            pendingResult.finish()
        }
    }
}
