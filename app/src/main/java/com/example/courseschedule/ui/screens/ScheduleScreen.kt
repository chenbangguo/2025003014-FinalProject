package com.example.courseschedule.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.courseschedule.data.entity.CourseEntity
import com.example.courseschedule.ui.components.CourseBlock
import com.example.courseschedule.ui.components.EmptyScheduleHint
import com.example.courseschedule.viewmodel.ScheduleUiState
import com.example.courseschedule.viewmodel.ScheduleViewModel

private val DAY_LABELS = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
private val PERIOD_TIMES = listOf(
    "08:00", "08:50", "09:50", "10:40", "11:30", "12:20",
    "13:30", "14:20", "15:20", "16:10", "17:00", "17:50",
    "19:00", "19:50"
)
private const val PERIOD_COUNT = 14
private const val CELL_WIDTH = 110
private const val TIME_COL_WIDTH = 50
private const val HEADER_HEIGHT = 44
private const val PERIOD_HEIGHT = 52

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: ScheduleViewModel,
    isAdmin: Boolean,
    onAddCourse: () -> Unit,
    onEditCourse: (CourseEntity) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentWeek by viewModel.currentWeek.collectAsState()
    val showWeekend by viewModel.showWeekend.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<CourseEntity?>(null) }

    LaunchedEffect(Unit) { viewModel.loadHitokoto() }

    // Delete confirm dialog (admin only)
    if (showDeleteDialog != null && isAdmin) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除课程") },
            text = { Text("确定要删除「${showDeleteDialog!!.name}」吗？") },
            confirmButton = {
                androidx.compose.material3.Button(onClick = {
                    viewModel.deleteCourse(showDeleteDialog!!)
                    showDeleteDialog = null
                }) { Text("删除") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showDeleteDialog = null }) { Text("取消") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📖 课程表")
                        if (!isAdmin) {
                            Text(
                                " · 学生",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.setWeek((currentWeek - 1).coerceAtLeast(1)) }) {
                        Icon(Icons.Default.ChevronLeft, "上一周")
                    }
                    Text(
                        "第${currentWeek}周",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(onClick = { viewModel.setWeek(currentWeek + 1) }) {
                        Icon(Icons.Default.ChevronRight, "下一周")
                    }
                    IconButton(onClick = { viewModel.loadHitokoto() }) {
                        Icon(Icons.Default.Refresh, "刷新格言")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = onAddCourse) {
                    Icon(Icons.Default.Add, "添加课程")
                }
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is ScheduleUiState.Loading -> Box(
                Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            is ScheduleUiState.Empty -> Box(Modifier.fillMaxSize().padding(innerPadding)) {
                EmptyScheduleHint()
            }

            is ScheduleUiState.Success -> {
                Column(Modifier.fillMaxSize().padding(innerPadding)) {
                    // Stats + quote card
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly) {
                                StatChip("📚 ${state.courses.size}门课", MaterialTheme.colorScheme.primary)
                                StatChip("📝 ${state.pendingCount}待交", Color(0xFFE53935))
                                StatChip("📅 第${currentWeek}周", Color(0xFF43A047))
                            }
                            Spacer(Modifier.height(8.dp))
                            if (state.isHitokotoLoading) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(8.dp))
                                    Text("加载格言...", style = MaterialTheme.typography.labelSmall)
                                }
                            } else if (state.hitokoto != null) {
                                Text("💡 ${state.hitokoto.content}", style = MaterialTheme.typography.bodySmall)
                                Text(state.hitokoto.source, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(top = 2.dp))
                            } else {
                                Text("💡 学而不思则罔，思而不学则殆。", style = MaterialTheme.typography.bodySmall)
                                Text("—— 孔子", style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            }
                        }
                    }

                    // Timetable grid
                    Box(Modifier.fillMaxSize().padding(top = 4.dp)) {
                        val hScroll = rememberScrollState()
                        val vScroll = rememberScrollState()

                        Column(Modifier.verticalScroll(vScroll).width(TIME_COL_WIDTH.dp).background(MaterialTheme.colorScheme.surface)) {
                            Box(Modifier.height(HEADER_HEIGHT.dp), contentAlignment = Alignment.Center) {
                                Text("时间", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            for (i in 0 until PERIOD_COUNT) {
                                Box(Modifier.height(PERIOD_HEIGHT.dp).fillMaxWidth().border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${i + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text(PERIOD_TIMES[i], fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }

                        Column(Modifier.fillMaxSize().padding(start = TIME_COL_WIDTH.dp).horizontalScroll(hScroll).verticalScroll(vScroll)) {
                            Row {
                                for (day in 1..7) {
                                    if (!showWeekend && day >= 6) continue
                                    val cal = java.util.Calendar.getInstance()
                                    val today = cal.get(java.util.Calendar.DAY_OF_WEEK)
                                    val todayAdj = if (today == 1) 7 else today - 1
                                    Box(Modifier.width(CELL_WIDTH.dp).height(HEADER_HEIGHT.dp).border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                        val isToday = day == todayAdj
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(DAY_LABELS[day - 1], style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
                                            if (isToday) Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                        }
                                    }
                                }
                            }

                            for (period in 0 until PERIOD_COUNT) {
                                Row {
                                    for (day in 1..7) {
                                        if (!showWeekend && day >= 6) continue
                                        Box(Modifier.width(CELL_WIDTH.dp).height(PERIOD_HEIGHT.dp).border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))) {
                                            val cell = state.courses.filter { it.dayOfWeek == day && it.startPeriod == period + 1 }
                                            if (cell.isNotEmpty()) {
                                                val c = cell.first()
                                                Box(Modifier.fillMaxWidth().height((PERIOD_HEIGHT * c.duration).dp).padding(1.dp)) {
                                                    CourseBlock(
                                                        course = c,
                                                        onClick = { if (isAdmin) onEditCourse(c) },
                                                        showDelete = if (isAdmin) {{ showDeleteDialog = c }} else null
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatChip(text: String, color: Color) {
    Text(text = text, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = color)
}
