package com.nexora.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_profiles")
data class LocalProfileEntity(
    @PrimaryKey val uid: String,
    val phone: String = "",
    val name: String = "",
    val photoUrl: String? = null,
    val publicKey: String? = null,
    val profileCompleted: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val uid: String,
    val phone: String,
    val name: String,
    val photoUrl: String? = null,
    val status: String = "Disponible",
    val isFavorite: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val groupId: String,
    val title: String,
    val memberIds: String,
    val ownerId: String,
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "statuses")
data class StatusEntity(
    @PrimaryKey val statusId: String,
    val ownerId: String,
    val ownerName: String,
    val kind: String,
    val encryptedPayload: String,
    val iv: String,
    val mediaUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 86_400_000L,
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val chatId: String,
    val participantIds: String,
    val lastMessagePreview: String,
    val lastMessageEpochMs: Long,
    val lastMessageSenderId: String,
    val isGroup: Boolean = false,
    val title: String? = null,
    val photoUrl: String? = null,
)

@Entity(tableName = "messages", primaryKeys = ["chatId", "messageId"])
data class MessageEntity(
    val chatId: String,
    val messageId: String,
    val senderId: String,
    val recipientId: String,
    val kind: String,
    val encryptedPayload: String,
    val iv: String,
    val timestampMs: Long,
    val delivered: Boolean,
    val read: Boolean,
    val mediaUrl: String? = null,
    val mediaMime: String? = null,
    val mediaName: String? = null,
    val protocolVersion: String = "nexora-ratchet-v1",
)
