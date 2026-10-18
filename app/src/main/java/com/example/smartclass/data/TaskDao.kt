package com.example.smartclass.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM task ORDER BY dueDate ASC")
    fun getAll(): LiveData<List<Task>>

    @Query("SELECT * FROM task WHERE id = :id")
    suspend fun getById(id: Long): Task?

    @Query("SELECT * FROM task WHERE scheduleId = :scheduleId")
    fun getTasksForSchedule(scheduleId: Long): LiveData<List<Task>>
}
