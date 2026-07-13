package com.example.offlinefirstcatalog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_token")
data class DeviceTokenEntity(
    @PrimaryKey val id: Int = 1,
    val token: String,
    val updatedAt: Long
)