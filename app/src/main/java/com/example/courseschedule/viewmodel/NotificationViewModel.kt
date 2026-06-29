package com.example.courseschedule.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.courseschedule.data.entity.NotificationEntity
import com.example.courseschedule.data.repository.CourseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

sealed interface NotificationUiState {
    data object Loading : NotificationUiState
    data class Success(val notifications: List<NotificationEntity>) : NotificationUiState
    data object Empty : NotificationUiState
}

class NotificationViewModel(
    private val repository: CourseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationUiState>(NotificationUiState.Loading)
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init { loadNotifications() }

    fun loadNotifications() {
        viewModelScope.launch {
            repository.getAllNotifications().collect { list ->
                _uiState.value = if (list.isEmpty()) NotificationUiState.Empty
                else NotificationUiState.Success(list)
            }
        }
    }

    fun publish(title: String, content: String, type: String) {
        viewModelScope.launch {
            repository.insertNotification(NotificationEntity(title = title, content = content, type = type))
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.deleteNotification(id) }
    }

    fun togglePin(id: Long, pinned: Boolean) {
        viewModelScope.launch { repository.toggleNotificationPin(id, pinned) }
    }

    class Factory(private val repository: CourseRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(NotificationViewModel::class.java))
                return NotificationViewModel(repository) as T
            throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
        }
    }
}
