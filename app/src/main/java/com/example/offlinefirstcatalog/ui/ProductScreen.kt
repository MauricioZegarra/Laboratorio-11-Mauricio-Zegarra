package com.example.offlinefirstcatalog.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.offlinefirstcatalog.data.local.ProductEntity
import com.example.offlinefirstcatalog.sync.FakePushService
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    viewModel: ProductViewModel,
    highlightProductId: Int? = null
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val pendingCount by viewModel.pendingCount.collectAsState()
    val syncState by viewModel.syncState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var editingProduct by remember { mutableStateOf<ProductEntity?>(null) }
    var highlightedId by remember { mutableStateOf(highlightProductId) }

    val listState = rememberLazyListState()

    val filteredProducts = remember(products, searchQuery) {
        if (searchQuery.isBlank()) products
        else products.filter { product -> product.name.contains(searchQuery, ignoreCase = true) }
    }

    LaunchedEffect(highlightProductId, filteredProducts) {
        if (highlightProductId != null && filteredProducts.isNotEmpty()) {
            val index = filteredProducts.indexOfFirst { product -> product.id == highlightProductId }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
            delay(2500)
            highlightedId = null
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Catálogo Offline-First", fontWeight = FontWeight.SemiBold) },
                    actions = {
                        if (pendingCount > 0) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(end = 16.dp)
                            ) {
                                Text("$pendingCount pendiente${if (pendingCount > 1) "s" else ""}")
                            }
                        }
                    }
                )
                SyncStatusBar(syncState)
            }
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = {
                        FakePushService.simulateIncomingPush(
                            context = context,
                            title = "Oferta especial",
                            body = "El Monitor 24\" cambió de precio",
                            productId = 3
                        )
                    }
                ) {
                    Icon(Icons.Filled.Notifications, contentDescription = "Simular push")
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(onClick = { viewModel.refresh() }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Sincronizar")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                placeholder = { Text("Buscar producto...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (filteredProducts.isEmpty()) {
                EmptyState(hasQuery = searchQuery.isNotBlank())
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredProducts, key = { product -> product.id }) { product ->
                        ProductCard(
                            product = product,
                            isHighlighted = product.id == highlightedId,
                            onEditClick = { editingProduct = product }
                        )
                    }
                }
            }
        }
    }

    val currentEditingProduct = editingProduct
    if (currentEditingProduct != null) {
        EditProductDialog(
            product = currentEditingProduct,
            onDismiss = { editingProduct = null },
            onConfirm = { name, price ->
                viewModel.editProduct(currentEditingProduct.id, name, price)
                editingProduct = null
            }
        )
    }
}

@Composable
private fun SyncStatusBar(state: SyncUiState) {
    val (text, color, icon) = when (state) {
        is SyncUiState.Idle -> Triple(
            "Toca ↻ para sincronizar", MaterialTheme.colorScheme.onSurfaceVariant, null
        )
        is SyncUiState.Syncing -> Triple(
            "Sincronizando...", MaterialTheme.colorScheme.primary, null
        )
        is SyncUiState.Done -> Triple(
            "Última sincronización: ${formatTime(state.timestamp)}",
            Color(0xFF2E7D32),
            Icons.Filled.CheckCircle
        )
        is SyncUiState.NoConnection -> Triple(
            "Sin conexión — mostrando datos locales",
            MaterialTheme.colorScheme.error,
            Icons.Filled.Info
        )
        is SyncUiState.Failed -> Triple(
            "Error al sincronizar: ${state.message}",
            MaterialTheme.colorScheme.error,
            Icons.Filled.Warning
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (state is SyncUiState.Syncing) {
            CircularProgressIndicator(modifier = Modifier.height(14.dp), strokeWidth = 2.dp)
        } else if (icon != null) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.height(16.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
private fun ProductCard(
    product: ProductEntity,
    isHighlighted: Boolean,
    onEditClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isHighlighted)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 600),
        label = "highlightColor"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(product.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    "S/ ${"%.2f".format(product.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (product.isPendingSync) {
                    Spacer(modifier = Modifier.height(4.dp))
                    AssistChip(
                        onClick = {},
                        label = { Text("Pendiente de sincronizar", style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    )
                }
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar ${product.name}")
            }
        }
    }
}

@Composable
private fun EmptyState(hasQuery: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            if (hasQuery) "No se encontraron productos" else "No hay productos todavía",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EditProductDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Double) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var priceText by remember { mutableStateOf(product.price.toString()) }
    val priceError = priceText.toDoubleOrNull() == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar producto") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Precio") },
                    singleLine = true,
                    isError = priceError,
                    supportingText = { if (priceError) Text("Precio inválido") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { priceText.toDoubleOrNull()?.let { onConfirm(name, it) } },
                enabled = name.isNotBlank() && !priceError
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private fun formatTime(timestamp: Long): String =
    SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(timestamp))