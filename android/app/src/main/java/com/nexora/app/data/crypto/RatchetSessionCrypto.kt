package com.nexora.app.data.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Conversation-scoped ratchet foundation.
 *
 * This gives every chat its own local AES-GCM key alias and protocol marker. It is ready for
 * future X3DH/Double-Ratchet key exchange, but it deliberately avoids claiming audited Signal
 * compatibility until a formal interoperable implementation is added.
 */
class RatchetSessionCrypto {
    data class CipherText(
        val encryptedPayload: String,
        val iv: String,
        val protocolVersion: String = ProtocolVersion,
    )

    fun encrypt(chatId: String, plainText: String): CipherText {
        val cipher = Cipher.getInstance(Transformation)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey(chatId))
        val ciphertext = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return CipherText(
            encryptedPayload = Base64.encodeToString(ciphertext, Base64.NO_WRAP),
            iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP),
        )
    }

    fun decrypt(chatId: String, encryptedPayload: String, iv: String): String {
        val cipher = Cipher.getInstance(Transformation)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(chatId),
            GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)),
        )
        val plain = cipher.doFinal(Base64.decode(encryptedPayload, Base64.NO_WRAP))
        return plain.toString(Charsets.UTF_8)
    }

    private fun getOrCreateKey(chatId: String): SecretKey {
        val alias = "nexora_chat_${chatId.hashCode().toUInt().toString(16)}"
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(alias, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setRandomizedEncryptionRequired(true)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    companion object {
        const val ProtocolVersion = "nexora-ratchet-v1"
        private const val Transformation = "AES/GCM/NoPadding"
    }
}
