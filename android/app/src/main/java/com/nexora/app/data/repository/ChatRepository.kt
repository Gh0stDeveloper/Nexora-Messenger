package com.nexora.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.nexora.app.data.crypto.AesGcmMessageCrypto
import com.nexora.app.data.local.ChatEntity
import com.nexora.app.data.local.ContactEntity
import com.nexora.app.data.local.GroupEntity
import com.nexora.app.data.local.MessageEntity
import com.nexora.app.data.local.NexoraDao
import com.nexora.app.data.local.StatusEntity
import com.nexora.app.data.preview.PreviewSession
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
        get() = PreviewSession.uid ?: auth.currentUser?.uid

    fun observeChats(): Flow<List<ChatEntity>> = dao.observeChats()

    fun observeMessages(chatId: String): Flow<List<MessageEntity>> = dao.observeMessages(chatId)

    fun observeContacts(): Flow<List<ContactEntity>> = dao.observeContacts()

    fun observeGroups(): Flow<List<GroupEntity>> = dao.observeGroups()

    fun observeStatuses(): Flow<List<StatusEntity>> = dao.observeActiveStatuses()

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
        if (PreviewSession.active) {
            seedPreviewData()
            return
        }

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
        if (PreviewSession.active) {
            seedPreviewData()
            return
        }

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
        val uid = requireNotNull(currentUserId) { "User must be signed in" }
        val cleanRecipient = recipientId.trim()
        require(cleanRecipient.isNotBlank()) { "Recipient is required" }
        require(plainText.isNotBlank()) { "Message is required" }

        val encrypted = messageCrypto.encrypt(plainText.trim())
        val messageId = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val chatId: String

        if (PreviewSession.active) {
            chatId = chatIdFor(cleanRecipient)
        } else {
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
            chatId = response.chatId
        }

        dao.upsertMessages(
            listOf(
                MessageEntity(
                    chatId = chatId,
                    messageId = messageId,
                    senderId = uid,
                    recipientId = cleanRecipient,
                    kind = "TEXT",
                    encryptedPayload = encrypted.encryptedPayload,
                    iv = encrypted.iv,
                    timestampMs = timestamp,
                    delivered = PreviewSession.active,
                    read = false,
                ),
            ),
        )
        dao.upsertChats(
            listOf(
                ChatEntity(
                    chatId = chatId,
                    participantIds = listOf(uid, cleanRecipient).joinToString(","),
                    lastMessagePreview = "Mensaje cifrado",
                    lastMessageEpochMs = timestamp,
                    lastMessageSenderId = uid,
                ),
            ),
        )
        if (!PreviewSession.active) {
            syncMessages(chatId)
            syncChats()
        }
        return chatId
    }

    fun previewFor(message: MessageEntity): String {
        return runCatching { messageCrypto.decrypt(message.encryptedPayload, message.iv) }
            .getOrElse { "Mensaje cifrado" }
    }

    fun previewStatus(status: StatusEntity): String {
        return runCatching { messageCrypto.decrypt(status.encryptedPayload, status.iv) }
            .getOrElse { "Estado cifrado" }
    }

    private suspend fun seedPreviewData() {
        val uid = PreviewSession.PreviewUid
        val friend = PreviewSession.FriendUid
        val now = System.currentTimeMillis()
        val chatId = chatIdFor(friend)
        val hello = messageCrypto.encrypt("Hola Ghost, este es un chat local de prueba.")
        val reply = messageCrypto.encrypt("Puedes revisar UI, burbujas, cifrado local y navegación sin VPS activo.")
        val status = messageCrypto.encrypt("Nexora preview status")

        dao.upsertContacts(
            listOf(
                ContactEntity(
                    uid = friend,
                    phone = "+526681112233",
                    name = "Akira Preview",
                    status = "Probando Nexora",
                    isFavorite = true,
                    updatedAt = now,
                ),
            ),
        )
        dao.upsertGroups(
            listOf(
                GroupEntity(
                    groupId = PreviewSession.GroupId,
                    title = "Nexora Testers",
                    memberIds = listOf(uid, friend).joinToString(","),
                    ownerId = uid,
                    createdAt = now - 7_200_000L,
                ),
            ),
        )
        dao.upsertStatuses(
            listOf(
                StatusEntity(
                    statusId = "preview_status_1",
                    ownerId = friend,
                    ownerName = "Akira Preview",
                    kind = "TEXT",
                    encryptedPayload = status.encryptedPayload,
                    iv = status.iv,
                    createdAt = now - 1_800_000L,
                    expiresAt = now + 86_400_000L,
                ),
            ),
        )
        dao.upsertChats(
            listOf(
                ChatEntity(
                    chatId = chatId,
                    participantIds = listOf(uid, friend).joinToString(","),
                    lastMessagePreview = "Mensaje cifrado",
                    lastMessageEpochMs = now - 300_000L,
                    lastMessageSenderId = friend,
                    title = "Akira Preview",
                ),
                ChatEntity(
                    chatId = PreviewSession.GroupId,
                    participantIds = listOf(uid, friend).joinToString(","),
                    lastMessagePreview = "Grupo de prueba",
                    lastMessageEpochMs = now - 900_000L,
                    lastMessageSenderId = uid,
                    isGroup = true,
                    title = "Nexora Testers",
                ),
            ),
        )
        dao.upsertMessages(
            listOf(
                MessageEntity(
                    chatId = chatId,
                    messageId = "preview_msg_1",
                    senderId = friend,
                    recipientId = uid,
                    kind = "TEXT",
                    encryptedPayload = hello.encryptedPayload,
                    iv = hello.iv,
                    timestampMs = now - 600_000L,
                    delivered = true,
                    read = true,
                ),
                MessageEntity(
                    chatId = chatId,
                    messageId = "preview_msg_2",
                    senderId = uid,
                    recipientId = friend,
                    kind = "TEXT",
                    encryptedPayload = reply.encryptedPayload,
                    iv = reply.iv,
                    timestampMs = now - 300_000L,
                    delivered = true,
                    read = false,
                ),
            ),
        )
    }
}
