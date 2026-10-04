package com.nexora.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexora.app.data.local.MessageEntity
import com.nexora.app.ui.viewmodel.ChatDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatDetailScreen(
    viewModel: ChatDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentUid = viewModel.currentUserId.orEmpty()

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ChatTopBar(
                title = readableTitle(state.recipientId),
                subtitle = "Perfil · cifrado · preview",
                onBack = onBack,
            )
        },
        bottomBar = {
            MessageComposer(
                input = state.input,
                loading = state.loading,
                onInputChange = viewModel::updateInput,
                onSend = viewModel::send,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (state.loading) CircularProgressIndicator()
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.messages, key = { it.messageId }) { message ->
                    MessageBubble(
                        message = message,
                        currentUid = currentUid,
                        preview = viewModel.preview(message),
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatTopBar(title: String, subtitle: String, onBack: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Text("←", fontSize = 27.sp, fontWeight = FontWeight.Black) }
            Surface(modifier = Modifier.size(44.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(contentAlignment = Alignment.Center) {
                    Text(title.firstOrNull()?.uppercase() ?: "N", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Black, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 1)
            }
            Text("Perfil", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("⋮", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun MessageComposer(input: String, loading: Boolean, onInputChange: (String) -> Unit, onSend: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("☺", fontSize = 22.sp)
                Text("＋", fontSize = 22.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                OutlinedTextField(
                    value = input,
                    onValueChange = onInputChange,
                    placeholder = { Text("Mensaje") },
                    modifier = Modifier.weight(1f),
                    minLines = 1,
                    maxLines = 4,
                    shape = RoundedCornerShape(24.dp),
                )
                Button(enabled = !loading, onClick = onSend, shape = RoundedCornerShape(22.dp)) { Text("Enviar") }
            }
            Text(
                "Texto, multimedia y stickers se preparan para cifrado local antes del relay.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
private fun MessageBubble(message: MessageEntity, currentUid: String, preview: String) {
    val mine = message.senderId == currentUid
    val alignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (mine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (mine) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (mine) 20.dp else 6.dp,
                bottomEnd = if (mine) 6.dp else 20.dp,
            ),
            color = bubbleColor,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(0.82f),
        ) {
            Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(if (mine) "Tú" else readableTitle(message.senderId), color = textColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(if (message.kind.equals("MEDIA", ignoreCase = true)) "Archivo cifrado · $preview" else preview, color = textColor)
                Text(formatTime(message.timestampMs), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

private fun readableTitle(value: String): String = when {
    value.contains("akira", ignoreCase = true) -> "Akira Preview"
    value.contains("group", ignoreCase = true) -> "Nexora Testers"
    else -> value.replace('_', ' ').replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

private fun formatTime(epochMs: Long): String = if (epochMs <= 0) {
    "Sin hora"
} else {
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMs))
}
