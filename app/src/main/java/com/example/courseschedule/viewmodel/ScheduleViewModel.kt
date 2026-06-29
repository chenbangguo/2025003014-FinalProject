package com.example.courseschedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.courseschedule.data.entity.CourseEntity
import com.example.courseschedule.data.entity.NotificationEntity
import com.example.courseschedule.data.repository.CourseRepository
import com.example.courseschedule.datastore.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val repository: CourseRepository,
    private val preferences: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ScheduleUiState>(ScheduleUiState.Loading)
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    private val _currentWeek = MutableStateFlow(UserPreferencesRepository.DEFAULT_CURRENT_WEEK)
    val currentWeek: StateFlow<Int> = _currentWeek.asStateFlow()

    private val _showWeekend = MutableStateFlow(true)
    val showWeekend: StateFlow<Boolean> = _showWeekend.asStateFlow()

    init {
        observeData()
    }

    private var _pendingCount = 0

    private fun observeData() {
        viewModelScope.launch {
            preferences.currentWeek.collect { week ->
                _currentWeek.value = week
            }
        }
        viewModelScope.launch {
            preferences.showWeekend.collect { show ->
                _showWeekend.value = show
            }
        }
        viewModelScope.launch {
            repository.getPendingCount().collect { count ->
                _pendingCount = count
            }
        }
        viewModelScope.launch {
            repository.getAllCourses().collect { courses ->
                if (courses.isEmpty()) {
                    _uiState.value = ScheduleUiState.Empty
                } else {
                    val hitokoto = (_uiState.value as? ScheduleUiState.Success)?.hitokoto
                    val week = _currentWeek.value
                    val filtered = courses.filter { course -> isCourseInWeek(course.weeks, week) }
                    _uiState.value = ScheduleUiState.Success(
                        courses = filtered,
                        currentWeek = week,
                        hitokoto = hitokoto,
                        pendingCount = _pendingCount
                    )
                }
            }
        }
    }

    /** Parse weeks string like "1-16" or "1,3,5,7-12" and check if [week] is included */
    private fun isCourseInWeek(weeks: String, week: Int): Boolean {
        return try {
            weeks.split(",").any { part ->
                val trimmed = part.trim()
                if (trimmed.contains("-")) {
                    val parts = trimmed.split("-")
                    val start = parts[0].toIntOrNull() ?: return@any false
                    val end = parts.getOrNull(1)?.toIntOrNull() ?: start
                    week in start..end
                } else {
                    trimmed.toIntOrNull() == week
                }
            }
        } catch (_: Exception) {
            true // parse error → show always
        }
    }

    fun loadHitokoto() {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is ScheduleUiState.Success) {
                _uiState.value = current.copy(isHitokotoLoading = true, quoteError = null)
            }
            repository.fetchHitokoto()
                .onSuccess { dto ->
                    val item = HitokotoItem(
                        content = dto.hitokoto,
                        source = if (!dto.from_who.isNullOrBlank()) "—— ${dto.from_who}" else "—— ${dto.from}"
                    )
                    val state = _uiState.value
                    if (state is ScheduleUiState.Success) {
                        _uiState.value = state.copy(hitokoto = item, isHitokotoLoading = false)
                    }
                }
                .onFailure {
                    val state = _uiState.value
                    if (state is ScheduleUiState.Success) {
                        _uiState.value = state.copy(quoteError = "获取格言失败", isHitokotoLoading = false)
                    }
                }
        }
    }

    fun setWeek(week: Int) {
        viewModelScope.launch { preferences.setCurrentWeek(week) }
    }

    fun deleteCourse(course: CourseEntity) {
        viewModelScope.launch {
            repository.deleteCourse(course)
            // Push notification
            repository.insertNotification(
                NotificationEntity(
                    title = "🗑️ 课程取消",
                    content = "《${course.name}》已从课表中移除。\n原时间：${course.dayOfWeek} 第${course.startPeriod}节\n如有疑问请联系管理员。",
                    type = "notice"
                )
            )
        }
    }

    class Factory(
        private val repository: CourseRepository,
        private val preferences: UserPreferencesRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ScheduleViewModel::class.java)) {
                return ScheduleViewModel(repository, preferences) as T
            }
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
