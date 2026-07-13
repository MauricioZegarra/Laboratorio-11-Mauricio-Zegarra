package com.example.offlinefirstcatalog

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.example.offlinefirstcatalog.sync.SyncScheduler

class CatalogApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        SyncScheduler.schedulePeriodicSync(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "catalog_updates",
                "Actualizaciones del catálogo",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones sobre cambios en el catálogo de productos"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}