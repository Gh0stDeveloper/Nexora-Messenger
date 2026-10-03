package com.nexora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.app.data.local.MessageEntity
import com.nexora.app.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatDetailUiState(
    val chatId: String,
    val recipientId: String,
    val messages: List<MessageEntity> = emptyList(),
    val input: String = "",
    val loading: Boolean = false,
    val error: String? = null,
)

class ChatDetailViewModel(
    private val repository: ChatRepository,
    chatId: String,
    recipientId: String,
) : ViewModel() {
    private val _state = MutableStateFlow(
        ChatDetailUiState(
            chatId = chatId,
            recipientId = recipientId,
        ),
    )
    val state: StateFlow<ChatDetailUiState> = _state.asStateFlow()

    val currentUserId: String?
        get() = repository.currentUserId

    init {
        viewModelScope.launch {
            repository.observeMessages(chatId).collectLatest { messages ->
                _state.update { it.copy(messages = messages) }
            }
        }
        refresh()
    }

    fun updateInput(value: String) = _state.update { it.copy(input = value, error = null) }

    fun refresh() {
        val chatId = _state.value.chatId
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repository.syncMessages(chatId) }
                .onSuccess { _state.update { it.copy(loading = false) } }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = error.localizedMessage ?: "No se pudieron sincronizar los mensajes",
                        )
                    }
                }
        }
    }

    fun send() {
        val snapshot = _state.value
        if (snapshot.input.isBlank()) {
            _state.update { it.copy(error = "Escribe un mensaje") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching {
                repository.sendTextMessage(
                    recipientId = snapshot.recipientId,
                    plainText = snapshot.input,
                )
            }
                .onSuccess {
                    _state.update { it.copy(loading = false, input = "") }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            loading = false,
                            error = error.localizedMessage ?: "No se pudo enviar el mensaje",
                        )
                    }
                }
        }
    }

    fun preview(message: MessageEntity): String = repository.previewFor(message)
}
