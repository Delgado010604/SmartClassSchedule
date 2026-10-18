package com.example.smartclass.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(s: Schedule): Long
    @Update
    suspend fun update(schedule: Schedule)

    @Delete
    suspend fun delete(s: Schedule)

    @Query("SELECT * FROM Schedule WHERE id = :id")
    suspend fun getById(id: Long): Schedule?

    @Query("SELECT * FROM Schedule ORDER BY startTimeMillis ASC")
    fun getAll(): LiveData<List<Schedule>>

    @Query("SELECT * FROM Schedule WHERE weekdays LIKE '%' || :weekday || '%' ORDER BY startTimeMillis ASC")
    fun getForWeekday(weekday: String): LiveData<List<Schedule>>
}
