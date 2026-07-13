package com.example.offlinefirstcatalog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products WHERE isDeletedLocally = 0 ORDER BY name ASC")
    fun getProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Int): ProductEntity?

    @Query("SELECT * FROM products WHERE isPendingSync = 1")
    suspend fun getPendingProducts(): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOne(product: ProductEntity)

    @Query("UPDATE products SET isPendingSync = 0 WHERE id = :id")
    suspend fun markAsSynced(id: Int)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM products WHERE isPendingSync = 1")
    fun getPendingCount(): Flow<Int>
}