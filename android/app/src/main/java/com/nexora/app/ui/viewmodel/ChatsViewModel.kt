package com.nexora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.app.data.local.ChatEntity
import com.nexora.app.data.preview.PreviewSession
import com.nexora.app.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PreviewChatTarget(
    val title: String,
    val subtitle: String,
    val recipientId: String,
)

data class ChatsUiState(
    val chats: List<ChatEntity> = emptyList(),
    val recipientId: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)

class ChatsViewModel(
    private val repository: ChatRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ChatsUiState())
    val state: StateFlow<ChatsUiState> = _state.asStateFlow()

    val currentUserId: String?
        get() = repository.currentUserId

    val previewTargets: List<PreviewChatTarget> = listOf(
        PreviewChatTarget(
            title = "Akira Preview",
            subtitle = "Contacto de prueba",
            recipientId = PreviewSession.FriendUid,
        ),
        PreviewChatTarget(
            title = "Nexora Testers",
            subtitle = "Grupo preview local",
            recipientId = PreviewSession.GroupId,
        ),
    )

    init {
        viewModelScope.launch {
            repository.observeChats().collectLatest { chats ->
                _state.update { it.copy(chats = chats) }
            }
        }
        refresh()
    }

    fun updateRecipientId(value: String) = _state.update { it.copy(recipientId = value.trim(), error = null) }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repository.syncChats() }
                .onSuccess { _state.update { it.copy(loading = false) } }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = error.localizedMessage ?: "No se pudieron sincronizar los chats",
                        )
                    }
                }
        }
    }

    fun targetFor(recipientId: String): Pair<String, String> {
        val cleanRecipient = recipientId.trim()
        return repository.chatIdFor(cleanRecipient) to cleanRecipient
    }

    fun previewChatFor(recipientId: String): Pair<String, String> = targetFor(recipientId)

    fun newChatTarget(): Pair<String, String>? {
        val recipientId = _state.value.recipientId.trim()
        if (recipientId.isBlank()) {
            _state.update { it.copy(error = "Escribe el UID destino o usa un contacto de prueba") }
            return null
        }
        return targetFor(recipientId)
    }

    fun openChatTarget(chat: ChatEntity): Pair<String, String> {
        return chat.chatId to repository.otherParticipant(chat)
    }
}
