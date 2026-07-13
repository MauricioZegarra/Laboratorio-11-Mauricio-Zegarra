package com.example.offlinefirstcatalog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offlinefirstcatalog.data.ProductRepository
import com.example.offlinefirstcatalog.data.SyncResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class SyncUiState {
    object Idle : SyncUiState()
    object Syncing : SyncUiState()
    data class Done(val timestamp: Long) : SyncUiState()
    object NoConnection : SyncUiState()
    data class Failed(val message: String) : SyncUiState()
}

class ProductViewModel(
    private val repository: ProductRepository
) : ViewModel() {

    val products = repository.getProducts()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    val pendingCount = repository.getPendingCount()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            0
        )

    private val _syncState = MutableStateFlow<SyncUiState>(SyncUiState.Idle)
    val syncState: StateFlow<SyncUiState> = _syncState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun refresh() {
        viewModelScope.launch {
            _syncState.value = SyncUiState.Syncing
            when (val result = repository.fullSync()) {
                is SyncResult.Success -> _syncState.value = SyncUiState.Done(System.currentTimeMillis())
                is SyncResult.NoConnection -> _syncState.value = SyncUiState.NoConnection
                is SyncResult.Error -> _syncState.value = SyncUiState.Failed(result.message)
            }
        }
    }

    fun editProduct(id: Int, name: String, price: Double) {
        viewModelScope.launch {
            repository.updateProductLocally(id, name, price)
        }
    }
}