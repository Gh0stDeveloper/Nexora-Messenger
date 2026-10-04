package com.nexora.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.primary, shadowElevation = 6.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) { Text("←", color = MaterialTheme.colorScheme.onPrimary, fontSize = 24.sp) }
                Column(Modifier.weight(1f)) {
                    Text("Chat cifrado", fontWeight = FontWeight.Black, fontSize = 22.sp, color = MaterialTheme.colorScheme.onPrimary)
                    Text("${state.recipientId} · online preview", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.76f), fontSize = 12.sp)
                }
                Text("⋮", color = MaterialTheme.colorScheme.onPrimary, fontSize = 24.sp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f))
                .padding(12.dp),
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

            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("☺", fontSize = 22.sp)
                    OutlinedTextField(
                        value = state.input,
                        onValueChange = viewModel::updateInput,
                        placeholder = { Text("Mensaje") },
                        modifier = Modifier.weight(1f),
                        minLines = 1,
                        maxLines = 4,
                        shape = RoundedCornerShape(22.dp),
                    )
                    Button(
                        enabled = !state.loading,
                        onClick = viewModel::send,
                        shape = RoundedCornerShape(20.dp),
                    ) {
                        Text("Enviar")
                    }
                }
                Text(
                    "Cifrado local antes del relay. Multimedia y stickers usan el mismo canal cifrado.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: MessageEntity,
    currentUid: String,
    preview: String,
) {
    val mine = message.senderId == currentUid
    val alignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (mine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (mine) 20.dp else 4.dp,
                bottomEnd = if (mine) 4.dp else 20.dp,
            ),
            color = bubbleColor,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth(0.84f),
        ) {
            Column(Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(if (mine) "Tú" else message.senderId, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(if (message.kind.equals("MEDIA", ignoreCase = true)) "📎 $preview" else preview)
                Text(formatTime(message.timestampMs), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
            }
        }
    }
}

private fun formatTime(epochMs: Long): String = if (epochMs <= 0) {
    "Sin hora"
} else {
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMs))
}
