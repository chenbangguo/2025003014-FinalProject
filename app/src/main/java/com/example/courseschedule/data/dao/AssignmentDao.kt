package com.example.courseschedule.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.courseschedule.data.entity.AssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(assignment: AssignmentEntity): Long

    @Update
    suspend fun update(assignment: AssignmentEntity)

    @Delete
    suspend fun delete(assignment: AssignmentEntity)

    @Query("SELECT * FROM assignments ORDER BY due_date ASC")
    fun getAllAssignments(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE course_id = :courseId ORDER BY due_date ASC")
    fun getAssignmentsByCourse(courseId: Long): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE is_completed = 0 ORDER BY due_date ASC")
    fun getPendingAssignments(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE id = :id")
    suspend fun getAssignmentById(id: Long): AssignmentEntity?

    @Query("UPDATE assignments SET is_completed = :completed WHERE id = :id")
    suspend fun toggleCompleted(id: Long, completed: Boolean)

    @Query("SELECT COUNT(*) FROM assignments WHERE is_completed = 0")
    fun getPendingCount(): Flow<Int>

    @Query("DELETE FROM assignments")
    suspend fun deleteAll()
}
