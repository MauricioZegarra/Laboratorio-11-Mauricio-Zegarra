package com.example.offlinefirstcatalog.data

import com.example.offlinefirstcatalog.data.local.ProductEntity
import com.example.offlinefirstcatalog.data.remote.ProductDto

fun ProductDto.toEntity(pending: Boolean = false) = ProductEntity(
    id = id,
    name = name,
    price = price,
    updatedAt = System.currentTimeMillis(),
    isPendingSync = pending
)

fun ProductEntity.toDto() = ProductDto(
    id = id,
    name = name,
    price = price
)