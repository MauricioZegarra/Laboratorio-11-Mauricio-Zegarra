package com.example.offlinefirstcatalog.di

import android.content.Context
import com.example.offlinefirstcatalog.data.ProductRepository
import com.example.offlinefirstcatalog.data.local.AppDatabase
import com.example.offlinefirstcatalog.data.remote.FakeProductApi
import com.example.offlinefirstcatalog.util.NetworkMonitor

object AppContainer {
    @Volatile private var repository: ProductRepository? = null

    fun getRepository(context: Context): ProductRepository {
        val existing = repository
        if (existing != null) return existing

        synchronized(this) {
            val existingAfterLock = repository
            if (existingAfterLock != null) return existingAfterLock

            val db = AppDatabase.getInstance(context)
            val newRepository = ProductRepository(
                dao = db.productDao(),
                tokenDao = db.deviceTokenDao(),
                api = FakeProductApi(),
                networkMonitor = NetworkMonitor(context)
            )
            repository = newRepository
            return newRepository
        }
    }
}