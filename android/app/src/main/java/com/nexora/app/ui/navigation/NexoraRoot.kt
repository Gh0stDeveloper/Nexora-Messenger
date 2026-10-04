package com.nexora.app.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexora.app.NexoraApplication
import com.nexora.app.ui.screens.AuthScreen
import com.nexora.app.ui.screens.ChatDetailScreen
import com.nexora.app.ui.screens.ChatsScreen
import com.nexora.app.ui.screens.ProfileSetupScreen
import com.nexora.app.ui.viewmodel.AuthViewModel
import com.nexora.app.ui.viewmodel.AuthViewModelFactory
import com.nexora.app.ui.viewmodel.ChatDetailViewModel
import com.nexora.app.ui.viewmodel.ChatDetailViewModelFactory
import com.nexora.app.ui.viewmodel.ChatsViewModel
import com.nexora.app.ui.viewmodel.ChatsViewModelFactory
import com.nexora.app.ui.viewmodel.ProfileViewModel
import com.nexora.app.ui.viewmodel.ProfileViewModelFactory

private data class SelectedChat(
    val chatId: String,
    val recipientId: String,
)

@Composable
fun NexoraRoot() {
    val app = LocalContext.current.applicationContext as NexoraApplication
    val container = app.container
    var selectedChat by remember { mutableStateOf<SelectedChat?>(null) }
    var showOwnProfile by remember { mutableStateOf(false) }

    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModelFactory(container.authRepository),
    )
    val authState by authViewModel.state.collectAsStateWithLifecycle()

    if (!authState.authenticated) {
        AuthScreen(viewModel = authViewModel)
        return
    }

    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(container.profileRepository),
    )
    val profileState by profileViewModel.state.collectAsStateWithLifecycle()

    if (!profileState.completed) {
        ProfileSetupScreen(viewModel = profileViewModel)
        return
    }

    val activeChat = selectedChat
    if (activeChat != null) {
        BackHandler { selectedChat = null }
        val chatDetailViewModel: ChatDetailViewModel = viewModel(
            key = "chat-${activeChat.chatId}",
            factory = ChatDetailViewModelFactory(
                repository = container.chatRepository,
                chatId = activeChat.chatId,
                recipientId = activeChat.recipientId,
            ),
        )
        ChatDetailScreen(
            viewModel = chatDetailViewModel,
            onBack = { selectedChat = null },
        )
        return
    }

    val chatsViewModel: ChatsViewModel = viewModel(
        factory = ChatsViewModelFactory(container.chatRepository),
    )
    ChatsScreen(
        viewModel = chatsViewModel,
        profileName = profileState.profile?.name.orEmpty().ifBlank { "Ghost Developer" },
        profilePhone = profileState.profile?.phone.orEmpty(),
        showOwnProfile = showOwnProfile,
        onToggleProfile = { showOwnProfile = !showOwnProfile },
        onCloseProfile = { showOwnProfile = false },
        onOpenChat = { chatId, recipientId -> selectedChat = SelectedChat(chatId, recipientId) },
    )
}
