package com.example.courseschedule.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.courseschedule.data.entity.AssignmentEntity
import com.example.courseschedule.viewmodel.AssignmentEditUiState
import com.example.courseschedule.viewmodel.AssignmentUiState
import com.example.courseschedule.viewmodel.AssignmentViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class AssignmentFilter { ALL, PENDING, COMPLETED, URGENT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignmentScreen(viewModel: AssignmentViewModel, isAdmin: Boolean) {
    val uiState by viewModel.uiState.collectAsState()
    val editState by viewModel.editState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(AssignmentFilter.ALL) }

    LaunchedEffect(editState) {
        if (editState is AssignmentEditUiState.Saved) {
            showAddDialog = false
            viewModel.resetEditState()
        }
    }

    if (showAddDialog && isAdmin) {
        AssignmentEditDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, desc, dueDate, priority, courseId ->
                viewModel.addAssignment(title, desc, dueDate, priority, courseId)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📝 作业管理")
                        if (!isAdmin) Text(" · 学生", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, "添加作业")
                }
            }
        }
    ) { innerPadding ->
        Column(Modifier.fillMaxSize().padding(innerPadding)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(AssignmentFilter.ALL to "全部", AssignmentFilter.PENDING to "待完成", AssignmentFilter.URGENT to "紧急", AssignmentFilter.COMPLETED to "已完成").forEach { (f, label) ->
                    FilterChip(selected = filter == f, onClick = { filter = f }, label = { Text(label, style = MaterialTheme.typography.labelSmall) })
                }
            }

            when (val state = uiState) {
                is AssignmentUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is AssignmentUiState.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Schedule, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("暂无作业", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (isAdmin) Text("点击右下角 + 添加作业", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
                is AssignmentUiState.Success -> {
                    val filtered = when (filter) {
                        AssignmentFilter.ALL -> state.assignments
                        AssignmentFilter.PENDING -> state.assignments.filter { !it.isCompleted }
                        AssignmentFilter.COMPLETED -> state.assignments.filter { it.isCompleted }
                        AssignmentFilter.URGENT -> state.assignments.filter { !it.isCompleted && it.priority == 2 }
                    }
                    val total = state.assignments.size
                    val pending = state.assignments.count { !it.isCompleted }
                    if (total > 0) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Text("共 $total 项", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("⏳ $pending 待完成", style = MaterialTheme.typography.labelSmall, color = Color(0xFFFF9800))
                            Text("✅ ${total - pending} 已完成", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50))
                        }
                    }
                    if (filtered.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("没有符合条件的作业", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filtered, key = { it.id }) { a ->
                            AssignmentItem(a, viewModel::toggleCompleted, if (isAdmin) viewModel::deleteAssignment else null)
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssignmentItem(assignment: AssignmentEntity, onToggle: (AssignmentEntity) -> Unit, onDelete: ((AssignmentEntity) -> Unit)?) {
    val df = remember { SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault()) }
    val isOverdue = assignment.dueDate < System.currentTimeMillis() && !assignment.isCompleted
    val priorityColors = listOf(Color(0xFF757575), Color(0xFFFF9800), Color(0xFFE53935))
    val priorityLabels = listOf("", "重要", "紧急")

    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = if (assignment.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null,
                tint = if (assignment.isCompleted) MaterialTheme.colorScheme.primary else priorityColors[assignment.priority.coerceIn(0, 2)],
                modifier = Modifier.size(32.dp).clickable { onToggle(assignment) })
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(assignment.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, textDecoration = if (assignment.isCompleted) TextDecoration.LineThrough else TextDecoration.None, modifier = Modifier.weight(1f))
                    if (assignment.priority > 0 && !assignment.isCompleted) Text(priorityLabels[assignment.priority], style = MaterialTheme.typography.labelSmall, color = priorityColors[assignment.priority], fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 4.dp))
                }
                if (assignment.description.isNotEmpty()) Text(assignment.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                Row {
                    Text(df.format(Date(assignment.dueDate)), style = MaterialTheme.typography.labelSmall, color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal)
                    if (isOverdue) Text("  已过期", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            }
            if (onDelete != null) IconButton(onClick = { onDelete(assignment) }) {
                Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun AssignmentEditDialog(onDismiss: () -> Unit, onSave: (String, String, Long, Int, Long) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(0) }
    var courseId by remember { mutableStateOf(0L) }
    var titleError by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val cal = remember { Calendar.getInstance() }
    var dueDateMs by remember { mutableStateOf(System.currentTimeMillis() + 86400000) }
    val df = remember { SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault()) }
    var dueDateText by remember { mutableStateOf(df.format(Date(dueDateMs))) }

    androidx.compose.material3.AlertDialog(onDismissRequest = onDismiss, title = { Text("添加作业") }, text = {
        Column {
            OutlinedTextField(value = title, onValueChange = { title = it; titleError = false }, label = { Text("作业名称 *") }, isError = titleError, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("描述") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("优先级：", style = MaterialTheme.typography.bodyMedium)
                listOf("普通", "重要", "紧急").forEachIndexed { i, l ->
                    TextButton(onClick = { priority = i }) {
                        Text(l, color = if (priority == i) listOf(Color(0xFF757575), Color(0xFFFF9800), Color(0xFFE53935))[i] else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = if (priority == i) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = dueDateText, onValueChange = {}, label = { Text("截止日期") }, readOnly = true, modifier = Modifier.fillMaxWidth().clickable {
                DatePickerDialog(context, { _, y, m, d -> cal.set(y, m, d); dueDateMs = cal.timeInMillis; dueDateText = df.format(Date(dueDateMs)) }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            })
        }
    }, confirmButton = {
        Button(onClick = { if (title.isBlank()) { titleError = true; return@Button }; onSave(title.trim(), desc.trim(), dueDateMs, priority, courseId) }) { Text("保存") }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } })
}
