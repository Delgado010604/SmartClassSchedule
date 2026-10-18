package com.example.smartclass

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartclass.data.Task
import com.example.smartclass.databinding.ActivityTasksBinding

class TasksActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTasksBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        taskAdapter = TaskAdapter(::onEdit, ::onCompletedChanged)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@TasksActivity)
            adapter = taskAdapter
        }

        viewModel.allTasks.observe(this) { tasks ->
            if (tasks.isEmpty()) {
                binding.tvEmpty.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.tvEmpty.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                taskAdapter.submitList(tasks.sortedBy { it.isCompleted })
            }
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddEditTaskActivity::class.java))
        }

        binding.bottomNavigation.selectedItemId = R.id.navigation_tasks

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
                    // Already on tasks
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
    }

    private fun onEdit(task: Task) {
        val intent = Intent(this, AddEditTaskActivity::class.java)
        intent.putExtra("id", task.id)
        startActivity(intent)
    }

    private fun onCompletedChanged(task: Task, isCompleted: Boolean) {
        viewModel.saveTask(task.copy(isCompleted = isCompleted)) {}
    }
}
