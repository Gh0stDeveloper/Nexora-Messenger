package com.nexora.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.DonutLarge
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexora.app.data.local.ChatEntity
import com.nexora.app.ui.theme.NexoraColors
import com.nexora.app.ui.viewmodel.ChatsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class MessengerTab(val label: String, val icon: ImageVector) {
    Chats("Chats", Icons.Rounded.ChatBubble),
    Contacts("Contactos", Icons.Rounded.Person),
    Groups("Grupos", Icons.Rounded.Groups),
    Status("Estados", Icons.Rounded.DonutLarge),
}

private data class PreviewPerson(val uid: String, val name: String, val phone: String, val status: String, val avatar: String)
private data class PreviewGroup(val id: String, val name: String, val members: String, val description: String)
private data class PreviewStatus(val id: String, val owner: String, val body: String, val time: String)

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

    val people = remember {
        listOf(
            PreviewPerson("preview_friend_akira", "Akira Preview", "+52 668 000 1122", "Probando Nexora Messenger", "A"),
            PreviewPerson("preview_friend_miko", "Miko Dev", "+52 668 000 3344", "Disponible para pruebas", "M"),
            PreviewPerson("preview_friend_kenji", "Kenji QA", "+52 668 000 5566", "Revisando cifrado", "K"),
        )
    }
    val groups = remember {
        listOf(
            PreviewGroup("group_nexora_testers", "Nexora Testers", "4 miembros", "Mensajes, multimedia y estados cifrados."),
            PreviewGroup("group_design_review", "Design Review", "3 miembros", "Revisión estricta de UI/UX mobile."),
        )
    }
    val statuses = remember(ownNameDraft) {
        listOf(
            PreviewStatus("status_me", ownNameDraft, "Trabajando en Nexora Messenger", "Ahora"),
            PreviewStatus("status_akira", "Akira Preview", "Probando chats cifrados", "22:40"),
            PreviewStatus("status_miko", "Miko Dev", "Nuevo diseño en revisión", "21:58"),
        )
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = NexoraColors.Amoled,
        topBar = {
            IosMessengerHeader(
                title = activeTab.label,
                profileName = ownNameDraft,
                profilePhone = profilePhone,
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onProfile = onToggleProfile,
                onSync = viewModel::refresh,
            )
        },
        bottomBar = {
            IosBottomBar(activeTab = activeTab, onTab = { activeTab = it; showComposer = false })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (activeTab) {
                        MessengerTab.Chats -> showComposer = !showComposer
                        MessengerTab.Contacts -> selectedPerson = people.first()
                        MessengerTab.Groups -> onOpenChat(groups.first().id, groups.first().id)
                        MessengerTab.Status -> Unit
                    }
                },
                containerColor = NexoraColors.Primary,
                contentColor = NexoraColors.Amoled,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Crear")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NexoraColors.Amoled)
                .padding(padding)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AnimatedVisibility(showOwnProfile) {
                OwnProfileSheet(
                    name = ownNameDraft,
                    phone = profilePhone,
                    status = ownStatusDraft,
                    editing = editingOwnProfile,
                    onName = { ownNameDraft = it.take(42) },
                    onStatus = { ownStatusDraft = it.take(90) },
                    onEdit = { editingOwnProfile = true },
                    onSave = { editingOwnProfile = false },
                    onClose = { editingOwnProfile = false; onCloseProfile() },
                )
            }
            AnimatedVisibility(showComposer && activeTab == MessengerTab.Chats) {
                NewChatSheet(
                    recipientId = state.recipientId,
                    people = people,
                    onRecipient = viewModel::updateRecipientId,
                    onPreview = { person -> onOpenChat(viewModel.previewChatFor(person.uid).first, person.uid) },
                    onAdvanced = {
                        viewModel.newChatTarget()?.let { target ->
                            showComposer = false
                            onOpenChat(target.first, target.second)
                        }
                    },
                )
            }

            when (activeTab) {
                MessengerTab.Chats -> ChatList(state.chats, searchQuery, state.loading, state.error) { chat ->
                    val target = viewModel.openChatTarget(chat)
                    onOpenChat(target.first, target.second)
                }
                MessengerTab.Contacts -> ContactsSection(people, searchQuery, { selectedPerson = it }) { person ->
                    onOpenChat(viewModel.previewChatFor(person.uid).first, person.uid)
                }
                MessengerTab.Groups -> GroupsSection(groups, searchQuery) { group -> onOpenChat(group.id, group.id) }
                MessengerTab.Status -> StatusSection(statuses, searchQuery)
            }
        }
    }

    selectedPerson?.let { person ->
        UserProfileDialog(person = person, onDismiss = { selectedPerson = null }) {
            selectedPerson = null
            onOpenChat(viewModel.previewChatFor(person.uid).first, person.uid)
        }
    }
}

@Composable
private fun IosMessengerHeader(
    title: String,
    profileName: String,
    profilePhone: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onProfile: () -> Unit,
    onSync: () -> Unit,
) {
    Surface(color = NexoraColors.Amoled.copy(alpha = 0.98f), tonalElevation = 0.dp, shadowElevation = 0.dp) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Avatar(profileName, modifier = Modifier.clickable(onClick = onProfile))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 34.sp, lineHeight = 36.sp, fontWeight = FontWeight.Black, color = NexoraColors.TextMain)
                    Text(profilePhone.ifBlank { "Nexora privado" }, color = NexoraColors.TextMuted, fontSize = 13.sp)
                }
                RoundIcon(Icons.Filled.Sync, "Sincronizar", onSync)
                RoundIcon(Icons.Filled.MoreHoriz, "Perfil", onProfile)
            }
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = NexoraColors.Glass,
                border = androidx.compose.foundation.BorderStroke(1.dp, NexoraColors.Stroke),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = NexoraColors.TextMuted)
                    androidx.compose.foundation.text.BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = NexoraColors.TextMain),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (query.isBlank()) Text("Buscar", color = NexoraColors.TextMuted, fontSize = 16.sp)
                            inner()
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun IosBottomBar(activeTab: MessengerTab, onTab: (MessengerTab) -> Unit) {
    Surface(color = NexoraColors.Amoled.copy(alpha = 0.96f), shadowElevation = 12.dp) {
        NavigationBar(
            containerColor = NexoraColors.Glass,
            modifier = Modifier.navigationBarsPadding().padding(horizontal = 10.dp, vertical = 8.dp).clip(RoundedCornerShape(28.dp)),
        ) {
            MessengerTab.values().forEach { tab ->
                NavigationBarItem(
                    selected = activeTab == tab,
                    onClick = { onTab(tab) },
                    icon = { Icon(tab.icon, contentDescription = tab.label) },
                    label = { Text(tab.label) },
                )
            }
        }
    }
}

@Composable
private fun RoundIcon(icon: ImageVector, label: String, onClick: () -> Unit) {
    Surface(shape = CircleShape, color = NexoraColors.GlassHigh, border = androidx.compose.foundation.BorderStroke(1.dp, NexoraColors.Stroke)) {
        IconButton(onClick = onClick) { Icon(icon, contentDescription = label, tint = NexoraColors.TextMain) }
    }
}

@Composable
private fun Avatar(text: String, modifier: Modifier = Modifier, large: Boolean = false) {
    val size = if (large) 74.dp else 52.dp
    Surface(modifier = modifier.size(size), shape = CircleShape, color = NexoraColors.PrimaryDeep) {
        Box(contentAlignment = Alignment.Center) {
            Text(text.firstOrNull()?.uppercase() ?: "N", fontWeight = FontWeight.Black, fontSize = if (large) 30.sp else 20.sp, color = NexoraColors.TextMain)
        }
    }
}

@Composable
private fun OwnProfileSheet(
    name: String,
    phone: String,
    status: String,
    editing: Boolean,
    onName: (String) -> Unit,
    onStatus: (String) -> Unit,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
) {
    PremiumCard {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Avatar(name, large = true)
            Column(modifier = Modifier.weight(1f)) {
                Text("Mi perfil", color = NexoraColors.TextMuted, fontSize = 13.sp)
                if (editing) OutlinedTextField(name, onName, label = { Text("Nombre") }, singleLine = true) else Text(name, fontWeight = FontWeight.Black, fontSize = 24.sp)
                Text(phone.ifBlank { "Sin teléfono visible" }, color = NexoraColors.TextMuted)
            }
            IconButton(onClick = if (editing) onSave else onEdit) { Icon(if (editing) Icons.Filled.Check else Icons.Filled.Edit, contentDescription = null) }
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = null) }
        }
        Spacer(Modifier.height(10.dp))
        if (editing) OutlinedTextField(status, onStatus, label = { Text("Estado") }, modifier = Modifier.fillMaxWidth()) else Text(status, color = NexoraColors.MintSoft)
        Text("Foto, nombre, estado, privacidad, seguridad y sesiones se administran desde aquí.", color = NexoraColors.TextMuted, fontSize = 12.sp)
    }
}

@Composable
private fun NewChatSheet(recipientId: String, people: List<PreviewPerson>, onRecipient: (String) -> Unit, onPreview: (PreviewPerson) -> Unit, onAdvanced: () -> Unit) {
    PremiumCard {
        Text("Nuevo chat", fontWeight = FontWeight.Black, fontSize = 22.sp)
        Text("Selecciona un contacto o usa UID avanzado para pruebas internas.", color = NexoraColors.TextMuted)
        people.take(2).forEach { person -> ContactRow(person, {}, { onPreview(person) }) }
        OutlinedTextField(recipientId, onRecipient, label = { Text("UID avanzado") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Button(onClick = onAdvanced, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) { Text("Abrir UID") }
    }
}

@Composable
private fun ChatList(chats: List<ChatEntity>, query: String, loading: Boolean, error: String?, onOpen: (ChatEntity) -> Unit) {
    if (loading) Text("Sincronizando…", color = NexoraColors.Primary)
    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    val filtered = chats.filter { query.isBlank() || (it.title ?: it.chatId).contains(query, true) || it.lastMessagePreview.contains(query, true) }
    if (filtered.isEmpty()) {
        EmptyPanel("Sin conversaciones", "Toca + para iniciar una conversación de prueba.")
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        items(filtered, key = { it.chatId }) { chat -> ChatRow(chat, onOpen) }
    }
}

@Composable
private fun ChatRow(chat: ChatEntity, onOpen: (ChatEntity) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).clickable { onOpen(chat) }.padding(horizontal = 6.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Avatar(chat.title ?: chat.chatId)
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(prettyTitle(chat.title ?: chat.chatId), fontWeight = FontWeight.Bold, fontSize = 17.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                Text(formatTime(chat.lastMessageEpochMs), color = NexoraColors.TextMuted, fontSize = 12.sp)
            }
            Text(chat.lastMessagePreview, color = NexoraColors.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(if (chat.isGroup) "Grupo cifrado" else "Chat cifrado", color = NexoraColors.Primary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun ContactsSection(people: List<PreviewPerson>, query: String, onProfile: (PreviewPerson) -> Unit, onChat: (PreviewPerson) -> Unit) {
    val filtered = people.filter { query.isBlank() || it.name.contains(query, true) || it.phone.contains(query, true) || it.status.contains(query, true) }
    if (filtered.isEmpty()) return EmptyPanel("Sin contactos", "No hay contactos con ese término.")
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) { items(filtered, key = { it.uid }) { ContactRow(it, { onProfile(it) }, { onChat(it) }) } }
}

@Composable
private fun ContactRow(person: PreviewPerson, onOpenProfile: () -> Unit, onOpenChat: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).clickable(onClick = onOpenProfile).padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Avatar(person.avatar)
        Column(modifier = Modifier.weight(1f)) {
            Text(person.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Text(person.status, color = NexoraColors.TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(person.phone, color = NexoraColors.Primary, fontSize = 12.sp)
        }
        RoundIcon(Icons.Filled.Send, "Mensaje", onOpenChat)
    }
}

@Composable
private fun GroupsSection(groups: List<PreviewGroup>, query: String, onOpen: (PreviewGroup) -> Unit) {
    val filtered = groups.filter { query.isBlank() || it.name.contains(query, true) || it.description.contains(query, true) }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(filtered, key = { it.id }) { group ->
            PremiumCard(modifier = Modifier.clickable { onOpen(group) }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Avatar(group.name)
                    Column(Modifier.weight(1f)) {
                        Text(group.name, fontWeight = FontWeight.Black, fontSize = 19.sp)
                        Text(group.members, color = NexoraColors.Primary, fontSize = 12.sp)
                        Text(group.description, color = NexoraColors.TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusSection(statuses: List<PreviewStatus>, query: String) {
    val filtered = statuses.filter { query.isBlank() || it.owner.contains(query, true) || it.body.contains(query, true) }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(filtered, key = { it.id }) { status ->
            PremiumCard {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Surface(modifier = Modifier.size(58.dp), shape = CircleShape, color = NexoraColors.Primary.copy(alpha = 0.18f), border = androidx.compose.foundation.BorderStroke(2.dp, NexoraColors.Primary)) {
                        Box(contentAlignment = Alignment.Center) { Text(status.owner.firstOrNull()?.uppercase() ?: "N", fontWeight = FontWeight.Black) }
                    }
                    Column(Modifier.weight(1f)) {
                        Text(status.owner, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(status.body, color = NexoraColors.TextMuted)
                    }
                    Text(status.time, color = NexoraColors.TextMuted, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun UserProfileDialog(person: PreviewPerson, onDismiss: () -> Unit, onMessage: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NexoraColors.GlassHigh,
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Avatar(person.name, large = true)
                Text(person.name, fontWeight = FontWeight.Black, fontSize = 24.sp)
                Text(person.status, color = NexoraColors.TextMuted)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(person.phone)
                Text("Perfil cifrado. En producción incluirá bloqueo, privacidad, archivos compartidos y seguridad.", color = NexoraColors.TextMuted)
            }
        },
        confirmButton = { Button(onClick = onMessage) { Text("Enviar mensaje") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
    )
}

@Composable
private fun PremiumCard(modifier: Modifier = Modifier, content: @Composable Column.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = NexoraColors.GlassHigh),
        border = androidx.compose.foundation.BorderStroke(1.dp, NexoraColors.Stroke),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

@Composable
private fun EmptyPanel(title: String, body: String) {
    PremiumCard {
        Text(title, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text(body, color = NexoraColors.TextMuted)
    }
}

private fun prettyTitle(value: String): String = when {
    value.contains("akira", true) -> "Akira Preview"
    value.contains("kenji", true) -> "Kenji QA"
    value.contains("group", true) -> "Nexora Testers"
    else -> value.replace("preview_friend_", "").replace("_preview_ghost_developer", "").replace('_', ' ').replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

private fun formatTime(epochMs: Long): String = if (epochMs <= 0) "" else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMs))
