package com.example.courseschedule.data.repository

import com.example.courseschedule.data.dao.AssignmentDao
import com.example.courseschedule.data.dao.CourseDao
import com.example.courseschedule.data.dao.NotificationDao
import com.example.courseschedule.data.entity.AssignmentEntity
import com.example.courseschedule.data.entity.CourseEntity
import com.example.courseschedule.data.entity.NotificationEntity
import com.example.courseschedule.data.network.HitokotoDto
import com.example.courseschedule.data.network.NetworkDataSource
import kotlinx.coroutines.flow.Flow

class CourseRepository(
    private val courseDao: CourseDao,
    private val assignmentDao: AssignmentDao,
    private val notificationDao: NotificationDao
) {
    // Course operations
    fun getAllCourses(): Flow<List<CourseEntity>> = courseDao.getAllCourses()
    fun getCoursesByDay(day: Int): Flow<List<CourseEntity>> = courseDao.getCoursesByDay(day)
    suspend fun getCourseById(id: Long): CourseEntity? = courseDao.getCourseById(id)
    fun searchCourses(query: String): Flow<List<CourseEntity>> = courseDao.searchCourses(query)

    suspend fun insertCourse(course: CourseEntity): Long = courseDao.insert(course)
    suspend fun updateCourse(course: CourseEntity) = courseDao.update(course)
    suspend fun deleteCourse(course: CourseEntity) = courseDao.delete(course)

    // Assignment operations
    fun getAllAssignments(): Flow<List<AssignmentEntity>> = assignmentDao.getAllAssignments()
    fun getPendingAssignments(): Flow<List<AssignmentEntity>> = assignmentDao.getPendingAssignments()
    fun getPendingCount(): Flow<Int> = assignmentDao.getPendingCount()

    suspend fun insertAssignment(assignment: AssignmentEntity): Long = assignmentDao.insert(assignment)
    suspend fun updateAssignment(assignment: AssignmentEntity) = assignmentDao.update(assignment)
    suspend fun deleteAssignment(assignment: AssignmentEntity) = assignmentDao.delete(assignment)
    suspend fun toggleAssignmentCompleted(id: Long, completed: Boolean) =
        assignmentDao.toggleCompleted(id, completed)
    suspend fun deleteAllAssignments() = assignmentDao.deleteAll()

    // Notification operations
    fun getAllNotifications(): Flow<List<NotificationEntity>> = notificationDao.getAllNotifications()
    fun getNotificationCount(): Flow<Int> = notificationDao.getCount()
    suspend fun insertNotification(notification: NotificationEntity): Long = notificationDao.insert(notification)
    suspend fun deleteNotification(id: Long) = notificationDao.deleteById(id)
    suspend fun toggleNotificationPin(id: Long, pinned: Boolean) = notificationDao.togglePin(id, pinned)
    suspend fun deleteAllNotifications() = notificationDao.deleteAll()

    // Network operations
    suspend fun fetchHitokoto(): Result<HitokotoDto> = NetworkDataSource.fetchHitokoto()
}
