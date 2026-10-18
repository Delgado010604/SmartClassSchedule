package com.example.smartclass

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.smartclass.data.Schedule
import com.example.smartclass.databinding.ActivityAddEditScheduleBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddEditScheduleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditScheduleBinding
    private val viewModel: MainViewModel by viewModels()

    private val selectedWeekDays = sortedSetOf<Int>()
    private val startTime = Calendar.getInstance()
    private val endTime = Calendar.getInstance()

    private var scheduleId: Long = 0L
    private var schedule: Schedule? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddEditScheduleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scheduleId = intent.getLongExtra("id", 0L)

        // Load schedule if editing
        if (scheduleId != 0L) {
            supportActionBar?.title = getString(R.string.edit_schedule)
            binding.btnDelete.visibility = View.VISIBLE
            viewModel.getScheduleById(scheduleId).observe(this) { schedule ->
                schedule?.let { s ->
                    this.schedule = s
                    binding.etSubject.setText(s.subject)
                    binding.etRoom.setText(s.room)
                    binding.etInstructor.setText(s.instructor)
                    binding.etNotes.setText(s.notes)

                    selectedWeekDays.clear()
                    selectedWeekDays.addAll(
                        s.weekdays.split(",").mapNotNull { it.toIntOrNull() }
                    )

                    startTime.timeInMillis = s.startTimeMillis
                    endTime.timeInMillis = s.endTimeMillis

                    updateDateButtonText()
                    updateTimeButtonText()
                }
            }
        } else {
            supportActionBar?.title = getString(R.string.add_schedule)
            // Default empty schedule
            startTime.set(Calendar.HOUR_OF_DAY, 0)
            startTime.set(Calendar.MINUTE, 0)
            endTime.set(Calendar.HOUR_OF_DAY, 0)
            endTime.set(Calendar.MINUTE, 0)
            updateDateButtonText()
            updateTimeButtonText()
        }

        // Button listeners
        binding.btnDate.setOnClickListener { showMultiDayDialog() }

        binding.btnStartTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    startTime.set(Calendar.HOUR_OF_DAY, hour)
                    startTime.set(Calendar.MINUTE, minute)
                    updateTimeButtonText()
                },
                startTime.get(Calendar.HOUR_OF_DAY),
                startTime.get(Calendar.MINUTE),
                false
            ).show()
        }

        binding.btnEndTime.setOnClickListener {
            TimePickerDialog(
                this,
                { _, hour, minute ->
                    endTime.set(Calendar.HOUR_OF_DAY, hour)
                    endTime.set(Calendar.MINUTE, minute)
                    updateTimeButtonText()
                },
                endTime.get(Calendar.HOUR_OF_DAY),
                endTime.get(Calendar.MINUTE),
                false
            ).show()
        }

        binding.btnSave.setOnClickListener { saveSchedule() }
        binding.btnDelete.setOnClickListener { deleteSchedule() }
    }

    private fun showMultiDayDialog() {
        val days = arrayOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val checkedItems = BooleanArray(7) { index -> selectedWeekDays.contains(index + 2) } // Calendar.MONDAY = 2

        AlertDialog.Builder(this)
            .setTitle("Select Class Days")
            .setMultiChoiceItems(days, checkedItems) { _, which, isChecked ->
                val dayCode = which + 2
                if (isChecked) selectedWeekDays.add(dayCode)
                else selectedWeekDays.remove(dayCode)
                updateDateButtonText()
            }
            .setPositiveButton("OK", null)
            .show()
    }

    private fun saveSchedule() {
        val subject = binding.etSubject.text.toString().trim()
        val room = binding.etRoom.text.toString().trim()
        val instructor = binding.etInstructor.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim().ifEmpty { null }

        if (subject.isBlank() || room.isBlank() || instructor.isBlank()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        if (endTime.timeInMillis <= startTime.timeInMillis) {
            Toast.makeText(this, "End time must be after start time", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedWeekDays.isEmpty()) {
            Toast.makeText(this, "Please select class days", Toast.LENGTH_SHORT).show()
            return
        }

        val weekdayString = selectedWeekDays.joinToString(",")
        val newSchedule = Schedule(
            id = scheduleId,
            subject = subject,
            room = room,
            instructor = instructor,
            weekdays = weekdayString,
            startTimeMillis = startTime.timeInMillis,
            endTimeMillis = endTime.timeInMillis,
            notes = notes
        )

        viewModel.allSchedules.observe(this) { existingSchedules ->
            val overlappingSchedule = existingSchedules.find {
                it.id != newSchedule.id &&
                        it.weekdays.split(",").mapNotNull { it.toIntOrNull() }.any { selectedWeekDays.contains(it) } &&
                        (newSchedule.startTimeMillis < it.endTimeMillis && newSchedule.endTimeMillis > it.startTimeMillis)
            }

            if (overlappingSchedule != null) {
                AlertDialog.Builder(this)
                    .setTitle("Schedule Conflict")
                    .setMessage("This schedule overlaps with an existing schedule: ${overlappingSchedule.subject}")
                    .setPositiveButton("OK", null)
                    .show()
            } else {
                viewModel.saveSchedule(newSchedule) { newId ->
                    val alarmId = if (scheduleId == 0L) newId else scheduleId
                    if (binding.switchReminder.isChecked) {
                        AlarmHelper.scheduleAlarms(
                            context = this,
                            scheduleId = alarmId,
                            weekdays = selectedWeekDays,
                            hour = startTime.get(Calendar.HOUR_OF_DAY),
                            minute = startTime.get(Calendar.MINUTE)
                        )
                    } else {
                        AlarmHelper.cancelScheduleAlarm(this, alarmId, selectedWeekDays)
                    }
                    Toast.makeText(this, "Schedule Saved!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun deleteSchedule() {
        schedule?.let {
            AlertDialog.Builder(this)
                .setTitle("Delete schedule?")
                .setPositiveButton("Yes") { _, _ ->
                    viewModel.delete(it)
                    val weekdays = it.weekdays.split(",").mapNotNull { it.toIntOrNull() }.toSet()
                    if (weekdays.isNotEmpty()) {
                        AlarmHelper.cancelScheduleAlarm(this, it.id, weekdays)
                    }
                    Toast.makeText(this, "Schedule Deleted!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("No", null)
                .show()
        }
    }

    private fun updateDateButtonText() {
        if (selectedWeekDays.isEmpty()) {
            binding.btnDate.text = "Select Days"
            return
        }
        val map = mapOf(
            2 to "Mon", 3 to "Tue", 4 to "Wed", 5 to "Thu", 6 to "Fri", 7 to "Sat", 1 to "Sun"
        )
        binding.btnDate.text = selectedWeekDays.sorted().joinToString(", ") { map[it] ?: "" }
    }

    private fun updateTimeButtonText() {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        binding.btnStartTime.text = if (startTime.get(Calendar.HOUR_OF_DAY) != 0 || startTime.get(Calendar.MINUTE) != 0)
            sdf.format(startTime.time) else getString(R.string.start_time)

        binding.btnEndTime.text = if (endTime.get(Calendar.HOUR_OF_DAY) != 0 || endTime.get(Calendar.MINUTE) != 0)
            sdf.format(endTime.time) else getString(R.string.end_time)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
