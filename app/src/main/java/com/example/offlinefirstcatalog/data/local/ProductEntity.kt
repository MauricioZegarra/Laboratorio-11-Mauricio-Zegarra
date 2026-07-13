package com.example.offlinefirstcatalog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val price: Double,
    val updatedAt: Long,
    val isPendingSync: Boolean = false,
    val isDeletedLocally: Boolean = false
)