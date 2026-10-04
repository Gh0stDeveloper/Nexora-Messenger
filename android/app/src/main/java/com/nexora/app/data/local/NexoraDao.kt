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
    suspend fun upsertContacts(contacts: List<ContactEntity>)

    @Query("SELECT * FROM contacts ORDER BY isFavorite DESC, name COLLATE NOCASE ASC")
    fun observeContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE uid = :uid LIMIT 1")
    suspend fun getContact(uid: String): ContactEntity?

    @Upsert
    suspend fun upsertGroups(groups: List<GroupEntity>)

    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    fun observeGroups(): Flow<List<GroupEntity>>

    @Upsert
    suspend fun upsertStatuses(statuses: List<StatusEntity>)

    @Query("SELECT * FROM statuses WHERE expiresAt > :now ORDER BY createdAt DESC")
    fun observeActiveStatuses(now: Long = System.currentTimeMillis()): Flow<List<StatusEntity>>

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
