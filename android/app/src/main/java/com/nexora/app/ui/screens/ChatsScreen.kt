package com.nexora.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexora.app.data.local.ChatEntity
import com.nexora.app.ui.viewmodel.ChatsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatsScreen(
    viewModel: ChatsViewModel,
    onOpenChat: (chatId: String, recipientId: String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Nexora", fontWeight = FontWeight.Black, fontSize = 30.sp, color = MaterialTheme.colorScheme.onPrimary)
                        Text("Messenger", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.76f), fontSize = 13.sp)
                    }
                    Button(enabled = !state.loading, onClick = viewModel::refresh) {
                        Text("Sync")
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = {}, label = { Text("Chats") })
                    AssistChip(onClick = {}, label = { Text("Contactos") })
                    AssistChip(onClick = {}, label = { Text("Grupos") })
                    AssistChip(onClick = {}, label = { Text("Estados") })
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Nuevo chat", fontWeight = FontWeight.Bold)
                    Text(
                        "Por ahora escribe el UID del contacto. La agenda real queda preparada en la base local.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                    )
                    OutlinedTextField(
                        value = state.recipientId,
                        onValueChange = viewModel::updateRecipientId,
                        label = { Text("UID destino") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Button(
                        enabled = !state.loading,
                        onClick = {
                            viewModel.newChatTarget()?.let { (chatId, recipientId) ->
                                onOpenChat(chatId, recipientId)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                    ) {
                        Text("Abrir conversación")
                    }
                }
            }

            if (state.loading) CircularProgressIndicator()
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            if (state.chats.isEmpty()) {
                EmptyMessengerHome()
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.chats, key = { it.chatId }) { chat ->
                        ChatItem(
                            chat = chat,
                            onClick = {
                                val target = viewModel.openChatTarget(chat)
                                onOpenChat(target.first, target.second)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMessengerHome() {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Todo listo para probar", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Cuando envíes mensajes, multimedia, estados o grupos, aparecerán en esta pantalla con estilo de messenger moderno.")
            Text("Emoji pack: Nexora incluye base propia. Assets externos se deben agregar solo con licencia.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ChatItem(chat: ChatEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    (chat.title ?: chat.chatId).firstOrNull()?.uppercase() ?: "N",
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(chat.title ?: chat.chatId, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(chat.lastMessagePreview, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${if (chat.isGroup) "Grupo" else "Chat"} · ${formatTime(chat.lastMessageEpochMs)}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
    }
}

private fun formatTime(epochMs: Long): String = if (epochMs <= 0) {
    "Sin actividad"
} else {
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMs))
}
