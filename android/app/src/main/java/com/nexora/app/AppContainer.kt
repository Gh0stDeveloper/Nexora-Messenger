package com.nexora.app

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.nexora.app.data.crypto.LocalKeyManager
import com.nexora.app.data.local.NexoraDatabase
import com.nexora.app.data.remote.RelayClientFactory
import com.nexora.app.data.repository.AuthRepository
import com.nexora.app.data.repository.ChatRepository
import com.nexora.app.data.repository.ProfileRepository

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val auth = FirebaseAuth.getInstance()
    private val messaging = FirebaseMessaging.getInstance()
    private val database = NexoraDatabase.get(appContext)
    private val relayApi = RelayClientFactory.create(BuildConfig.NEXORA_RELAY_BASE_URL, auth)
    private val keyManager = LocalKeyManager(appContext)

    val authRepository = AuthRepository(auth)
    val profileRepository = ProfileRepository(
        context = appContext,
        auth = auth,
        messaging = messaging,
        relayApi = relayApi,
        dao = database.dao(),
        keyManager = keyManager,
    )
    val chatRepository = ChatRepository(
        auth = auth,
        relayApi = relayApi,
        dao = database.dao(),
    )
}
