package com.example.smartclass.data

import androidx.lifecycle.LiveData

class ScheduleRepository(private val dao: ScheduleDao) {

    suspend fun insert(s: Schedule): Long = dao.insert(s)

    suspend fun update(s: Schedule) = dao.update(s)

    fun getAll(): LiveData<List<Schedule>> = dao.getAll()

    fun getForWeekday(weekday: String): LiveData<List<Schedule>> =
        dao.getForWeekday(weekday)

    suspend fun delete(s: Schedule) = dao.delete(s)

    suspend fun getById(id: Long): Schedule? = dao.getById(id)
}
