package com.nexora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.app.data.local.ChatEntity
import com.nexora.app.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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

    fun newChatTarget(): Pair<String, String>? {
        val recipientId = _state.value.recipientId.trim()
        if (recipientId.isBlank()) {
            _state.update { it.copy(error = "Escribe el UID destino") }
            return null
        }
        return repository.chatIdFor(recipientId) to recipientId
    }

    fun openChatTarget(chat: ChatEntity): Pair<String, String> {
        return chat.chatId to repository.otherParticipant(chat)
    }
}
