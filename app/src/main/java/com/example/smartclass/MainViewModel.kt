package com.example.smartclass

import android.app.Application
import androidx.lifecycle.*
import com.example.smartclass.data.AppDatabase
import com.example.smartclass.data.Schedule
import com.example.smartclass.data.ScheduleRepository
import com.example.smartclass.data.Task
import com.example.smartclass.data.TaskRepository
import kotlinx.coroutines.launch
import java.util.Calendar

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val scheduleRepo = ScheduleRepository(AppDatabase.getDatabase(app).scheduleDao())
    private val taskRepo = TaskRepository(AppDatabase.getDatabase(app).taskDao())
    private val _selectedWeekday = MutableLiveData<Int>()

    val schedulesForSelectedWeekday: LiveData<List<Schedule>> = _selectedWeekday.switchMap { day ->
        scheduleRepo.getForWeekday(day.toString())
    }

    val allSchedules: LiveData<List<Schedule>> = scheduleRepo.getAll()
    val allTasks: LiveData<List<Task>> = taskRepo.getAll()

    init {
        _selectedWeekday.value = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    }

    fun setSelectedWeekday(day: Int) {
        _selectedWeekday.value = day
    }

    fun saveSchedule(schedule: Schedule, callback: (Long) -> Unit) = viewModelScope.launch {
        val id = if (schedule.id == 0L) scheduleRepo.insert(schedule)
        else { scheduleRepo.update(schedule); schedule.id }
        callback(id)
    }

    fun delete(schedule: Schedule) = viewModelScope.launch { scheduleRepo.delete(schedule) }

    fun getScheduleById(id: Long): LiveData<Schedule?> = liveData { emit(scheduleRepo.getById(id)) }

    fun getSchedulesForDay(dayOfWeek: Int): LiveData<List<Schedule>> {
        return scheduleRepo.getForWeekday(dayOfWeek.toString())
    }

    fun saveTask(task: Task, callback: (Long) -> Unit) = viewModelScope.launch {
        val id = if (task.id == 0L) taskRepo.insert(task)
        else { taskRepo.update(task); task.id }
        callback(id)
    }

    fun deleteTask(task: Task) = viewModelScope.launch { taskRepo.delete(task) }

    fun getTaskById(id: Long): LiveData<Task?> = liveData { emit(taskRepo.getById(id)) }

    fun getTasksForSchedule(scheduleId: Long): LiveData<List<Task>> {
        return taskRepo.getTasksForSchedule(scheduleId)
    }
}
