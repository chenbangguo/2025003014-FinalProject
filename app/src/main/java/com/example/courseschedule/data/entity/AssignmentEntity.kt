package com.example.courseschedule.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "course_id")
    val courseId: Long,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String = "",
    @ColumnInfo(name = "due_date")
    val dueDate: Long,            // 截止时间戳
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    @ColumnInfo(name = "priority")
    val priority: Int = 0,        // 0=普通, 1=重要, 2=紧急
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
