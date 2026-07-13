package com.example.offlinefirstcatalog.data

import com.example.offlinefirstcatalog.data.local.DeviceTokenDao
import com.example.offlinefirstcatalog.data.local.DeviceTokenEntity
import com.example.offlinefirstcatalog.data.local.ProductDao
import com.example.offlinefirstcatalog.data.local.ProductEntity
import com.example.offlinefirstcatalog.data.remote.ProductApi
import com.example.offlinefirstcatalog.util.NetworkMonitor
import kotlinx.coroutines.flow.Flow

sealed class SyncResult {
    object Success : SyncResult()
    object NoConnection : SyncResult()
    data class Error(val message: String) : SyncResult()
}

class ProductRepository(
    private val dao: ProductDao,
    private val tokenDao: DeviceTokenDao,
    private val api: ProductApi,
    private val networkMonitor: NetworkMonitor
) {
    fun getProducts(): Flow<List<ProductEntity>> = dao.getProducts()

    fun getPendingCount(): Flow<Int> = dao.getPendingCount()

    fun getStoredToken(): Flow<DeviceTokenEntity?> = tokenDao.getToken()

    private suspend fun syncFromRemote() {
        val remoteProducts = api.getProducts()
        remoteProducts.forEach { remoteDto ->
            val local = dao.getProductById(remoteDto.id)
            val remoteEntity = remoteDto.toEntity()
            val resolved = if (local == null) remoteEntity else resolveConflict(local, remoteEntity)
            dao.insertOne(resolved)
        }
    }

    private suspend fun pushPendingChanges() {
        val pending = dao.getPendingProducts()
        pending.forEach { entity ->
            try {
                api.updateProduct(entity.id, entity.toDto())
                dao.markAsSynced(entity.id)
            } catch (e: Exception) {
                // se reintenta en el siguiente ciclo
            }
        }
    }

    suspend fun updateProductLocally(id: Int, name: String, price: Double) {
        val existing = dao.getProductById(id)
        val entity = ProductEntity(
            id = id,
            name = name,
            price = price,
            updatedAt = System.currentTimeMillis(),
            isPendingSync = true,
            isDeletedLocally = existing?.isDeletedLocally ?: false
        )
        dao.insertOne(entity)
    }

    private fun resolveConflict(local: ProductEntity, remote: ProductEntity): ProductEntity {
        if (local.isPendingSync) return local
        return if (remote.updatedAt > local.updatedAt) remote else local
    }

    suspend fun fullSync(): SyncResult {
        if (!networkMonitor.isConnected()) return SyncResult.NoConnection
        return try {
            pushPendingChanges()
            syncFromRemote()
            SyncResult.Success
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Error desconocido")
        }
    }

    suspend fun registerDeviceToken(token: String) {
        tokenDao.saveToken(DeviceTokenEntity(token = token, updatedAt = System.currentTimeMillis()))
        if (networkMonitor.isConnected()) {
            try {
                api.registerToken(token)
            } catch (e: Exception) {
                // el token ya quedó guardado localmente
            }
        }
    }
}