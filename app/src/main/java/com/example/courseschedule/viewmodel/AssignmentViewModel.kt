package com.example.courseschedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.courseschedule.data.entity.AssignmentEntity
import com.example.courseschedule.data.entity.NotificationEntity
import com.example.courseschedule.data.repository.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AssignmentViewModel(
    private val repository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AssignmentUiState>(AssignmentUiState.Loading)
    val uiState: StateFlow<AssignmentUiState> = _uiState.asStateFlow()

    private val _editState = MutableStateFlow<AssignmentEditUiState>(AssignmentEditUiState.Idle)
    val editState: StateFlow<AssignmentEditUiState> = _editState.asStateFlow()

    init {
        loadAssignments()
    }

    fun loadAssignments() {
        viewModelScope.launch {
            repository.getAllAssignments().collect { assignments ->
                _uiState.value = if (assignments.isEmpty()) {
                    AssignmentUiState.Empty
                } else {
                    AssignmentUiState.Success(assignments)
                }
            }
        }
    }

    fun addAssignment(title: String, desc: String, dueDate: Long, priority: Int, courseId: Long) {
        viewModelScope.launch {
            _editState.value = AssignmentEditUiState.Saving
            try {
                repository.insertAssignment(
                    AssignmentEntity(title = title, description = desc, dueDate = dueDate, priority = priority, courseId = courseId)
                )
                // Push notification
                val prioLabel = when (priority) { 1 -> "重要" 2 -> "紧急" else -> "" }
                repository.insertNotification(
                    NotificationEntity(
                        title = "📝 新作业发布",
                        content = "「${title}」${prioLabel}\n${desc}\n截止时间见作业列表。",
                        type = "notice"
                    )
                )
                _editState.value = AssignmentEditUiState.Saved
            } catch (e: Exception) {
                _editState.value = AssignmentEditUiState.Error(e.message ?: "保存失败")
            }
        }
    }

    fun toggleCompleted(assignment: AssignmentEntity) {
        viewModelScope.launch {
            repository.toggleAssignmentCompleted(assignment.id, !assignment.isCompleted)
        }
    }

    fun deleteAssignment(assignment: AssignmentEntity) {
        viewModelScope.launch {
            repository.deleteAssignment(assignment)
        }
    }

    fun resetEditState() {
        _editState.value = AssignmentEditUiState.Idle
    }

    class Factory(
        private val repository: CourseRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AssignmentViewModel::class.java)) {
                return AssignmentViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
