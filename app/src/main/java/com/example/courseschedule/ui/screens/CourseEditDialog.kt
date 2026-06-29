package com.example.courseschedule.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.courseschedule.data.entity.CourseEntity
import com.example.courseschedule.ui.theme.CourseColors

@Composable
fun CourseEditDialog(
    course: CourseEntity?,
    onDismiss: () -> Unit,
    onSave: (CourseEntity) -> Unit
) {
    var name by remember { mutableStateOf(course?.name ?: "") }
    var teacher by remember { mutableStateOf(course?.teacher ?: "") }
    var classroom by remember { mutableStateOf(course?.classroom ?: "") }
    var dayOfWeek by remember { mutableStateOf(course?.dayOfWeek?.toString() ?: "1") }
    var startPeriod by remember { mutableStateOf(course?.startPeriod?.toString() ?: "1") }
    var duration by remember { mutableStateOf(course?.duration?.toString() ?: "2") }
    var weeks by remember { mutableStateOf(course?.weeks ?: "1-16") }
    var color by remember { mutableStateOf(course?.color ?: CourseColors[0]) }
    var note by remember { mutableStateOf(course?.note ?: "") }
    var nameError by remember { mutableStateOf(false) }

    val dayLabels = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (course == null) "添加课程" else "编辑课程") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it; nameError = false },
                    label = { Text("课程名称 *") }, isError = nameError,
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = teacher, onValueChange = { teacher = it },
                    label = { Text("授课教师") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = classroom, onValueChange = { classroom = it },
                    label = { Text("上课教室") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                // Day of week selector
                Text("上课日期", style = MaterialTheme.typography.bodyMedium)
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    dayLabels.forEachIndexed { idx, label ->
                        val d = idx + 1
                        val isSelected = dayOfWeek == d.toString()
                        TextButton(
                            onClick = { dayOfWeek = d.toString() },
                            modifier = Modifier.weight(1f).then(
                                if (isSelected) Modifier.clip(CircleShape) else Modifier
                            )
                        ) {
                            Text(label, color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Start period + duration
                Row(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = startPeriod,
                        onValueChange = { if (it.isEmpty() || it.all(Char::isDigit)) startPeriod = it },
                        label = { Text("开始节次") }, singleLine = true, modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = duration,
                        onValueChange = { if (it.isEmpty() || it.all(Char::isDigit)) duration = it },
                        label = { Text("持续节数") }, singleLine = true, modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = weeks, onValueChange = { weeks = it },
                    label = { Text("上课周次 (如: 1-16)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note, onValueChange = { note = it },
                    label = { Text("备注 (可选)") }, modifier = Modifier.fillMaxWidth(), minLines = 1
                )

                // Color picker
                Spacer(Modifier.height(8.dp))
                Text("课程颜色", style = MaterialTheme.typography.bodyMedium)
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CourseColors.take(8).forEach { c ->
                        val isSelected = c == color
                        Canvas(Modifier.size(32.dp).clickable { color = c }) {
                            drawCircle(Color(c))
                            if (isSelected) {
                                drawCircle(Color.White, radius = size.minDimension / 5)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isBlank()) { nameError = true; return@Button }
                val entity = CourseEntity(
                    id = course?.id ?: 0,
                    name = name.trim(),
                    teacher = teacher.trim(),
                    classroom = classroom.trim(),
                    dayOfWeek = dayOfWeek.toIntOrNull()?.coerceIn(1, 7) ?: 1,
                    startPeriod = startPeriod.toIntOrNull()?.coerceIn(1, 14) ?: 1,
                    duration = duration.toIntOrNull()?.coerceIn(1, 6) ?: 2,
                    weeks = weeks.ifBlank { "1-16" },
                    color = color,
                    note = note.trim()
                )
                onSave(entity)
            }) { Text("保存") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
