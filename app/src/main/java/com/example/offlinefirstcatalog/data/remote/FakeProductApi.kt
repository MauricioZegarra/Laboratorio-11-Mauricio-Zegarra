package com.example.offlinefirstcatalog.data.remote

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

class FakeProductApi(
    private val simulatedDelayMs: Long = 800L,
    private val failureRate: Float = 0.15f
) : ProductApi {

    private val mutex = Mutex()
    private val serverStorage = mutableMapOf(
        1 to ProductDto(1, "Teclado mecánico", 45.90),
        2 to ProductDto(2, "Mouse inalámbrico", 22.50),
        3 to ProductDto(3, "Monitor 24\"", 189.99),
        4 to ProductDto(4, "Audífonos Bluetooth", 39.90),
        5 to ProductDto(5, "Webcam HD", 28.00)
    )

    private val registeredTokens = mutableSetOf<String>()

    private fun maybeFail() {
        if (Random.nextFloat() < failureRate) {
            throw RuntimeException("Fallo de red simulado")
        }
    }

    override suspend fun getProducts(): List<ProductDto> = mutex.withLock {
        delay(simulatedDelayMs)
        maybeFail()
        serverStorage.values.toList()
    }

    override suspend fun updateProduct(id: Int, product: ProductDto): ProductDto = mutex.withLock {
        delay(simulatedDelayMs)
        maybeFail()
        serverStorage[id] = product
        product
    }

    override suspend fun createProduct(product: ProductDto): ProductDto = mutex.withLock {
        delay(simulatedDelayMs)
        maybeFail()
        val newId = (serverStorage.keys.maxOrNull() ?: 0) + 1
        val created = product.copy(id = newId)
        serverStorage[newId] = created
        created
    }

    override suspend fun deleteProduct(id: Int): Unit = mutex.withLock {
        delay(simulatedDelayMs)
        maybeFail()
        serverStorage.remove(id)
        Unit
    }

    override suspend fun registerToken(token: String): Unit = mutex.withLock {
        delay(simulatedDelayMs)
        maybeFail()
        registeredTokens.add(token)
    }
}