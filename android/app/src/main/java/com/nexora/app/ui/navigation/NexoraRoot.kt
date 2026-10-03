package com.nexora.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexora.app.NexoraApplication
import com.nexora.app.ui.screens.AuthScreen
import com.nexora.app.ui.screens.ChatsScreen
import com.nexora.app.ui.screens.ProfileSetupScreen
import com.nexora.app.ui.viewmodel.AuthViewModel
import com.nexora.app.ui.viewmodel.AuthViewModelFactory
import com.nexora.app.ui.viewmodel.ChatsViewModel
import com.nexora.app.ui.viewmodel.ChatsViewModelFactory
import com.nexora.app.ui.viewmodel.ProfileViewModel
import com.nexora.app.ui.viewmodel.ProfileViewModelFactory

@Composable
fun NexoraRoot() {
    val app = LocalContext.current.applicationContext as NexoraApplication
    val container = app.container

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

    val chatsViewModel: ChatsViewModel = viewModel(
        factory = ChatsViewModelFactory(container.chatRepository),
    )
    ChatsScreen(viewModel = chatsViewModel)
}
