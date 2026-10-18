package com.example.smartclass

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartclass.data.Schedule
import com.example.smartclass.databinding.ActivityMainBinding
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapter: ScheduleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        adapter = ScheduleAdapter(::onEdit, ::onDelete, viewModel, this, showTasks = false)
        binding.rvSchedules.layoutManager = LinearLayoutManager(this)
        binding.rvSchedules.adapter = adapter

        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            val weekday = calendar.get(Calendar.DAY_OF_WEEK)
            viewModel.setSelectedWeekday(weekday)
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditScheduleActivity::class.java))
        }

        viewModel.schedulesForSelectedWeekday.observe(this) { schedules ->
            adapter.submitList(schedules)
        }

        binding.bottomNavigation.selectedItemId = R.id.navigation_main

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent)
                    true
                }
                R.id.navigation_main -> {
                    // Already on main
                    true
                }
                R.id.navigation_tasks -> {
                    val intent = Intent(this, TasksActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent)
                    true
                }
                R.id.navigation_notifications -> {
                    val intent = Intent(this, NotificationsActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    startActivity(intent)
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

        val today = Calendar.getInstance()
        viewModel.setSelectedWeekday(today.get(Calendar.DAY_OF_WEEK))
    }

    private fun onEdit(schedule: Schedule) {
        val i = Intent(this, AddEditScheduleActivity::class.java)
        i.putExtra("id", schedule.id)
        startActivity(i)
    }

    private fun onDelete(schedule: Schedule) {
        val weekdays = schedule.weekdays.split(",").mapNotNull { it.toIntOrNull() }.toSet()

        AlertDialog.Builder(this)
            .setTitle("Delete schedule?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.delete(schedule)

                if (weekdays.isNotEmpty()) {
                    AlarmHelper.cancelScheduleAlarm(this, schedule.id, weekdays)
                }
            }
            .setNegativeButton("No", null)
            .show()
        }
}
