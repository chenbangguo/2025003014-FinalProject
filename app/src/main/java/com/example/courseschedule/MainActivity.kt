package com.example.courseschedule

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.courseschedule.navigation.AppNavigation
import com.example.courseschedule.ui.theme.CourseScheduleTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CourseScheduleTheme {
                AppNavigation()
            }
        }
    }
}
