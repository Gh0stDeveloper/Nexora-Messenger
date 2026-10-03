package com.nexora.app.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val phone: String = "",
    val code: String = "",
    val verificationId: String? = null,
    val loading: Boolean = false,
    val authenticated: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(
    private val repository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(
        AuthUiState(authenticated = repository.currentUid != null),
    )
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun updatePhone(value: String) = _state.update { it.copy(phone = value, error = null) }
    fun updateCode(value: String) = _state.update { it.copy(code = value.filter(Char::isDigit).take(6), error = null) }

    fun requestOtp(activity: Activity) {
        val phone = _state.value.phone.trim()
        if (!phone.startsWith("+") || phone.length < 10) {
            _state.update { it.copy(error = "Usa formato internacional. Ejemplo: +5215512345678") }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        repository.requestOtp(
            activity = activity,
            phone = phone,
            onCodeSent = { verificationId ->
                _state.update { it.copy(loading = false, verificationId = verificationId) }
            },
            onAutoVerified = {
                _state.update { it.copy(loading = false, authenticated = true, verificationId = null) }
            },
            onError = { error ->
                _state.update { it.copy(loading = false, error = error.localizedMessage ?: "No se pudo enviar el código") }
            },
        )
    }

    fun verifyCode() {
        val verificationId = _state.value.verificationId
        val code = _state.value.code
        if (verificationId.isNullOrBlank()) {
            _state.update { it.copy(error = "Primero solicita el código SMS") }
            return
        }
        if (code.length != 6) {
            _state.update { it.copy(error = "El código debe tener 6 dígitos") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            runCatching { repository.verifyCode(verificationId, code) }
                .onSuccess { _state.update { it.copy(loading = false, authenticated = true) } }
                .onFailure { error -> _state.update { it.copy(loading = false, error = error.localizedMessage ?: "Código inválido") } }
        }
    }
}
