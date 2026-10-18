package com.example.smartclass.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Schedule(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val subject: String,
    val room: String,
    val instructor: String,

    val startTimeMillis: Long,
    val endTimeMillis: Long,
    val weekdays: String,

    val notes: String? = null
)
