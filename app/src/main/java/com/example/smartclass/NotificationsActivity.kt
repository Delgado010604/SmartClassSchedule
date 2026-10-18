package com.example.smartclass

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MediatorLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartclass.data.NotificationItem
import com.example.smartclass.data.Schedule
import com.example.smartclass.data.Task
import com.example.smartclass.databinding.ActivityNotificationsBinding
import java.util.Calendar

class NotificationsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        notificationAdapter = NotificationAdapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@NotificationsActivity)
            adapter = notificationAdapter
        }

        val today = Calendar.getInstance()
        val startOfDay = today.clone() as Calendar
        startOfDay.set(Calendar.HOUR_OF_DAY, 0)
        startOfDay.set(Calendar.MINUTE, 0)
        startOfDay.set(Calendar.SECOND, 0)
        startOfDay.set(Calendar.MILLISECOND, 0)

        val endOfDay = today.clone() as Calendar
        endOfDay.set(Calendar.HOUR_OF_DAY, 23)
        endOfDay.set(Calendar.MINUTE, 59)
        endOfDay.set(Calendar.SECOND, 59)
        endOfDay.set(Calendar.MILLISECOND, 999)

        val combinedNotifications = MediatorLiveData<List<NotificationItem>>().apply {
            var schedules: List<Schedule>? = null
            var tasks: List<Task>? = null

            fun update() {
                if (schedules != null && tasks != null) {
                    val items = mutableListOf<NotificationItem>()
                    items.addAll(schedules!!.map { NotificationItem.ScheduleItem(it) })
                    items.addAll(tasks!!.filter { it.dueDate >= startOfDay.timeInMillis && it.dueDate <= endOfDay.timeInMillis }.map { NotificationItem.TaskItem(it) })
                    value = items
                }
            }

            addSource(viewModel.getSchedulesForDay(today.get(Calendar.DAY_OF_WEEK))) { schedules = it; update() }
            addSource(viewModel.allTasks) { tasks = it; update() }
        }

        combinedNotifications.observe(this) { items ->
            if (items.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                notificationAdapter.submitList(items)
            }
        }

        binding.bottomNavigation.selectedItemId = R.id.navigation_notifications

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent)
                    true
                }
                R.id.navigation_main -> {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent)
                    true
                }
                R.id.navigation_tasks -> {
                    val intent = Intent(this, TasksActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent)
                    true
                }
                R.id.navigation_notifications -> {
                    true
                }
                R.id.navigation_settings -> {
                    val intent = Intent(this, SettingsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }
}
