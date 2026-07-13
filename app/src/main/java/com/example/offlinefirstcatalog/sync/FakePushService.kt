package com.example.offlinefirstcatalog.sync

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.offlinefirstcatalog.MainActivity
import com.example.offlinefirstcatalog.R
import com.example.offlinefirstcatalog.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

object FakePushService {

    fun generateAndRegisterToken(context: Context) {
        val repository = AppContainer.getRepository(context)
        val fakeToken = "fake-fcm-token-${UUID.randomUUID().toString().take(12)}"
        CoroutineScope(Dispatchers.IO).launch {
            repository.registerDeviceToken(fakeToken)
        }
    }

    fun simulateIncomingPush(
        context: Context,
        title: String,
        body: String,
        productId: Int? = null,
        triggerSync: Boolean = true
    ) {
        val repository = AppContainer.getRepository(context)

        if (triggerSync) {
            CoroutineScope(Dispatchers.IO).launch {
                repository.fullSync()
            }
        }

        showLocalNotification(context, title, body, productId)
    }

    private fun showLocalNotification(context: Context, title: String, body: String, productId: Int?) {
        val channelId = "catalog_updates"

        val intent = Intent(context, MainActivity::class.java).apply {
            productId?.let { putExtra("productId", it) }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(productId ?: 0, builder.build())
    }
}