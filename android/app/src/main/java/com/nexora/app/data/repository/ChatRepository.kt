package com.nexora.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.nexora.app.data.crypto.AesGcmMessageCrypto
import com.nexora.app.data.local.ChatEntity
import com.nexora.app.data.local.MessageEntity
import com.nexora.app.data.local.NexoraDao
import com.nexora.app.data.remote.RelayApi
import com.nexora.app.data.remote.SendMessageRequest
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ChatRepository(
    private val auth: FirebaseAuth,
    private val relayApi: RelayApi,
    private val dao: NexoraDao,
    private val messageCrypto: AesGcmMessageCrypto,
) {
    val currentUserId: String?
        get() = auth.currentUser?.uid

    fun observeChats(): Flow<List<ChatEntity>> = dao.observeChats()

    fun observeMessages(chatId: String): Flow<List<MessageEntity>> = dao.observeMessages(chatId)

    fun chatIdFor(recipientId: String): String {
        val uid = requireNotNull(currentUserId) { "User must be signed in" }
        return listOf(uid, recipientId.trim()).sorted().joinToString("_")
    }

    fun otherParticipant(chat: ChatEntity): String {
        val uid = currentUserId.orEmpty()
        return chat.participantIds.split(',')
            .map { it.trim() }
            .firstOrNull { it.isNotBlank() && it != uid }
            ?: chat.participantIds
    }

    suspend fun syncChats() {
        val response = relayApi.getChats()
        dao.upsertChats(
            response.chats.map { dto ->
                ChatEntity(
                    chatId = dto.chatId,
                    participantIds = dto.participants.joinToString(","),
                    lastMessagePreview = dto.lastMessagePreview,
                    lastMessageEpochMs = dto.lastMessageEpochMs,
                    lastMessageSenderId = dto.lastMessageSenderId,
                )
            },
        )
    }

    suspend fun syncMessages(chatId: String) {
        val since = dao.latestMessageTimestamp(chatId)
        val response = relayApi.getMessages(chatId = chatId, since = since)
        dao.upsertMessages(
            response.messages.map { dto ->
                MessageEntity(
                    chatId = dto.chatId,
                    messageId = dto.messageId,
                    senderId = dto.senderId,
                    recipientId = dto.recipientId,
                    kind = dto.kind,
                    encryptedPayload = dto.encryptedPayload,
                    iv = dto.iv,
                    timestampMs = dto.timestampMs,
                    delivered = dto.delivered,
                    read = dto.read,
                )
            },
        )
    }

    suspend fun sendTextMessage(recipientId: String, plainText: String): String {
        val uid = requireNotNull(auth.currentUser?.uid) { "User must be signed in" }
        val cleanRecipient = recipientId.trim()
        require(cleanRecipient.isNotBlank()) { "Recipient is required" }
        require(plainText.isNotBlank()) { "Message is required" }

        val encrypted = messageCrypto.encrypt(plainText.trim())
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val response = relayApi.sendMessage(
            SendMessageRequest(
                senderId = uid,
                recipientId = cleanRecipient,
                encryptedPayload = encrypted.encryptedPayload,
                iv = encrypted.iv,
                messageId = messageId,
                timestamp = timestamp,
            ),
        )

        dao.upsertMessages(
            listOf(
                MessageEntity(
                    chatId = response.chatId,
                    messageId = messageId,
                    senderId = uid,
                    recipientId = cleanRecipient,
                    kind = "TEXT",
                    encryptedPayload = encrypted.encryptedPayload,
                    iv = encrypted.iv,
                    timestampMs = timestamp,
                    delivered = false,
                    read = false,
                ),
            ),
        )
        syncMessages(response.chatId)
        syncChats()
        return response.chatId
    }

    fun previewFor(message: MessageEntity): String {
        return runCatching { messageCrypto.decrypt(message.encryptedPayload, message.iv) }
            .getOrElse { "Mensaje cifrado" }
    }
}
