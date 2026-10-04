package com.nexora.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexora.app.data.local.ChatEntity
import com.nexora.app.data.local.ContactEntity
import com.nexora.app.data.local.GroupEntity
import com.nexora.app.data.local.StatusEntity
import com.nexora.app.ui.theme.NexoraColors
import com.nexora.app.ui.viewmodel.ChatsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class MessengerTab(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
) {
    Chats("Chats", "Conversaciones privadas", Icons.Rounded.ChatBubble),
    Contacts("Contactos", "Personas y claves locales", Icons.Rounded.Person),
    Groups("Grupos", "Espacios compartidos cifrados", Icons.Rounded.Groups),
    Status("Estados", "Actualizaciones de 24 horas", Icons.Rounded.DonutLarge),
}

@Composable
fun ChatsScreen(
    viewModel: ChatsViewModel,
    profileName: String,
    profilePhone: String,
    profileSaving: Boolean,
    profileError: String?,
    showOwnProfile: Boolean,
    onToggleProfile: () -> Unit,
    onCloseProfile: () -> Unit,
    onSaveProfileName: (String) -> Unit,
    onOpenChat: (chatId: String, recipientId: String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var activeTab by remember { mutableStateOf(MessengerTab.Chats) }
    var searchQuery by remember { mutableStateOf("") }
    var showComposer by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<ContactEntity?>(null) }
    var ownNameDraft by remember(profileName) { mutableStateOf(profileName.ifBlank { "Ghost Developer" }) }
    var editingOwnProfile by remember { mutableStateOf(false) }

    LaunchedEffect(profileName, editingOwnProfile) {
        if (!editingOwnProfile) {
            ownNameDraft = profileName.ifBlank { "Ghost Developer" }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = NexoraColors.Amoled,
        topBar = {
            IosMessengerHeader(
                title = activeTab.label,
                subtitle = activeTab.subtitle,
                profileName = ownNameDraft,
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onClearSearch = { searchQuery = "" },
                onProfile = onToggleProfile,
                onSync = viewModel::refresh,
            )
        },
        bottomBar = {
            IosBottomBar(
                activeTab = activeTab,
                onTab = { tab ->
                    activeTab = tab
                    showComposer = false
                    searchQuery = ""
                },
            )
        },
        floatingActionButton = {
            if (activeTab == MessengerTab.Chats) {
                FloatingActionButton(
                    onClick = { showComposer = !showComposer },
                    containerColor = NexoraColors.Primary,
                    contentColor = NexoraColors.Amoled,
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = if (showComposer) Icons.Filled.Close else Icons.Filled.Add,
                        contentDescription = if (showComposer) "Cerrar nuevo chat" else "Nuevo chat",
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(NexoraColors.Amoled, NexoraColors.Ink, NexoraColors.Amoled),
                    ),
                )
                .padding(padding)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnimatedVisibility(showOwnProfile) {
                OwnProfileSheet(
                    name = ownNameDraft,
                    phone = profilePhone,
                    saving = profileSaving,
                    error = profileError,
                    editing = editingOwnProfile,
                    onName = { ownNameDraft = it.take(80) },
                    onEdit = { editingOwnProfile = true },
                    onSave = {
                        onSaveProfileName(ownNameDraft)
                        editingOwnProfile = false
                    },
                    onClose = {
                        editingOwnProfile = false
                        onCloseProfile()
                    },
                )
            }

            AnimatedVisibility(showComposer && activeTab == MessengerTab.Chats) {
                NewChatSheet(
                    recipientId = state.recipientId,
                    contacts = state.contacts,
                    onRecipient = viewModel::updateRecipientId,
                    onContact = { contact ->
                        showComposer = false
                        val target = viewModel.previewChatFor(contact.uid)
                        onOpenChat(target.first, target.second)
                    },
                    onAdvanced = {
                        viewModel.newChatTarget()?.let { target ->
                            showComposer = false
                            onOpenChat(target.first, target.second)
                        }
                    },
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                when (activeTab) {
                    MessengerTab.Chats -> ChatList(
                        chats = state.chats,
                        query = searchQuery,
                        loading = state.loading,
                        error = state.error,
                    ) { chat ->
                        val target = viewModel.openChatTarget(chat)
                        onOpenChat(target.first, target.second)
                    }

                    MessengerTab.Contacts -> ContactsSection(
                        contacts = state.contacts,
                        query = searchQuery,
                        onProfile = { selectedContact = it },
                        onChat = { contact ->
                            val target = viewModel.previewChatFor(contact.uid)
                            onOpenChat(target.first, target.second)
                        },
                    )

                    MessengerTab.Groups -> GroupsSection(
                        groups = state.groups,
                        query = searchQuery,
                    ) { group ->
                        onOpenChat(group.groupId, group.groupId)
                    }

                    MessengerTab.Status -> StatusSection(
                        statuses = state.statuses,
                        query = searchQuery,
                        preview = viewModel::previewStatus,
                    )
                }
            }
        }
    }

    selectedContact?.let { contact ->
        UserProfileDialog(
            contact = contact,
            onDismiss = { selectedContact = null },
            onMessage = {
                selectedContact = null
                val target = viewModel.previewChatFor(contact.uid)
                onOpenChat(target.first, target.second)
            },
        )
    }
}

@Composable
private fun IosMessengerHeader(
    title: String,
    subtitle: String,
    profileName: String,
    query: String,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    onProfile: () -> Unit,
    onSync: () -> Unit,
) {
    Surface(
        color = NexoraColors.Amoled.copy(alpha = 0.98f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 18.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Avatar(
                    text = profileName,
                    modifier = Modifier.clickable(onClick = onProfile),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 32.sp,
                        lineHeight = 35.sp,
                        fontWeight = FontWeight.Black,
                        color = NexoraColors.TextMain,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = NexoraColors.Primary,
                        )
                        Text(
                            text = subtitle,
                            color = NexoraColors.TextMuted,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                RoundIcon(Icons.Filled.Refresh, "Sincronizar", onSync)
                RoundIcon(Icons.Filled.MoreHoriz, "Mi perfil", onProfile)
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = NexoraColors.Glass,
                border = BorderStroke(1.dp, NexoraColors.Stroke),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp),
                ) {
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = null,
                        tint = NexoraColors.TextMuted,
                        modifier = Modifier.size(20.dp),
                    )
                    androidx.compose.foundation.text.BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = NexoraColors.TextMain),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (query.isBlank()) {
                                Text("Buscar en $title", color = NexoraColors.TextMuted, fontSize = 15.sp)
                            }
                            inner()
                        },
                    )
                    if (query.isNotBlank()) {
                        IconButton(
                            onClick = onClearSearch,
                            modifier = Modifier.size(30.dp),
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Limpiar búsqueda",
                                tint = NexoraColors.TextMuted,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IosBottomBar(
    activeTab: MessengerTab,
    onTab: (MessengerTab) -> Unit,
) {
    Surface(
        color = NexoraColors.Amoled.copy(alpha = 0.98f),
        shadowElevation = 18.dp,
    ) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(NexoraColors.Glass)
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            MessengerTab.values().forEach { tab ->
                val selected = activeTab == tab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTab(tab) },
                    shape = RoundedCornerShape(22.dp),
                    color = if (selected) NexoraColors.Primary.copy(alpha = 0.16f) else Color.Transparent,
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (selected) NexoraColors.Primary else NexoraColors.TextMuted,
                            modifier = Modifier.size(22.dp),
                        )
                        Text(
                            text = tab.label,
                            color = if (selected) NexoraColors.Primary else NexoraColors.TextMuted,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RoundIcon(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        shape = CircleShape,
        color = NexoraColors.GlassHigh,
        border = BorderStroke(1.dp, NexoraColors.Stroke),
    ) {
        IconButton(onClick = onClick) {
            Icon(
                icon,
                contentDescription = label,
                tint = NexoraColors.TextMain,
            )
        }
    }
}

@Composable
private fun Avatar(
    text: String,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    val size = if (large) 76.dp else 50.dp
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = NexoraColors.PrimaryDeep,
        border = BorderStroke(1.dp, NexoraColors.Primary.copy(alpha = 0.35f)),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text.firstOrNull()?.uppercase() ?: "N",
                fontWeight = FontWeight.Black,
                fontSize = if (large) 30.sp else 19.sp,
                color = NexoraColors.TextMain,
            )
        }
    }
}

@Composable
private fun OwnProfileSheet(
    name: String,
    phone: String,
    saving: Boolean,
    error: String?,
    editing: Boolean,
    onName: (String) -> Unit,
    onEdit: () -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit,
) {
    PremiumCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Avatar(name, large = true)
            Column(modifier = Modifier.weight(1f)) {
                Text("Mi perfil", color = NexoraColors.TextMuted, fontSize = 12.sp)
                if (editing) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = onName,
                        label = { Text("Nombre") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Black,
                        fontSize = 23.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = phone.ifBlank { "Número privado" },
                    color = NexoraColors.TextMuted,
                    fontSize = 12.sp,
                )
            }
            IconButton(
                enabled = !saving,
                onClick = if (editing) onSave else onEdit,
            ) {
                Icon(
                    if (editing) Icons.Filled.Check else Icons.Filled.Edit,
                    contentDescription = if (editing) "Guardar nombre" else "Editar nombre",
                    tint = NexoraColors.Primary,
                )
            }
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = "Cerrar perfil",
                    tint = NexoraColors.TextMuted,
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = NexoraColors.Glass,
            border = BorderStroke(1.dp, NexoraColors.Stroke),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(9.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = NexoraColors.Primary,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    "La clave E2EE permanece en el dispositivo. El nombre sí se guarda en tu perfil.",
                    color = NexoraColors.TextMuted,
                    fontSize = 12.sp,
                )
            }
        }

        if (saving) {
            Text("Guardando perfil…", color = NexoraColors.Primary, fontSize = 12.sp)
        }
        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
        }
    }
}

@Composable
private fun NewChatSheet(
    recipientId: String,
    contacts: List<ContactEntity>,
    onRecipient: (String) -> Unit,
    onContact: (ContactEntity) -> Unit,
    onAdvanced: () -> Unit,
) {
    PremiumCard {
        Text("Nuevo chat", fontWeight = FontWeight.Black, fontSize = 22.sp)
        Text(
            "Selecciona un contacto guardado o usa un UID para una conversación directa.",
            color = NexoraColors.TextMuted,
        )

        contacts.take(3).forEach { contact ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onContact(contact) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Avatar(contact.name)
                Column(modifier = Modifier.weight(1f)) {
                    Text(contact.name, fontWeight = FontWeight.Bold)
                    Text(contact.status, color = NexoraColors.TextMuted, fontSize = 12.sp)
                }
                Icon(
                    Icons.Filled.Send,
                    contentDescription = "Abrir chat",
                    tint = NexoraColors.Primary,
                )
            }
        }

        OutlinedTextField(
            value = recipientId,
            onValueChange = onRecipient,
            label = { Text("UID del destinatario") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Button(
            enabled = recipientId.isNotBlank(),
            onClick = onAdvanced,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
        ) {
            Text("Abrir conversación")
        }
    }
}

@Composable
private fun ChatList(
    chats: List<ChatEntity>,
    query: String,
    loading: Boolean,
    error: String?,
    onOpen: (ChatEntity) -> Unit,
) {
    val filtered = chats.filter {
        query.isBlank() ||
            (it.title ?: it.chatId).contains(query, true) ||
            it.lastMessagePreview.contains(query, true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (loading) {
            item {
                InlineInfo("Sincronizando conversaciones…")
            }
        }
        error?.let { message ->
            item {
                InlineInfo(message, isError = true)
            }
        }
        if (filtered.isEmpty()) {
            item {
                EmptyPanel(
                    title = if (query.isBlank()) "Sin conversaciones" else "Sin resultados",
                    body = if (query.isBlank()) {
                        "Toca + para iniciar una conversación."
                    } else {
                        "No hay chats que coincidan con “$query”."
                    },
                )
            }
        } else {
            items(filtered, key = { it.chatId }) { chat ->
                ChatRow(chat, onOpen)
            }
        }
    }
}

@Composable
private fun ChatRow(
    chat: ChatEntity,
    onOpen: (ChatEntity) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { onOpen(chat) }
            .padding(horizontal = 6.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Avatar(prettyTitle(chat.title ?: chat.chatId))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = prettyTitle(chat.title ?: chat.chatId),
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatTime(chat.lastMessageEpochMs),
                    color = NexoraColors.TextMuted,
                    fontSize = 11.sp,
                )
            }
            Text(
                text = chat.lastMessagePreview,
                color = NexoraColors.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = NexoraColors.Primary,
                    modifier = Modifier.size(11.dp),
                )
                Text(
                    text = if (chat.isGroup) "Grupo cifrado" else "Chat cifrado",
                    color = NexoraColors.Primary,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun ContactsSection(
    contacts: List<ContactEntity>,
    query: String,
    onProfile: (ContactEntity) -> Unit,
    onChat: (ContactEntity) -> Unit,
) {
    val filtered = contacts.filter {
        query.isBlank() ||
            it.name.contains(query, true) ||
            it.phone.contains(query, true) ||
            it.status.contains(query, true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (filtered.isEmpty()) {
            item {
                EmptyPanel(
                    "Sin contactos",
                    if (query.isBlank()) {
                        "Los contactos guardados aparecerán aquí."
                    } else {
                        "No hay contactos con ese término."
                    },
                )
            }
        } else {
            items(filtered, key = { it.uid }) { contact ->
                ContactRow(
                    contact = contact,
                    onOpenProfile = { onProfile(contact) },
                    onOpenChat = { onChat(contact) },
                )
            }
        }
    }
}

@Composable
private fun ContactRow(
    contact: ContactEntity,
    onOpenProfile: () -> Unit,
    onOpenChat: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onOpenProfile)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Avatar(contact.name)
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(contact.name, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                if (contact.isFavorite) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = "Favorito",
                        tint = NexoraColors.Primary,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            Text(
                contact.status,
                color = NexoraColors.TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(contact.phone, color = NexoraColors.Primary, fontSize = 12.sp)
        }
        RoundIcon(Icons.Filled.Send, "Mensaje", onOpenChat)
    }
}

@Composable
private fun GroupsSection(
    groups: List<GroupEntity>,
    query: String,
    onOpen: (GroupEntity) -> Unit,
) {
    val filtered = groups.filter {
        query.isBlank() ||
            it.title.contains(query, true) ||
            it.memberIds.contains(query, true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (filtered.isEmpty()) {
            item {
                EmptyPanel(
                    "Sin grupos",
                    if (query.isBlank()) {
                        "Los grupos sincronizados aparecerán aquí."
                    } else {
                        "No hay grupos que coincidan con la búsqueda."
                    },
                )
            }
        } else {
            items(filtered, key = { it.groupId }) { group ->
                PremiumCard(
                    modifier = Modifier.clickable { onOpen(group) },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Avatar(group.title)
                        Column(Modifier.weight(1f)) {
                            Text(group.title, fontWeight = FontWeight.Black, fontSize = 18.sp)
                            Text(
                                "${memberCount(group.memberIds)} miembros",
                                color = NexoraColors.Primary,
                                fontSize = 12.sp,
                            )
                            Text(
                                "Grupo local cifrado",
                                color = NexoraColors.TextMuted,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusSection(
    statuses: List<StatusEntity>,
    query: String,
    preview: (StatusEntity) -> String,
) {
    val filtered = statuses.map { it to preview(it) }.filter { (status, body) ->
        query.isBlank() ||
            status.ownerName.contains(query, true) ||
            body.contains(query, true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (filtered.isEmpty()) {
            item {
                EmptyPanel(
                    "Sin estados",
                    if (query.isBlank()) {
                        "Los estados activos aparecerán aquí hasta que expiren."
                    } else {
                        "No hay estados que coincidan con la búsqueda."
                    },
                )
            }
        } else {
            items(filtered, key = { it.first.statusId }) { (status, body) ->
                PremiumCard {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Surface(
                            modifier = Modifier.size(58.dp),
                            shape = CircleShape,
                            color = NexoraColors.Primary.copy(alpha = 0.12f),
                            border = BorderStroke(2.dp, NexoraColors.Primary),
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    status.ownerName.firstOrNull()?.uppercase() ?: "N",
                                    fontWeight = FontWeight.Black,
                                )
                            }
                        }
                        Column(Modifier.weight(1f)) {
                            Text(status.ownerName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text(
                                body,
                                color = NexoraColors.TextMuted,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Text(
                            formatRelativeTime(status.createdAt),
                            color = NexoraColors.TextMuted,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserProfileDialog(
    contact: ContactEntity,
    onDismiss: () -> Unit,
    onMessage: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NexoraColors.GlassHigh,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Avatar(contact.name, large = true)
                Spacer(Modifier.height(10.dp))
                Text(contact.name, fontWeight = FontWeight.Black, fontSize = 24.sp)
                Text(contact.status, color = NexoraColors.TextMuted)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(contact.phone)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NexoraColors.Glass,
                    border = BorderStroke(1.dp, NexoraColors.Stroke),
                ) {
                    Row(
                        modifier = Modifier.padding(11.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = NexoraColors.Primary,
                            modifier = Modifier.size(17.dp),
                        )
                        Text(
                            "Contacto guardado localmente. La conversación usa cifrado antes del relay.",
                            color = NexoraColors.TextMuted,
                            fontSize = 12.sp,
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onMessage) {
                Text("Enviar mensaje")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
    )
}

@Composable
private fun PremiumCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = NexoraColors.GlassHigh),
        border = BorderStroke(1.dp, NexoraColors.Stroke),
    ) {
        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content,
        )
    }
}

@Composable
private fun EmptyPanel(
    title: String,
    body: String,
) {
    PremiumCard {
        Text(title, fontWeight = FontWeight.Black, fontSize = 20.sp)
        Text(body, color = NexoraColors.TextMuted)
    }
}

@Composable
private fun InlineInfo(
    text: String,
    isError: Boolean = false,
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = NexoraColors.Glass,
        border = BorderStroke(
            1.dp,
            if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.45f) else NexoraColors.Stroke,
        ),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = if (isError) MaterialTheme.colorScheme.error else NexoraColors.Primary,
            fontSize = 12.sp,
        )
    }
}

private fun memberCount(memberIds: String): Int {
    return memberIds.split(',').count { it.trim().isNotBlank() }
}

private fun prettyTitle(value: String): String = when {
    value.contains("akira", true) -> "Akira Preview"
    value.contains("kenji", true) -> "Kenji QA"
    value.contains("miko", true) -> "Miko Dev"
    value.contains("group", true) -> "Nexora Testers"
    else -> value
        .replace("preview_friend_", "")
        .replace("_preview_ghost_developer", "")
        .replace('_', ' ')
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

private fun formatTime(epochMs: Long): String {
    return if (epochMs <= 0) "" else {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMs))
    }
}

private fun formatRelativeTime(epochMs: Long): String {
    if (epochMs <= 0) return ""
    val elapsed = (System.currentTimeMillis() - epochMs).coerceAtLeast(0L)
    val minutes = elapsed / 60_000L
    return when {
        minutes < 1 -> "Ahora"
        minutes < 60 -> "Hace ${minutes}m"
        minutes < 1_440 -> "Hace ${minutes / 60}h"
        else -> SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(epochMs))
    }
}
