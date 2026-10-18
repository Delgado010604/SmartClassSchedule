package com.example.smartclass.data

import androidx.lifecycle.LiveData

class TaskRepository(private val dao: TaskDao) {

    suspend fun insert(task: Task): Long = dao.insert(task)

    suspend fun update(task: Task) = dao.update(task)

    fun getAll(): LiveData<List<Task>> = dao.getAll()

    suspend fun delete(task: Task) = dao.delete(task)

    suspend fun getById(id: Long): Task? = dao.getById(id)

    fun getTasksForSchedule(scheduleId: Long): LiveData<List<Task>> = dao.getTasksForSchedule(scheduleId)
}
