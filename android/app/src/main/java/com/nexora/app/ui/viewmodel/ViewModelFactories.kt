package com.nexora.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nexora.app.data.repository.AuthRepository
import com.nexora.app.data.repository.ChatRepository
import com.nexora.app.data.repository.ProfileRepository

class AuthViewModelFactory(
    private val repository: AuthRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AuthViewModel(repository) as T
    }
}

class ProfileViewModelFactory(
    private val repository: ProfileRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ProfileViewModel(repository) as T
    }
}

class ChatsViewModelFactory(
    private val repository: ChatRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ChatsViewModel(repository) as T
    }
}

class ChatDetailViewModelFactory(
    private val repository: ChatRepository,
    private val chatId: String,
    private val recipientId: String,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ChatDetailViewModel(repository, chatId, recipientId) as T
    }
}
