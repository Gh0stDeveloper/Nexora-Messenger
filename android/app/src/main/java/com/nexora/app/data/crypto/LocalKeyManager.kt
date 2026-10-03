package com.nexora.app.data.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.spec.ECGenParameterSpec

class LocalKeyManager(context: Context) {
    private val prefs = context.getSharedPreferences("nexora_crypto", Context.MODE_PRIVATE)

    fun getOrCreatePublicKey(): String {
        prefs.getString(PREF_PUBLIC_KEY, null)?.let { return it }

        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore",
        )
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY,
        )
            .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
            .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
            .setUserAuthenticationRequired(false)
            .build()
        keyPairGenerator.initialize(spec)
        val publicKey = keyPairGenerator.generateKeyPair().public.encoded
        return Base64.encodeToString(publicKey, Base64.NO_WRAP).also {
            prefs.edit().putString(PREF_PUBLIC_KEY, it).apply()
        }
    }

    fun hasPrivateKey(): Boolean {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        return keyStore.containsAlias(KEY_ALIAS)
    }

    companion object {
        private const val KEY_ALIAS = "nexora_identity_key_v1"
        private const val PREF_PUBLIC_KEY = "identity_public_key_base64"
    }
}
