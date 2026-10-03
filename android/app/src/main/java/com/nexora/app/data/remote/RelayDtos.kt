package com.nexora.app.data.remote

import com.google.gson.annotations.SerializedName

data class ApiEnvelope(
    val ok: Boolean,
    val error: String? = null,
)

data class BootstrapProfileRequest(
    val name: String,
    val phone: String? = null,
    val photoUrl: String? = null,
    val publicKey: String? = null,
    val fcmToken: String? = null,
)

data class BootstrapProfileResponse(
    val ok: Boolean,
    val uid: String,
)

data class AvatarUploadResponse(
    val ok: Boolean,
    val photoUrl: String,
)

data class FcmTokenRequest(
    val token: String,
)

data class ChatDto(
    @SerializedName("chat_id") val chatId: String,
    val participants: List<String>,
    @SerializedName("last_message_preview") val lastMessagePreview: String,
    @SerializedName("last_message_epoch_ms") val lastMessageEpochMs: Long,
    @SerializedName("last_message_sender_id") val lastMessageSenderId: String,
)

data class ChatsResponse(
    val ok: Boolean,
    val chats: List<ChatDto> = emptyList(),
)

data class MessageDto(
    @SerializedName("chat_id") val chatId: String,
    @SerializedName("message_id") val messageId: String,
    @SerializedName("sender_id") val senderId: String,
    @SerializedName("recipient_id") val recipientId: String,
    val kind: String,
    @SerializedName("encrypted_payload") val encryptedPayload: String,
    val iv: String,
    @SerializedName("timestamp_ms") val timestampMs: Long,
    val delivered: Boolean,
    val read: Boolean,
)

data class MessagesResponse(
    val ok: Boolean,
    val messages: List<MessageDto> = emptyList(),
)

data class SendMessageRequest(
    val senderId: String,
    val recipientId: String,
    val kind: String = "TEXT",
    val encryptedPayload: String,
    val iv: String,
    val messageId: String,
    val timestamp: Long,
)

data class SendMessageResponse(
    val ok: Boolean,
    val chatId: String,
    val messageId: String,
)
