package com.example.courseschedule.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.courseschedule.data.database.AppDatabase
import com.example.courseschedule.data.entity.CourseEntity
import com.example.courseschedule.data.repository.CourseRepository
import com.example.courseschedule.datastore.UserPreferencesRepository
import com.example.courseschedule.datastore.UserRole
import com.example.courseschedule.ui.screens.AssignmentScreen
import com.example.courseschedule.ui.screens.CourseEditDialog
import com.example.courseschedule.ui.screens.NotificationScreen
import com.example.courseschedule.ui.screens.ScheduleScreen
import com.example.courseschedule.ui.screens.SettingsScreen
import com.example.courseschedule.viewmodel.AssignmentViewModel
import com.example.courseschedule.viewmodel.CourseEditUiState
import com.example.courseschedule.viewmodel.CourseEditViewModel
import com.example.courseschedule.viewmodel.NotificationViewModel
import com.example.courseschedule.viewmodel.ScheduleViewModel

object Routes {
    const val SCHEDULE = "schedule"
    const val NOTIFICATIONS = "notifications"
    const val ASSIGNMENTS = "assignments"
    const val SETTINGS = "settings"
}

data class BottomNavItem(val route: String, val label: String, val icon: @Composable () -> Unit)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    val database = remember { AppDatabase.getInstance(context) }
    val repository = remember { CourseRepository(database.courseDao(), database.assignmentDao(), database.notificationDao()) }
    val preferences = remember { UserPreferencesRepository(context) }

    val role by preferences.userRole.collectAsState(initial = UserRole.ADMIN)
    val isAdmin = role == UserRole.ADMIN

    val items = listOf(
        BottomNavItem(Routes.SCHEDULE, "课程表", { Icon(Icons.Default.Home, null) }),
        BottomNavItem(Routes.NOTIFICATIONS, "通知", { Icon(Icons.Default.Campaign, null) }),
        BottomNavItem(Routes.ASSIGNMENTS, "作业", { Icon(Icons.AutoMirrored.Filled.ListAlt, null) }),
        BottomNavItem(Routes.SETTINGS, "设置", { Icon(Icons.Default.Settings, null) })
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var editingCourse by remember { mutableStateOf<CourseEntity?>(null) }
    var showCourseDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        icon = item.icon,
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        if (showCourseDialog && isAdmin) {
            val courseEditVM: CourseEditViewModel = viewModel(factory = CourseEditViewModel.Factory(repository))
            val editState by courseEditVM.editState.collectAsState()
            CourseEditDialog(
                course = editingCourse,
                onDismiss = { showCourseDialog = false; editingCourse = null; courseEditVM.resetEditState() },
                onSave = { courseEditVM.saveCourse(it) }
            )
            if (editState is CourseEditUiState.Saved) {
                showCourseDialog = false; editingCourse = null; courseEditVM.resetEditState()
            }
        }

        NavHost(navController = navController, startDestination = Routes.SCHEDULE, modifier = Modifier.padding(innerPadding)) {
            composable(Routes.SCHEDULE) {
                val vm: ScheduleViewModel = viewModel(factory = ScheduleViewModel.Factory(repository, preferences))
                ScheduleScreen(viewModel = vm, isAdmin = isAdmin,
                    onAddCourse = { editingCourse = null; showCourseDialog = true },
                    onEditCourse = { editingCourse = it; showCourseDialog = true })
            }
            composable(Routes.NOTIFICATIONS) {
                val vm: NotificationViewModel = viewModel(factory = NotificationViewModel.Factory(repository))
                NotificationScreen(viewModel = vm, isAdmin = isAdmin)
            }
            composable(Routes.ASSIGNMENTS) {
                val vm: AssignmentViewModel = viewModel(factory = AssignmentViewModel.Factory(repository))
                AssignmentScreen(viewModel = vm, isAdmin = isAdmin)
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(preferences = preferences)
            }
        }
    }
}
