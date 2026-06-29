package com.example.courseschedule.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.courseschedule.data.entity.CourseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: CourseEntity): Long

    @Update
    suspend fun update(course: CourseEntity)

    @Delete
    suspend fun delete(course: CourseEntity)

    @Query("SELECT * FROM courses ORDER BY day_of_week, start_period")
    fun getAllCourses(): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE day_of_week = :day ORDER BY start_period")
    fun getCoursesByDay(day: Int): Flow<List<CourseEntity>>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getCourseById(id: Long): CourseEntity?

    @Query("SELECT * FROM courses WHERE name LIKE '%' || :query || '%' OR teacher LIKE '%' || :query || '%' ORDER BY day_of_week")
    fun searchCourses(query: String): Flow<List<CourseEntity>>

    @Query("SELECT COUNT(*) FROM courses")
    fun getCourseCount(): Flow<Int>
}
