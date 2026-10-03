package com.nexora.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexora.app.data.local.ChatEntity
import com.nexora.app.ui.viewmodel.ChatsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatsScreen(viewModel: ChatsViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Nexora", fontWeight = FontWeight.Black, fontSize = 30.sp)
                Text("Chats sincronizados con VPS", color = MaterialTheme.colorScheme.primary)
            }
            Button(enabled = !state.loading, onClick = viewModel::refresh) {
                Text("Sync")
            }
        }

        Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Enviar payload cifrado", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = state.recipientId,
                    onValueChange = viewModel::updateRecipientId,
                    label = { Text("UID destino") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.encryptedText,
                    onValueChange = viewModel::updateEncryptedText,
                    label = { Text("Payload cifrado") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(enabled = !state.loading, onClick = viewModel::sendDemoEncryptedMessage, modifier = Modifier.fillMaxWidth()) {
                    Text("Enviar por relay")
                }
            }
        }

        if (state.loading) CircularProgressIndicator()
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        if (state.chats.isEmpty()) {
            Spacer(Modifier.height(12.dp))
            Text("Todavía no hay chats. Cuando envíes o recibas mensajes cifrados, aparecerán aquí.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(state.chats, key = { it.chatId }) { chat ->
                    ChatItem(chat)
                }
            }
        }
    }
}

@Composable
private fun ChatItem(chat: ChatEntity) {
    Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(chat.chatId, fontWeight = FontWeight.Bold)
            Text(chat.lastMessagePreview, color = MaterialTheme.colorScheme.primary)
            Text(
                "Participantes: ${chat.participantIds}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
            )
            Text(
                formatTime(chat.lastMessageEpochMs),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
            )
        }
    }
}

private fun formatTime(epochMs: Long): String = if (epochMs <= 0) {
    "Sin actividad"
} else {
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(epochMs))
}
