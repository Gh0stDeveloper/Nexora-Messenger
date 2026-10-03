package com.nexora.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LocalProfileEntity::class, ChatEntity::class, MessageEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class NexoraDatabase : RoomDatabase() {
    abstract fun dao(): NexoraDao

    companion object {
        @Volatile private var instance: NexoraDatabase? = null

        fun get(context: Context): NexoraDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                NexoraDatabase::class.java,
                "nexora_local.db",
            ).build().also { instance = it }
        }
    }
}
