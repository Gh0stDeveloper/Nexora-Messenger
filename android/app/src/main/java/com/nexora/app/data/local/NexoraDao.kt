package com.nexora.app.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface NexoraDao {
    @Upsert
    suspend fun upsertProfile(profile: LocalProfileEntity)

    @Query("SELECT * FROM local_profiles WHERE uid = :uid LIMIT 1")
    fun observeProfile(uid: String): Flow<LocalProfileEntity?>

    @Query("SELECT * FROM local_profiles WHERE uid = :uid LIMIT 1")
    suspend fun getProfile(uid: String): LocalProfileEntity?

    @Upsert
    suspend fun upsertChats(chats: List<ChatEntity>)

    @Query("SELECT * FROM chats ORDER BY lastMessageEpochMs DESC")
    fun observeChats(): Flow<List<ChatEntity>>

    @Upsert
    suspend fun upsertMessages(messages: List<MessageEntity>)

    @Query("SELECT * FROM messages WHERE chatId = :chatId ORDER BY timestampMs ASC")
    fun observeMessages(chatId: String): Flow<List<MessageEntity>>

    @Query("SELECT COALESCE(MAX(timestampMs), 0) FROM messages WHERE chatId = :chatId")
    suspend fun latestMessageTimestamp(chatId: String): Long
}
