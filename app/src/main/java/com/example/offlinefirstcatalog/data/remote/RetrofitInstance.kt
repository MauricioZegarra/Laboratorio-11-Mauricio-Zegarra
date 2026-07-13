package com.example.offlinefirstcatalog.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val api: ProductApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.miservidor.com/") // reemplazar con backend real
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductApi::class.java)
    }
}