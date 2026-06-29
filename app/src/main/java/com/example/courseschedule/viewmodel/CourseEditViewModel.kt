package com.example.courseschedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.courseschedule.data.entity.CourseEntity
import com.example.courseschedule.data.entity.NotificationEntity
import com.example.courseschedule.data.repository.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CourseEditViewModel(
    private val repository: CourseRepository
) : ViewModel() {

    private val _editState = MutableStateFlow<CourseEditUiState>(CourseEditUiState.Idle)
    val editState: StateFlow<CourseEditUiState> = _editState.asStateFlow()

    private val _allCourses = MutableStateFlow<List<CourseEntity>>(emptyList())
    val allCourses: StateFlow<List<CourseEntity>> = _allCourses.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllCourses().collect { courses ->
                _allCourses.value = courses
            }
        }
    }

    private val dayLabels = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    fun saveCourse(course: CourseEntity) {
        viewModelScope.launch {
            _editState.value = CourseEditUiState.Saving
            try {
                val isNew = course.id == 0L
                if (isNew) {
                    repository.insertCourse(course)
                } else {
                    repository.updateCourse(course)
                }
                // Push notification
                val notif = if (isNew) {
                    val day = dayLabels.getOrElse(course.dayOfWeek - 1) { "未知" }
                    val periods = if (course.duration == 1) "第${course.startPeriod}节"
                    else "第${course.startPeriod}-${course.startPeriod + course.duration - 1}节"
                    NotificationEntity(
                        title = "📋 新增课程",
                        content = "《${course.name}》已添加到课表\n时间：${day} $periods\n教室：${course.classroom.ifEmpty { "待定" }}\n教师：${course.teacher.ifEmpty { "待定" }}\n周次：${course.weeks}",
                        type = "notice"
                    )
                } else {
                    NotificationEntity(
                        title = "✏️ 课程变更",
                        content = "《${course.name}》信息已更新。\n请查看课表确认最新的上课时间和地点。",
                        type = "notice"
                    )
                }
                repository.insertNotification(notif)
                _editState.value = CourseEditUiState.Saved
            } catch (e: Exception) {
                _editState.value = CourseEditUiState.Error(e.message ?: "保存失败")
            }
        }
    }

    fun resetEditState() {
        _editState.value = CourseEditUiState.Idle
    }

    class Factory(
        private val repository: CourseRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CourseEditViewModel::class.java)) {
                return CourseEditViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
