package com.example.smartclass

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.smartclass.data.Schedule
import com.example.smartclass.data.Task
import com.example.smartclass.databinding.ActivityAddEditTaskBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEditTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditTaskBinding
    private val viewModel: MainViewModel by viewModels()

    private val dueDate = Calendar.getInstance()
    private var schedules = listOf<Schedule>()
    private var selectedSchedule: Schedule? = null

    private var taskId: Long = 0L
    private var task: Task? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddEditTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        taskId = intent.getLongExtra("id", 0L)

        viewModel.allSchedules.observe(this) { schedules ->
            this.schedules = schedules
            val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_item,
                schedules.map { it.subject }
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerSubject.adapter = adapter

            // Load task if editing
            if (taskId != 0L) {
                supportActionBar?.title = getString(R.string.edit_task)
                binding.btnDelete.visibility = View.VISIBLE
                viewModel.getTaskById(taskId).observe(this) { task ->
                    task?.let { t ->
                        this.task = t
                        val scheduleIndex = schedules.indexOfFirst { it.id == t.scheduleId }
                        if (scheduleIndex != -1) {
                            binding.spinnerSubject.setSelection(scheduleIndex)
                        }
                        binding.etDescription.setText(t.description)
                        dueDate.timeInMillis = t.dueDate
                        updateDueDateButtonText()
                        updateDueTimeButtonText()

                        if (t.taskType == "Assignment") {
                            binding.rbAssignment.isChecked = true
                        } else {
                            binding.rbExam.isChecked = true
                        }
                    }
                }
            } else {
                supportActionBar?.title = getString(R.string.add_task)
                updateDueDateButtonText()
                updateDueTimeButtonText()
            }
        }

        binding.spinnerSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedSchedule = schedules[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedSchedule = null
            }
        }

        // Button listeners
        binding.btnDueDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    dueDate.set(Calendar.YEAR, year)
                    dueDate.set(Calendar.MONTH, month)
                    dueDate.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    updateDueDateButtonText()
                },
                dueDate.get(Calendar.YEAR),
                dueDate.get(Calendar.MONTH),
                dueDate.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = System.currentTimeMillis()
            datePickerDialog.show()
        }

        binding.btnDueTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    dueDate.set(Calendar.HOUR_OF_DAY, hour)
                    dueDate.set(Calendar.MINUTE, minute)
                    updateDueTimeButtonText()
                },
                dueDate.get(Calendar.HOUR_OF_DAY),
                dueDate.get(Calendar.MINUTE),
                false
            ).show()
        }

        binding.btnSave.setOnClickListener { saveTask() }
        binding.btnDelete.setOnClickListener { deleteTask() }
    }

    private fun saveTask() {
        val description = binding.etDescription.text.toString().trim()
        if (description.isBlank()) {
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedSchedule == null) {
            Toast.makeText(this, "Please select a subject", Toast.LENGTH_SHORT).show()
            return
        }

        val taskType = if (binding.rbAssignment.isChecked) "Assignment" else "Exam"

        val task = Task(
            id = taskId,
            scheduleId = selectedSchedule!!.id,
            description = description,
            dueDate = dueDate.timeInMillis,
            taskType = taskType
        )

        viewModel.saveTask(task) { newId ->
            val alarmId = if (taskId == 0L) newId else taskId
            val savedTask = task.copy(id = alarmId)
            if (binding.switchReminder.isChecked) {
                AlarmHelper.scheduleTaskAlarm(this, savedTask)
            } else {
                AlarmHelper.cancelTaskAlarm(this, savedTask)
            }
            Toast.makeText(this, "Task Saved!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun deleteTask() {
        task?.let {
            AlertDialog.Builder(this)
                .setTitle("Delete task?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.deleteTask(it)
                    AlarmHelper.cancelTaskAlarm(this, it)
                    Toast.makeText(this, "Task Deleted!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun updateDueDateButtonText() {
        val sdf = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
        binding.btnDueDate.text = sdf.format(dueDate.time)
    }

    private fun updateDueTimeButtonText() {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        binding.btnDueTime.text = sdf.format(dueDate.time)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
