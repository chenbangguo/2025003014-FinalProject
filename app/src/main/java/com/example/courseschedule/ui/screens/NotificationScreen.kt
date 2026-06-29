package com.example.courseschedule.ui.screens

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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.courseschedule.data.entity.NotificationEntity
import com.example.courseschedule.viewmodel.NotificationUiState
import com.example.courseschedule.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(viewModel: NotificationViewModel, isAdmin: Boolean) {
    val uiState by viewModel.uiState.collectAsState()
    var showPublish by remember { mutableStateOf(false) }

    if (showPublish && isAdmin) {
        PublishDialog(onDismiss = { showPublish = false }, onPublish = { title, content, type ->
            viewModel.publish(title, content, type); showPublish = false
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📢 通知公告") },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (isAdmin) FloatingActionButton(onClick = { showPublish = true }) {
                Icon(Icons.Default.Add, "发布公告")
            }
        }
    ) { innerPadding ->
        when (val state = uiState) {
            is NotificationUiState.Loading -> Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            is NotificationUiState.Empty -> Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Campaign, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Spacer(Modifier.height(16.dp))
                    Text("暂无通知", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (isAdmin) Text("点击右下角 + 发布公告", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
            is NotificationUiState.Success -> LazyColumn(Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.notifications, key = { it.id }) { notif ->
                    NotificationCard(notif, isAdmin = isAdmin, onDelete = { viewModel.delete(notif.id) }, onTogglePin = { viewModel.togglePin(notif.id, !notif.isPinned) })
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun NotificationCard(notif: NotificationEntity, isAdmin: Boolean, onDelete: () -> Unit, onTogglePin: () -> Unit) {
    val df = remember { SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault()) }
    val typeIcon = when (notif.type) {
        "exam" -> "📝"
        "activity" -> "🎉"
        else -> "📢"
    }
    val typeColor = when (notif.type) {
        "exam" -> Color(0xFFE53935)
        "activity" -> Color(0xFFFF9800)
        else -> Color(0xFF1E88E5)
    }
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (notif.isPinned) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(typeIcon, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(notif.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        if (notif.isPinned) Text("📌", style = MaterialTheme.typography.labelSmall)
                    }
                }
                if (isAdmin) {
                    IconButton(onClick = onTogglePin, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.PushPin, "置顶", tint = if (notif.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(18.dp))
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(notif.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 4, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(6.dp))
            Text(df.format(Date(notif.createdAt)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun PublishDialog(onDismiss: () -> Unit, onPublish: (String, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("notice") }
    var titleError by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    val typeLabels = mapOf("notice" to "📢 通知", "exam" to "📝 考试", "activity" to "🎉 活动")

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("发布公告") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it; titleError = false }, label = { Text("标题 *") }, isError = titleError, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))

                // Type selector
                Box {
                    OutlinedTextField(value = typeLabels[type] ?: "", onValueChange = {}, label = { Text("类型") }, readOnly = true, modifier = Modifier.fillMaxWidth(), trailingIcon = { TextButton(onClick = { typeExpanded = true }) { Text("▾") } })
                    DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        typeLabels.forEach { (k, v) ->
                            DropdownMenuItem(text = { Text(v) }, onClick = { type = k; typeExpanded = false })
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = content, onValueChange = { content = it }, label = { Text("内容 *") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (title.isBlank()) { titleError = true; return@Button }
                if (content.isBlank()) { content = title }
                onPublish(title.trim(), content.trim(), type)
            }) { Text("发布") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}
