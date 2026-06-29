package com.example.courseschedule.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.courseschedule.data.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notification: NotificationEntity): Long

    @Query("SELECT * FROM notifications ORDER BY is_pinned DESC, created_at DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE notifications SET is_pinned = :pinned WHERE id = :id")
    suspend fun togglePin(id: Long, pinned: Boolean)

    @Query("SELECT COUNT(*) FROM notifications")
    fun getCount(): Flow<Int>

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}
