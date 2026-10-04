package com.nexora.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
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

private enum class MessengerTab(val label: String, val icon: ImageVector) {
    Chats("Chats", Icons.Filled.Chat),
    Contacts("Contactos", Icons.Filled.Person),
    Groups("Grupos", Icons.Filled.Group),
    Status("Estados", Icons.Filled.Chat),
}

private data class PreviewPerson(
    val uid: String,
    val name: String,
    val phone: String,
    val status: String,
    val avatar: String,
)

private data class PreviewGroup(
    val id: String,
    val name: String,
    val members: String,
    val description: String,
)

private data class PreviewStatus(
    val id: String,
    val owner: String,
    val body: String,
    val time: String,
)

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
    var activeTab by remember { mutableStateOf(MessengerTab.Chats) }
    var searchQuery by remember { mutableStateOf("") }
    var showComposer by remember { mutableStateOf(false) }
    var selectedPerson by remember { mutableStateOf<PreviewPerson?>(null) }
    var ownNameDraft by remember(profileName) { mutableStateOf(profileName.ifBlank { "Ghost Developer" }) }
    var ownStatusDraft by remember { mutableStateOf("Disponible") }
    var editingOwnProfile by remember { mutableStateOf(false) }

    val previewPeople = remember {
        listOf(
            PreviewPerson("preview_friend_akira", "Akira Preview", "+52 668 000 1122", "Probando Nexora Messenger", "A"),
            PreviewPerson("preview_friend_miko", "Miko Dev", "+52 668 000 3344", "Disponible para pruebas", "M"),
            PreviewPerson("preview_friend_kenji", "Kenji QA", "+52 668 000 5566", "Revisando cifrado", "K"),
        )
    }
    val previewGroups = remember {
        listOf(
            PreviewGroup("group_nexora_testers", "Nexora Testers", "4 miembros", "Grupo de pruebas para mensajes, multimedia y estados."),
            PreviewGroup("group_design_review", "Design Review", "3 miembros", "Revisión de UI/UX y experiencia mobile."),
        )
    }
    val previewStatuses = remember {
        listOf(
            PreviewStatus("status_me", ownNameDraft, "Trabajando en Nexora Messenger", "Ahora"),
            PreviewStatus("status_akira", "Akira Preview", "Probando chats cifrados", "22:40"),
            PreviewStatus("status_miko", "Miko Dev", "Nuevo diseño en revisión", "21:58"),
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            MessengerHeader(
                profileName = ownNameDraft,
                profilePhone = profilePhone,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onToggleProfile = onToggleProfile,
                onSync = viewModel::refresh,
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                MessengerTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab; showComposer = false },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
        floatingActionButton = {
            when (activeTab) {
                MessengerTab.Chats -> ExtendedFloatingActionButton(
                    onClick = { showComposer = !showComposer },
                    icon = { Icon(if (showComposer) Icons.Filled.Close else Icons.Filled.Add, contentDescription = null) },
                    text = { Text(if (showComposer) "Cerrar" else "Nuevo chat") },
                )
                MessengerTab.Contacts -> ExtendedFloatingActionButton(
                    onClick = { selectedPerson = previewPeople.first() },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    text = { Text("Ver perfil") },
                )
                MessengerTab.Groups -> ExtendedFloatingActionButton(
                    onClick = { onOpenChat("group_nexora_testers", "group_nexora_testers") },
                    icon = { Icon(Icons.Filled.Group, contentDescription = null) },
                    text = { Text("Abrir grupo") },
                )
                MessengerTab.Status -> ExtendedFloatingActionButton(
                    onClick = {},
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text("Nuevo estado") },
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(showOwnProfile) {
                OwnProfileCard(
                    profileName = ownNameDraft,
                    profilePhone = profilePhone,
                    status = ownStatusDraft,
                    editing = editingOwnProfile,
                    onNameChange = { ownNameDraft = it.take(40) },
                    onStatusChange = { ownStatusDraft = it.take(80) },
                    onEdit = { editingOwnProfile = true },
                    onSave = { editingOwnProfile = false },
                    onClose = { editingOwnProfile = false; onCloseProfile() },
                )
            }

            AnimatedVisibility(showComposer && activeTab == MessengerTab.Chats) {
                NewChatCard(
                    recipientId = state.recipientId,
                    people = previewPeople,
                    onRecipientChange = viewModel::updateRecipientId,
                    onOpenPreview = { person -> onOpenChat(viewModel.previewChatFor(person.uid).first, person.uid) },
                    onOpenAdvanced = {
                        viewModel.newChatTarget()?.let { (chatId, recipientId) ->
                            showComposer = false
                            onOpenChat(chatId, recipientId)
                        }
                    },
                )
            }

            when (activeTab) {
                MessengerTab.Chats -> ChatList(
                    chats = state.chats,
                    query = searchQuery,
                    loading = state.loading,
                    error = state.error,
                    onOpenChat = { chat ->
                        val target = viewModel.openChatTarget(chat)
                        onOpenChat(target.first, target.second)
                    },
                )
                MessengerTab.Contacts -> ContactsSection(
                    people = previewPeople,
                    query = searchQuery,
                    onOpenProfile = { selectedPerson = it },
                    onOpenChat = { person -> onOpenChat(viewModel.previewChatFor(person.uid).first, person.uid) },
                )
                MessengerTab.Groups -> GroupsSection(
                    groups = previewGroups,
                    query = searchQuery,
                    onOpenGroup = { group -> onOpenChat(group.id, group.id) },
                )
                MessengerTab.Status -> StatusSection(statuses = previewStatuses, query = searchQuery)
            }
        }
    }

    selectedPerson?.let { person ->
        UserProfileDialog(
            person = person,
            onDismiss = { selectedPerson = null },
            onMessage = {
                selectedPerson = null
                onOpenChat(viewModel.previewChatFor(person.uid).first, person.uid)
            },
        )
    }
}

@Composable
private fun MessengerHeader(
    profileName: String,
    profilePhone: String,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onToggleProfile: () -> Unit,
    onSync: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 3.dp) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Avatar(text = profileName, modifier = Modifier.clickable(onClick = onToggleProfile))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Nexora", fontSize = 25.sp, fontWeight = FontWeight.Black)
                    Text(profilePhone.ifBlank { "Messenger privado" }, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                IconButton(onClick = onSync) { Icon(Icons.Filled.Sync, contentDescription = "Sincronizar") }
                IconButton(onClick = onToggleProfile) { Icon(Icons.Filled.MoreVert, contentDescription = "Más") }
            }
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Buscar") },
                placeholder = { Text("Buscar chats, contactos, grupos o estados") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
            )
        }
    }
}

@Composable
private fun OwnProfileCard(
    profileName: String,
    profilePhone: String,
    status: String,
    editing: Boolean,
    onNameChange: (String) -> Unit,
    onStatusChange: (String) -> Unit,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
) {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Avatar(text = profileName, large = true)
                Column(modifier = Modifier.weight(1f)) {
                    Text("Mi perfil", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f))
                    if (editing) {
                        OutlinedTextField(value = profileName, onValueChange = onNameChange, label = { Text("Nombre") }, singleLine = true)
                    } else {
                        Text(profileName, fontSize = 22.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Text(profilePhone.ifBlank { "Sin teléfono visible" }, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f))
                }
                IconButton(onClick = if (editing) onSave else onEdit) {
                    Icon(if (editing) Icons.Filled.Check else Icons.Filled.Edit, contentDescription = null)
                }
                IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Cerrar") }
            }
            if (editing) {
                OutlinedTextField(value = status, onValueChange = onStatusChange, label = { Text("Estado") }, modifier = Modifier.fillMaxWidth())
            } else {
                Text(status, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            Text("Privacidad, seguridad, foto, nombre y estado quedan centralizados aquí.", color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f), fontSize = 12.sp)
        }
    }
}

@Composable
private fun NewChatCard(
    recipientId: String,
    people: List<PreviewPerson>,
    onRecipientChange: (String) -> Unit,
    onOpenPreview: (PreviewPerson) -> Unit,
    onOpenAdvanced: () -> Unit,
) {
    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Nuevo chat", fontWeight = FontWeight.Black, fontSize = 19.sp)
            Text("Elige un contacto de prueba. El UID queda disponible solo para revisión técnica.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            people.take(2).forEach { person ->
                ContactRow(person = person, onOpenProfile = {}, onOpenChat = { onOpenPreview(person) })
            }
            OutlinedTextField(value = recipientId, onValueChange = onRecipientChange, label = { Text("UID avanzado") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Button(onClick = onOpenAdvanced, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) { Text("Abrir UID avanzado") }
        }
    }
}

@Composable
private fun ChatList(chats: List<ChatEntity>, query: String, loading: Boolean, error: String?, onOpenChat: (ChatEntity) -> Unit) {
    if (loading) Text("Sincronizando…", color = MaterialTheme.colorScheme.primary)
    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    val filtered = chats.filter { chat ->
        query.isBlank() || (chat.title ?: chat.chatId).contains(query, ignoreCase = true) || chat.lastMessagePreview.contains(query, ignoreCase = true)
    }
    if (filtered.isEmpty()) {
        EmptyPanel("Sin conversaciones", "Toca Nuevo chat para abrir una conversación de prueba o busca otro término.")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(filtered, key = { it.chatId }) { chat -> ChatItem(chat = chat, onClick = { onOpenChat(chat) }) }
    }
}

@Composable
private fun ContactsSection(people: List<PreviewPerson>, query: String, onOpenProfile: (PreviewPerson) -> Unit, onOpenChat: (PreviewPerson) -> Unit) {
    val filtered = people.filter { query.isBlank() || it.name.contains(query, true) || it.phone.contains(query, true) || it.status.contains(query, true) }
    if (filtered.isEmpty()) {
        EmptyPanel("Sin contactos", "No hay contactos que coincidan con la búsqueda.")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(filtered, key = { it.uid }) { person -> ContactRow(person, onOpenProfile = { onOpenProfile(person) }, onOpenChat = { onOpenChat(person) }) }
    }
}

@Composable
private fun GroupsSection(groups: List<PreviewGroup>, query: String, onOpenGroup: (PreviewGroup) -> Unit) {
    val filtered = groups.filter { query.isBlank() || it.name.contains(query, true) || it.description.contains(query, true) }
    if (filtered.isEmpty()) {
        EmptyPanel("Sin grupos", "No hay grupos que coincidan con la búsqueda.")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filtered, key = { it.id }) { group ->
            Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth().clickable { onOpenGroup(group) }) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Avatar(group.name)
                    Column(Modifier.weight(1f)) {
                        Text(group.name, fontWeight = FontWeight.Black)
                        Text(group.description, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Text(group.members, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                    Icon(Icons.Filled.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun StatusSection(statuses: List<PreviewStatus>, query: String) {
    val filtered = statuses.filter { query.isBlank() || it.owner.contains(query, true) || it.body.contains(query, true) }
    if (filtered.isEmpty()) {
        EmptyPanel("Sin estados", "No hay estados que coincidan con la búsqueda.")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(filtered, key = { it.id }) { status ->
            Card(shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Avatar(status.owner)
                    Column(Modifier.weight(1f)) {
                        Text(status.owner, fontWeight = FontWeight.Black)
                        Text(status.body, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(status.time, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(person: PreviewPerson, onOpenProfile: () -> Unit, onOpenChat: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onOpenProfile).padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Avatar(person.avatar)
        Column(modifier = Modifier.weight(1f)) {
            Text(person.name, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(person.status, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(person.phone, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
        }
        IconButton(onClick = onOpenChat) { Icon(Icons.Filled.Chat, contentDescription = "Mensaje") }
    }
}

@Composable
private fun UserProfileDialog(person: PreviewPerson, onDismiss: () -> Unit, onMessage: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { Button(onClick = onMessage) { Text("Mensaje") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
        icon = { Avatar(person.name, large = true) },
        title = { Text(person.name, fontWeight = FontWeight.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(person.phone)
                Text(person.status, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Perfil ajeno preparado para foto, estado, bloqueo, privacidad y acciones de seguridad.")
            }
        },
    )
}

@Composable
private fun ChatItem(chat: ChatEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).clickable(onClick = onClick).padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Avatar(chat.title ?: chat.chatId)
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

@Composable
private fun EmptyPanel(title: String, body: String) {
    Card(shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, fontWeight = FontWeight.Black, fontSize = 19.sp)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Avatar(text: String, modifier: Modifier = Modifier, large: Boolean = false) {
    val size = if (large) 70.dp else 52.dp
    Surface(modifier = modifier.size(size), shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
        Box(contentAlignment = Alignment.Center) {
            Text(text.firstOrNull()?.uppercase() ?: "N", fontSize = if (large) 28.sp else 18.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

private fun formatTime(epochMs: Long): String = if (epochMs <= 0) "" else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMs))
