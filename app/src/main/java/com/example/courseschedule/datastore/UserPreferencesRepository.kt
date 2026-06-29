package com.example.courseschedule.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class UserRole(val label: String) {
    STUDENT("学生"),
    ADMIN("管理员");

    companion object {
        fun fromString(s: String) = entries.find { it.name == s } ?: ADMIN
    }
}

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val CURRENT_WEEK = intPreferencesKey("current_week")
        val FIRST_DAY_MONDAY = booleanPreferencesKey("first_day_monday")
        val SHOW_WEEKEND = booleanPreferencesKey("show_weekend")
        val DEFAULT_SEMESTER = intPreferencesKey("default_semester")
        val USER_ROLE = stringPreferencesKey("user_role")

        const val DEFAULT_CURRENT_WEEK = 1
        const val DEFAULT_SEMESTER_START = 202603
    }

    // ===== User Role =====
    val userRole: Flow<UserRole> = context.dataStore.data.map { prefs ->
        val raw = prefs[USER_ROLE] ?: UserRole.ADMIN.name
        UserRole.fromString(raw)
    }

    suspend fun setUserRole(role: UserRole) {
        context.dataStore.edit { prefs ->
            prefs[USER_ROLE] = role.name
        }
    }

    // ===== Existing preferences =====
    val currentWeek: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[CURRENT_WEEK] ?: DEFAULT_CURRENT_WEEK
    }

    val firstDayMonday: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[FIRST_DAY_MONDAY] ?: true
    }

    val showWeekend: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[SHOW_WEEKEND] ?: true
    }

    val defaultSemester: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[DEFAULT_SEMESTER] ?: DEFAULT_SEMESTER_START
    }

    suspend fun setCurrentWeek(week: Int) {
        context.dataStore.edit { prefs -> prefs[CURRENT_WEEK] = week }
    }

    suspend fun setFirstDayMonday(monday: Boolean) {
        context.dataStore.edit { prefs -> prefs[FIRST_DAY_MONDAY] = monday }
    }

    suspend fun setShowWeekend(show: Boolean) {
        context.dataStore.edit { prefs -> prefs[SHOW_WEEKEND] = show }
    }

    suspend fun setDefaultSemester(semester: Int) {
        context.dataStore.edit { prefs -> prefs[DEFAULT_SEMESTER] = semester }
    }
}
