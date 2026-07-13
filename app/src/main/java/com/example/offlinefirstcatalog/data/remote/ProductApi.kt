package com.example.offlinefirstcatalog.data.remote

interface ProductApi {
    suspend fun getProducts(): List<ProductDto>
    suspend fun updateProduct(id: Int, product: ProductDto): ProductDto
    suspend fun createProduct(product: ProductDto): ProductDto
    suspend fun deleteProduct(id: Int)
    suspend fun registerToken(token: String)
}