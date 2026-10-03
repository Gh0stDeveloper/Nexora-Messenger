package com.nexora.app.data.messaging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nexora.app.BuildConfig
import com.nexora.app.MainActivity
import com.nexora.app.R
import com.nexora.app.data.remote.FcmTokenRequest
import com.nexora.app.data.remote.RelayClientFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NexoraMessagingService : FirebaseMessagingService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        scope.launch {
            runCatching {
                val auth = FirebaseAuth.getInstance()
                if (auth.currentUser == null) return@launch
                val api = RelayClientFactory.create(BuildConfig.NEXORA_RELAY_BASE_URL, auth)
                api.syncFcmToken(FcmTokenRequest(token))
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        showEncryptedMessageNotification(
            chatId = message.data["chatId"].orEmpty(),
            title = message.notification?.title ?: "Nexora",
            body = message.notification?.body ?: "Tienes un mensaje cifrado nuevo",
        )
    }

    private fun showEncryptedMessageNotification(chatId: String, title: String, body: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_MESSAGES,
                "Mensajes cifrados",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Notificaciones privadas de Nexora Messenger"
            }
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("chatId", chatId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            chatId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_MESSAGES)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify((chatId.ifBlank { System.currentTimeMillis().toString() }).hashCode(), notification)
    }

    companion object {
        private const val CHANNEL_MESSAGES = "nexora_messages"
    }
}
