package com.nexora.app.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexora.app.data.repository.AuthRepository
import com.nexora.app.util.PhoneNumberNormalizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val phone: String = "",
    val code: String = "",
    val normalizedPhone: String = "",
    val verificationId: String? = null,
    val loading: Boolean = false,
    val authenticated: Boolean = false,
    val previewMode: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(
    private val repository: AuthRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(
        AuthUiState(authenticated = repository.currentUid != null, previewMode = repository.isPreview),
    )
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun updatePhone(value: String) {
        val normalized = PhoneNumberNormalizer.normalizeMexico(value)
        _state.update { it.copy(phone = value, normalizedPhone = normalized, error = null) }
    }

    fun updateCode(value: String) = _state.update { it.copy(code = value.filter { char -> char.isDigit() }.take(6), error = null) }

    fun requestOtp(activity: Activity) {
        val normalized = PhoneNumberNormalizer.normalizeMexico(_state.value.phone)
        if (!PhoneNumberNormalizer.isValidMexico(normalized)) {
            _state.update { it.copy(error = "Escribe tu número de México: +52 y 10 dígitos. El 1 es opcional.") }
            return
        }
        _state.update { it.copy(loading = true, error = null, normalizedPhone = normalized) }
        repository.requestOtp(
            activity = activity,
            phone = normalized,
            onCodeSent = { verificationId ->
                _state.update { it.copy(loading = false, verificationId = verificationId, previewMode = repository.isPreview) }
            },
            onAutoVerified = {
                _state.update { it.copy(loading = false, authenticated = true, verificationId = null, previewMode = repository.isPreview) }
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
                .onSuccess { _state.update { it.copy(loading = false, authenticated = true, previewMode = repository.isPreview) } }
                .onFailure { error -> _state.update { it.copy(loading = false, error = error.localizedMessage ?: "Código inválido") } }
        }
    }
}
