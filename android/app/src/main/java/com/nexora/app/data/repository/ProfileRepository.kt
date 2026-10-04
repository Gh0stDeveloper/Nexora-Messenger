package com.nexora.app.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.nexora.app.data.crypto.LocalKeyManager
import com.nexora.app.data.local.LocalProfileEntity
import com.nexora.app.data.local.NexoraDao
import com.nexora.app.data.preview.PreviewSession
import com.nexora.app.data.remote.BootstrapProfileRequest
import com.nexora.app.data.remote.FcmTokenRequest
import com.nexora.app.data.remote.RelayApi
import com.nexora.app.util.await
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ProfileRepository(
    private val context: Context,
    private val auth: FirebaseAuth,
    private val messaging: FirebaseMessaging,
    private val relayApi: RelayApi,
    private val dao: NexoraDao,
    private val keyManager: LocalKeyManager,
) {
    private val currentUid: String
        get() = PreviewSession.uid ?: requireNotNull(auth.currentUser?.uid) { "User must be signed in" }

    private val currentPhone: String
        get() = PreviewSession.phone ?: auth.currentUser?.phoneNumber.orEmpty()

    fun observeCurrentProfile(): Flow<LocalProfileEntity?> = dao.observeProfile(currentUid)

    suspend fun completeProfile(name: String, avatarUri: Uri?): LocalProfileEntity {
        val uid = currentUid
        val phone = currentPhone
        val trimmedName = name.trim()
        require(trimmedName.length >= 2) { "El nombre debe tener mínimo 2 caracteres" }

        val publicKey = keyManager.getOrCreatePublicKey()
        val fcmToken = if (PreviewSession.active) null else runCatching { messaging.token.await() }.getOrNull()
        var photoUrl: String? = null

        if (!PreviewSession.active) {
            runCatching {
                relayApi.bootstrapProfile(
                    BootstrapProfileRequest(
                        name = trimmedName,
                        phone = phone,
                        publicKey = publicKey,
                        fcmToken = fcmToken,
                    ),
                )
            }

            if (avatarUri != null) {
                photoUrl = uploadAvatar(avatarUri)
            }

            if (!fcmToken.isNullOrBlank()) {
                runCatching { relayApi.syncFcmToken(FcmTokenRequest(fcmToken)) }
            }
        } else {
            photoUrl = null
        }

        val entity = LocalProfileEntity(
            uid = uid,
            phone = phone,
            name = trimmedName,
            photoUrl = photoUrl,
            publicKey = publicKey,
            profileCompleted = true,
        )
        dao.upsertProfile(entity)
        return entity
    }

    private suspend fun uploadAvatar(uri: Uri): String {
        val resolver = context.contentResolver
        val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("No se pudo leer la imagen seleccionada")
        require(bytes.size <= MAX_AVATAR_BYTES) { "La imagen debe pesar máximo 2 MB" }
        val mime = resolver.getType(uri) ?: "image/jpeg"
        val requestBody = bytes.toRequestBody(mime.toMediaType())
        val part = MultipartBody.Part.createFormData("avatar", "avatar.jpg", requestBody)
        return relayApi.uploadAvatar(part).photoUrl
    }

    companion object {
        private const val MAX_AVATAR_BYTES = 2 * 1024 * 1024
    }
}
