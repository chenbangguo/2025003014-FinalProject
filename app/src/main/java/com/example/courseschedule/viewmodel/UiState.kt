package com.example.courseschedule.viewmodel

import com.example.courseschedule.data.entity.AssignmentEntity
import com.example.courseschedule.data.entity.CourseEntity
import com.example.courseschedule.data.network.HitokotoDto

// --- Schedule UiState ---
sealed interface ScheduleUiState {
    data object Loading : ScheduleUiState
    data class Success(
        val courses: List<CourseEntity>,
        val currentWeek: Int,
        val hitokoto: HitokotoItem? = null,
        val isHitokotoLoading: Boolean = false,
        val quoteError: String? = null,
        val pendingCount: Int = 0
    ) : ScheduleUiState
    data object Empty : ScheduleUiState
}

data class HitokotoItem(
    val content: String,
    val source: String
)

// --- Assignment UiState ---
sealed interface AssignmentUiState {
    data object Loading : AssignmentUiState
    data class Success(val assignments: List<AssignmentEntity>) : AssignmentUiState
    data object Empty : AssignmentUiState
}

// --- Course Edit UiState ---
sealed interface CourseEditUiState {
    data object Idle : CourseEditUiState
    data object Saving : CourseEditUiState
    data object Saved : CourseEditUiState
    data class Error(val message: String) : CourseEditUiState
}

// --- Assignment Edit UiState ---
sealed interface AssignmentEditUiState {
    data object Idle : AssignmentEditUiState
    data object Saving : AssignmentEditUiState
    data object Saved : AssignmentEditUiState
    data class Error(val message: String) : AssignmentEditUiState
}
