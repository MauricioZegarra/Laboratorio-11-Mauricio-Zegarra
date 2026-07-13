package com.example.offlinefirstcatalog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceTokenDao {

    @Query("SELECT * FROM device_token WHERE id = 1")
    fun getToken(): Flow<DeviceTokenEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveToken(token: DeviceTokenEntity)
}