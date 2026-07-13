package com.example.offlinefirstcatalog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.offlinefirstcatalog.data.ProductRepository

class ProductViewModelFactory(
    private val repository: ProductRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ProductViewModel(repository) as T
    }
}