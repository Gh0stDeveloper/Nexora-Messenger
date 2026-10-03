package com.nexora.app.data.repository

import com.google.firebase.auth.FirebaseAuth
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
) {
    fun observeChats(): Flow<List<ChatEntity>> = dao.observeChats()

    fun observeMessages(chatId: String): Flow<List<MessageEntity>> = dao.observeMessages(chatId)

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

    suspend fun sendEncryptedText(recipientId: String, encryptedPayload: String, iv: String) {
        val uid = requireNotNull(auth.currentUser?.uid) { "User must be signed in" }
        val response = relayApi.sendMessage(
            SendMessageRequest(
                senderId = uid,
                recipientId = recipientId.trim(),
                encryptedPayload = encryptedPayload,
                iv = iv,
                messageId = UUID.randomUUID().toString(),
                timestamp = System.currentTimeMillis(),
            ),
        )
        syncMessages(response.chatId)
        syncChats()
    }
}
