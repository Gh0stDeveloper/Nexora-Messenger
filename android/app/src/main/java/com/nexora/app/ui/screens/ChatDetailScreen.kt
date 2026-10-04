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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.InsertEmoticon
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
fun ChatDetailScreen(viewModel: ChatDetailViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val currentUid = viewModel.currentUserId.orEmpty()
    var showEmojiPicker by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = NexoraColors.Amoled,
        topBar = {
            ChatHeader(
                title = readableTitle(state.recipientId),
                subtitle = "Cifrado local · preview",
                onBack = onBack,
            )
        },
        bottomBar = {
            ChatComposer(
                input = state.input,
                loading = state.loading,
                showEmojiPicker = showEmojiPicker,
                onToggleEmoji = { showEmojiPicker = !showEmojiPicker },
                onEmoji = { emoji -> viewModel.updateInput(state.input + emoji) },
                onInput = viewModel::updateInput,
                onSend = { showEmojiPicker = false; viewModel.send() },
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
                .padding(padding)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SecureBanner()
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                reverseLayout = false,
            ) {
                if (state.messages.isEmpty()) {
                    item { EmptyConversationCard() }
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
private fun ChatHeader(title: String, subtitle: String, onBack: () -> Unit) {
    Surface(color = NexoraColors.Amoled.copy(alpha = 0.98f), shadowElevation = 0.dp) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Atrás", tint = NexoraColors.TextMain) }
            Surface(modifier = Modifier.size(48.dp), shape = CircleShape, color = NexoraColors.PrimaryDeep) {
                Box(contentAlignment = Alignment.Center) { Text(title.firstOrNull()?.uppercase() ?: "N", fontWeight = FontWeight.Black, color = NexoraColors.TextMain) }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Black, fontSize = 20.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(13.dp), tint = NexoraColors.Primary)
                    Text(subtitle, color = NexoraColors.TextMuted, fontSize = 12.sp, maxLines = 1)
                }
            }
            IconButton(onClick = {}) { Icon(Icons.Filled.MoreHoriz, contentDescription = "Más", tint = NexoraColors.TextMain) }
        }
    }
}

@Composable
private fun SecureBanner() {
    Surface(shape = RoundedCornerShape(22.dp), color = NexoraColors.Glass, border = BorderStroke(1.dp, NexoraColors.Stroke)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = NexoraColors.Primary)
            Text("Los mensajes se cifran antes de salir del teléfono.", color = NexoraColors.TextMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun EmptyConversationCard() {
    Card(shape = RoundedCornerShape(28.dp), colors = CardDefaults.cardColors(containerColor = NexoraColors.GlassHigh), border = BorderStroke(1.dp, NexoraColors.Stroke)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Conversación lista", fontWeight = FontWeight.Black, fontSize = 20.sp)
            Text("Envía un mensaje, inserta emojis o prueba adjuntos cifrados desde el compositor inferior.", color = NexoraColors.TextMuted)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChatComposer(
    input: String,
    loading: Boolean,
    showEmojiPicker: Boolean,
    onToggleEmoji: () -> Unit,
    onEmoji: (String) -> Unit,
    onInput: (String) -> Unit,
    onSend: () -> Unit,
) {
    Surface(color = NexoraColors.Amoled.copy(alpha = 0.98f), shadowElevation = 16.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AnimatedVisibility(showEmojiPicker) {
                Surface(shape = RoundedCornerShape(26.dp), color = NexoraColors.GlassHigh, border = BorderStroke(1.dp, NexoraColors.Stroke)) {
                    FlowRow(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("😀", "😂", "🥹", "😍", "😎", "😭", "😡", "👍", "🙏", "🔥", "✨", "💯", "❤️", "💙", "👀", "🎮", "🎧", "🚀", "🌙", "⭐", "🍜", "☕", "📎", "🔒").forEach { emoji ->
                            Surface(shape = CircleShape, color = NexoraColors.Glass, modifier = Modifier.size(42.dp).clickable { onEmoji(emoji) }) {
                                Box(contentAlignment = Alignment.Center) { Text(emoji, fontSize = 22.sp) }
                            }
                        }
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onToggleEmoji) { Icon(Icons.Filled.InsertEmoticon, contentDescription = "Emojis", tint = NexoraColors.Primary) }
                IconButton(onClick = {}) { Icon(Icons.Filled.AttachFile, contentDescription = "Adjuntar", tint = NexoraColors.Primary) }
                OutlinedTextField(
                    value = input,
                    onValueChange = onInput,
                    placeholder = { Text("Mensaje") },
                    modifier = Modifier.weight(1f),
                    minLines = 1,
                    maxLines = 4,
                    shape = RoundedCornerShape(28.dp),
                )
                Surface(shape = CircleShape, color = if (input.isBlank()) NexoraColors.GlassHigh else NexoraColors.Primary) {
                    IconButton(enabled = !loading && input.isNotBlank(), onClick = onSend) {
                        Icon(Icons.Filled.Send, contentDescription = "Enviar", tint = if (input.isBlank()) NexoraColors.TextMuted else NexoraColors.Amoled)
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: MessageEntity, currentUid: String, preview: String) {
    val mine = message.senderId == currentUid
    val alignment = if (mine) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (mine) NexoraColors.BubbleMine else NexoraColors.BubbleOther
    val corner = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp, bottomStart = if (mine) 22.dp else 7.dp, bottomEnd = if (mine) 7.dp else 22.dp)

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(shape = corner, color = bubbleColor, border = BorderStroke(1.dp, NexoraColors.Stroke), modifier = Modifier.fillMaxWidth(0.80f)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(if (mine) "Tú" else readableTitle(message.senderId), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NexoraColors.MintSoft)
                Text(if (message.kind.equals("MEDIA", true)) "Archivo cifrado · $preview" else preview, color = NexoraColors.TextMain)
                Text(formatTime(message.timestampMs), color = NexoraColors.TextMuted, fontSize = 11.sp)
            }
        }
    }
}

private fun readableTitle(value: String): String = when {
    value.contains("akira", true) -> "Akira Preview"
    value.contains("kenji", true) -> "Kenji QA"
    value.contains("miko", true) -> "Miko Dev"
    value.contains("group", true) -> "Nexora Testers"
    else -> value.replace('_', ' ').replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

private fun formatTime(epochMs: Long): String = if (epochMs <= 0) "Sin hora" else SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMs))
