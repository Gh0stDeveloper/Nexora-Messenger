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

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val chatId: String,
    val participantIds: String,
    val lastMessagePreview: String,
    val lastMessageEpochMs: Long,
    val lastMessageSenderId: String,
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
)
