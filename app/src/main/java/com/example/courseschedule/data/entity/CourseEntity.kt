package com.example.courseschedule.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "teacher")
    val teacher: String = "",
    @ColumnInfo(name = "classroom")
    val classroom: String = "",
    @ColumnInfo(name = "day_of_week")
    val dayOfWeek: Int,           // 1=周一 ... 7=周日
    @ColumnInfo(name = "start_period")
    val startPeriod: Int,         // 第几节课开始 (1-12)
    @ColumnInfo(name = "duration")
    val duration: Int = 1,        // 持续几节课
    @ColumnInfo(name = "weeks")
    val weeks: String = "1-16",   // 上课周次，如 "1-16" 或 "1,3,5,7-16"
    @ColumnInfo(name = "color")
    val color: Int = 0xFF4CAF50.toInt(),  // ARGB 颜色
    @ColumnInfo(name = "note")
    val note: String = ""
)
