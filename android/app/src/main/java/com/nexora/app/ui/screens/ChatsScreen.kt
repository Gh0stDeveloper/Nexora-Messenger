package com.nexora.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.nexora.app.ui.viewmodel.PreviewChatTarget
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatsScreen(
    viewModel: ChatsViewModel,
    profileName: String,
    profilePhone: String,
    showOwnProfile: Boolean,
    onToggleProfile: () -> Unit,
    onCloseProfile: () -> Unit,
    onOpenChat: (chatId: String, recipientId: String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showComposer by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf("Chats") }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            if (activeTab == "Chats") {
                ExtendedFloatingActionButton(
                    onClick = { showComposer = !showComposer },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) { Text(if (showComposer) "Cerrar" else "Nuevo chat") }
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .background(MaterialTheme.colorScheme.background),
        ) {
            MessengerHeader(
                profileName = profileName,
                profilePhone = profilePhone,
                onToggleProfile = onToggleProfile,
                onSync = viewModel::refresh,
            )

            if (showOwnProfile) {
                OwnProfileCard(
                    profileName = profileName,
                    profilePhone = profilePhone,
                    onClose = onCloseProfile,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    listOf("Chats", "Contactos", "Grupos", "Estados").forEach { tab ->
                        FilterChip(
                            selected = activeTab == tab,
                            onClick = { activeTab = tab },
                            label = { Text(tab) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                if (showComposer && activeTab == "Chats") {
                    NewChatCard(
                        targets = viewModel.previewTargets,
                        recipientId = state.recipientId,
                        onRecipientChange = viewModel::updateRecipientId,
                        onPreviewOpen = { target ->
                            val resolved = viewModel.targetFor(target.recipientId)
                            showComposer = false
                            onOpenChat(resolved.first, resolved.second)
                        },
                        onManualOpen = {
                            viewModel.newChatTarget()?.let { (chatId, recipientId) ->
                                showComposer = false
                                onOpenChat(chatId, recipientId)
                            }
                        },
                    )
                }

                when (activeTab) {
                    "Contactos" -> PreviewCollectionPanel(
                        title = "Contactos",
                        body = "Agenda preparada para sincronizar usuarios reales desde tu VPS.",
                        rows = viewModel.previewTargets.map { it.title to it.subtitle },
                    )
                    "Grupos" -> PreviewCollectionPanel(
                        title = "Grupos",
                        body = "Grupos cifrados listos para miembros, roles y multimedia privada.",
                        rows = listOf("Nexora Testers" to "Grupo preview local"),
                    )
                    "Estados" -> PreviewCollectionPanel(
                        title = "Estados",
                        body = "Estados cifrados con expiración automática. En preview se muestran datos locales.",
                        rows = listOf("Ghost Developer" to "Probando Nexora Messenger"),
                    )
                    else -> ChatList(
                        chats = state.chats,
                        loading = state.loading,
                        error = state.error,
                        onOpenChat = { chat ->
                            val target = viewModel.openChatTarget(chat)
                            onOpenChat(target.first, target.second)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun MessengerHeader(
    profileName: String,
    profilePhone: String,
    onToggleProfile: () -> Unit,
    onSync: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 3.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = onToggleProfile),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(profileName.firstOrNull()?.uppercase() ?: "N", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text("Nexora", fontSize = 25.sp, fontWeight = FontWeight.Black)
                Text(profilePhone.ifBlank { "Messenger privado" }, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Text("Buscar", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Text("Sync", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onSync))
        }
    }
}

@Composable
private fun OwnProfileCard(profileName: String, profilePhone: String, onClose: () -> Unit) {
    Card(
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Surface(modifier = Modifier.size(70.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primary) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(profileName.firstOrNull()?.uppercase() ?: "N", fontSize = 28.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(profileName, fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(profilePhone.ifBlank { "Sin teléfono visible" }, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f))
                    Text("Disponible", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f))
                }
                Text("Cerrar", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onClose))
            }
            Text("Perfil propio: foto, nombre, estado, privacidad y seguridad quedan centralizados aquí.", color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

@Composable
private fun NewChatCard(
    targets: List<PreviewChatTarget>,
    recipientId: String,
    onRecipientChange: (String) -> Unit,
    onPreviewOpen: (PreviewChatTarget) -> Unit,
    onManualOpen: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Iniciar conversación", fontWeight = FontWeight.Black, fontSize = 18.sp)
            Text("Para probar sin servidor, abre un contacto preview. En producción esto saldrá de tu agenda real.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            targets.forEach { target ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onPreviewOpen(target) }
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Surface(modifier = Modifier.size(42.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(target.title.firstOrNull()?.uppercase() ?: "N", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(target.title, fontWeight = FontWeight.Bold)
                        Text(target.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                    Text("Abrir", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            OutlinedTextField(
                value = recipientId,
                onValueChange = onRecipientChange,
                label = { Text("UID manual avanzado") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = onManualOpen, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Text("Abrir UID manual")
            }
        }
    }
}

@Composable
private fun ChatList(chats: List<ChatEntity>, loading: Boolean, error: String?, onOpenChat: (ChatEntity) -> Unit) {
    if (loading) Text("Sincronizando…", color = MaterialTheme.colorScheme.primary)
    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    if (chats.isEmpty()) {
        PreviewCollectionPanel("Sin conversaciones", "Toca Nuevo chat para abrir una conversación de prueba. Después se llenará desde contactos reales.", emptyList())
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(chats, key = { it.chatId }) { chat ->
            ChatItem(chat = chat, onClick = { onOpenChat(chat) })
        }
    }
}

@Composable
private fun PreviewCollectionPanel(title: String, body: String, rows: List<Pair<String, String>>) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontWeight = FontWeight.Black, fontSize = 19.sp)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
            rows.forEach { row ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(modifier = Modifier.size(38.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(row.first.firstOrNull()?.uppercase() ?: "N", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Column {
                        Text(row.first, fontWeight = FontWeight.Bold)
                        Text(row.second, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
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
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(modifier = Modifier.size(54.dp), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Box(contentAlignment = Alignment.Center) {
                Text((chat.title ?: chat.chatId).firstOrNull()?.uppercase() ?: "N", fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(chat.title ?: chat.chatId, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(formatTime(chat.lastMessageEpochMs), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Text(chat.lastMessagePreview, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(if (chat.isGroup) "Grupo cifrado" else "Chat cifrado", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
        }
    }
}

private fun formatTime(epochMs: Long): String = if (epochMs <= 0) {
    ""
} else {
    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMs))
}
