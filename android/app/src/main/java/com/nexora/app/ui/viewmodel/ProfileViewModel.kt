package com.nexora.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.app.data.local.LocalProfileEntity
import com.nexora.app.data.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "",
    val avatarUri: Uri? = null,
    val loading: Boolean = false,
    val completed: Boolean = false,
    val error: String? = null,
    val profile: LocalProfileEntity? = null,
)

class ProfileViewModel(
    private val repository: ProfileRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching {
                repository.observeCurrentProfile().collect { profile ->
                    _state.update {
                        it.copy(
                            profile = profile,
                            completed = profile?.profileCompleted == true,
                            name = if (it.name.isBlank()) profile?.name.orEmpty() else it.name,
                        )
                    }
                }
            }
        }
    }

    fun updateName(value: String) = _state.update { it.copy(name = value.take(80), error = null) }
    fun updateAvatar(uri: Uri?) = _state.update { it.copy(avatarUri = uri, error = null) }

    fun completeProfile() {
        val snapshot = _state.value
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repository.completeProfile(snapshot.name, snapshot.avatarUri) }
                .onSuccess { profile ->
                    _state.update { it.copy(loading = false, completed = true, profile = profile) }
                }
                .onFailure { error ->
                    _state.update { it.copy(loading = false, error = error.localizedMessage ?: "No se pudo guardar el perfil") }
                }
        }
    }
}
