package com.luminor.tavernquest.data.remote.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.luminor.tavernquest.R

class TavernQuestMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        runCatching {
            val user = FirebaseAuth.getInstance().currentUser ?: return
            FirebaseFirestore.getInstance().collection("users").document(user.uid)
                .set(mapOf("fcmTokens" to FieldValue.arrayUnion(token)), SetOptions.merge())
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: "TavernQuest"
        val body = message.notification?.body ?: "Sua aventura tem novidades."
        val manager = getSystemService(NotificationManager::class.java)
        val channelId = "tavernquest_updates"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) manager.createNotificationChannel(NotificationChannel(channelId, "Atualizações da Taberna", NotificationManager.IMPORTANCE_DEFAULT))
        manager.notify(message.data["checkInId"]?.hashCode() ?: System.currentTimeMillis().toInt(), NotificationCompat.Builder(this, channelId).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title).setContentText(body).setAutoCancel(true).build())
    }
}
