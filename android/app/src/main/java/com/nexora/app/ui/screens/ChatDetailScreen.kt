package com.nexora.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.InsertEmoticon
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexora.app.data.local.MessageEntity
import com.nexora.app.ui.theme.NexoraColors
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
    val listState = rememberLazyListState()
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showAttachmentNotice by remember { mutableStateOf(false) }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = NexoraColors.Amoled,
        topBar = {
            ChatHeader(
                title = readableTitle(state.recipientId),
                onBack = onBack,
                onRefresh = viewModel::refresh,
            )
        },
        bottomBar = {
            ChatComposer(
                input = state.input,
                loading = state.loading,
                showEmojiPicker = showEmojiPicker,
                showAttachmentNotice = showAttachmentNotice,
                onToggleEmoji = {
                    showEmojiPicker = !showEmojiPicker
                    showAttachmentNotice = false
                },
                onToggleAttachment = {
                    showAttachmentNotice = !showAttachmentNotice
                    showEmojiPicker = false
                },
                onEmoji = { emoji -> viewModel.updateInput(state.input + emoji) },
                onInput = viewModel::updateInput,
                onSend = {
                    showEmojiPicker = false
                    showAttachmentNotice = false
                    viewModel.send()
                },
            )
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
                .padding(padding),
        ) {
            if (state.loading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = NexoraColors.Primary,
                    trackColor = NexoraColors.GlassHigh,
                )
            }

            state.error?.let { message ->
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f)),
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp),
            ) {
                item {
                    SecureNotice()
                }
                if (state.messages.isEmpty()) {
                    item {
                        EmptyConversationCard()
                    }
                }
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
private fun ChatHeader(
    title: String,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    Surface(
        color = NexoraColors.Amoled.copy(alpha = 0.98f),
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Filled.ArrowBackIosNew,
                    contentDescription = "Atrás",
                    tint = NexoraColors.TextMain,
                )
            }

            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = NexoraColors.PrimaryDeep,
                border = BorderStroke(1.dp, NexoraColors.Primary.copy(alpha = 0.35f)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        title.firstOrNull()?.uppercase() ?: "N",
                        fontWeight = FontWeight.Black,
                        color = NexoraColors.TextMain,
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
                        "Cifrado extremo a extremo",
                        color = NexoraColors.TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = NexoraColors.GlassHigh,
                border = BorderStroke(1.dp, NexoraColors.Stroke),
            ) {
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = "Sincronizar chat",
                        tint = NexoraColors.TextMain,
                    )
                }
            }
        }
    }
}

@Composable
private fun SecureNotice() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = NexoraColors.Glass,
            border = BorderStroke(1.dp, NexoraColors.Stroke),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = NexoraColors.Primary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    "Mensajes cifrados antes del relay",
                    color = NexoraColors.TextMuted,
                    fontSize = 11.sp,
                )
            }
        }
    }
}

@Composable
private fun EmptyConversationCard() {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = NexoraColors.GlassHigh),
        border = BorderStroke(1.dp, NexoraColors.Stroke),
    ) {
        Column(
            Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                "Conversación lista",
                fontWeight = FontWeight.Black,
                fontSize = 19.sp,
            )
            Text(
                "Escribe el primer mensaje. El contenido se cifra localmente antes de enviarse.",
                color = NexoraColors.TextMuted,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatComposer(
    input: String,
    loading: Boolean,
    showEmojiPicker: Boolean,
    showAttachmentNotice: Boolean,
    onToggleEmoji: () -> Unit,
    onToggleAttachment: () -> Unit,
    onEmoji: (String) -> Unit,
    onInput: (String) -> Unit,
    onSend: () -> Unit,
) {
    Surface(
        color = NexoraColors.Amoled.copy(alpha = 0.98f),
        shadowElevation = 18.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 10.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AnimatedVisibility(showEmojiPicker) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = NexoraColors.GlassHigh,
                    border = BorderStroke(1.dp, NexoraColors.Stroke),
                ) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(11.dp),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        listOf(
                            "😀", "😂", "🥹", "😍", "😎", "😭", "😡", "👍",
                            "🙏", "🔥", "✨", "💯", "❤️", "💙", "👀", "🎮",
                            "🎧", "🚀", "🌙", "⭐", "🍜", "☕", "📎", "🔒",
                        ).forEach { emoji ->
                            Surface(
                                shape = CircleShape,
                                color = NexoraColors.Glass,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clickable { onEmoji(emoji) },
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(emoji, fontSize = 21.sp)
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(showAttachmentNotice) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = NexoraColors.Glass,
                    border = BorderStroke(1.dp, NexoraColors.Stroke),
                ) {
                    Text(
                        "Multimedia todavía no está habilitada en este relay; el botón queda visible sin simular un envío.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 13.dp, vertical = 9.dp),
                        color = NexoraColors.TextMuted,
                        fontSize = 11.sp,
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                IconButton(onClick = onToggleEmoji) {
                    Icon(
                        Icons.Filled.InsertEmoticon,
                        contentDescription = "Emojis",
                        tint = if (showEmojiPicker) NexoraColors.MintSoft else NexoraColors.Primary,
                    )
                }

                IconButton(onClick = onToggleAttachment) {
                    Icon(
                        Icons.Filled.AttachFile,
                        contentDescription = "Adjuntar",
                        tint = if (showAttachmentNotice) NexoraColors.MintSoft else NexoraColors.Primary,
                    )
                }

                OutlinedTextField(
                    value = input,
                    onValueChange = onInput,
                    placeholder = { Text("Mensaje") },
                    modifier = Modifier.weight(1f),
                    minLines = 1,
                    maxLines = 4,
                    shape = RoundedCornerShape(26.dp),
                )

                Surface(
                    shape = CircleShape,
                    color = if (input.isBlank() || loading) NexoraColors.GlassHigh else NexoraColors.Primary,
                ) {
                    IconButton(
                        enabled = !loading && input.isNotBlank(),
                        onClick = onSend,
                    ) {
                        Icon(
                            Icons.Filled.Send,
                            contentDescription = "Enviar",
                            tint = if (input.isBlank() || loading) NexoraColors.TextMuted else NexoraColors.Amoled,
                        )
                    }
                }
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
    val bubbleColor = if (mine) NexoraColors.BubbleMine else NexoraColors.BubbleOther
    val corner = RoundedCornerShape(
        topStart = 21.dp,
        topEnd = 21.dp,
        bottomStart = if (mine) 21.dp else 7.dp,
        bottomEnd = if (mine) 7.dp else 21.dp,
    )

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment,
    ) {
        Surface(
            shape = corner,
            color = bubbleColor,
            border = BorderStroke(1.dp, NexoraColors.Stroke),
            modifier = Modifier.fillMaxWidth(0.80f),
        ) {
            Column(
                Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (!mine) {
                    Text(
                        readableTitle(message.senderId),
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = NexoraColors.MintSoft,
                    )
                }

                Text(
                    text = if (message.kind.equals("MEDIA", true)) {
                        message.mediaName?.let { "$it · $preview" } ?: "Archivo cifrado · $preview"
                    } else {
                        preview
                    },
                    color = NexoraColors.TextMain,
                    fontSize = 15.sp,
                )

                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        formatTime(message.timestampMs),
                        color = NexoraColors.TextMuted,
                        fontSize = 10.sp,
                    )
                    if (mine) {
                        Icon(
                            imageVector = if (message.read) Icons.Filled.DoneAll else Icons.Filled.Check,
                            contentDescription = when {
                                message.read -> "Leído"
                                message.delivered -> "Entregado"
                                else -> "Enviado"
                            },
                            tint = if (message.read) NexoraColors.Primary else NexoraColors.TextMuted,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun readableTitle(value: String): String = when {
    value.contains("akira", true) -> "Akira Preview"
    value.contains("kenji", true) -> "Kenji QA"
    value.contains("miko", true) -> "Miko Dev"
    value.contains("group", true) -> "Nexora Testers"
    else -> value
        .replace('_', ' ')
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

private fun formatTime(epochMs: Long): String {
    return if (epochMs <= 0) {
        ""
    } else {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMs))
    }
}
