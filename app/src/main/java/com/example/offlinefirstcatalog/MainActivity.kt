package com.example.offlinefirstcatalog

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.offlinefirstcatalog.di.AppContainer
import com.example.offlinefirstcatalog.sync.FakePushService
import com.example.offlinefirstcatalog.sync.SyncScheduler
import com.example.offlinefirstcatalog.ui.ProductScreen
import com.example.offlinefirstcatalog.ui.ProductViewModel
import com.example.offlinefirstcatalog.ui.ProductViewModelFactory
import com.example.offlinefirstcatalog.ui.theme.OfflinefirstcatalogTheme

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestNotificationPermissionIfNeeded()

        val repository = AppContainer.getRepository(applicationContext)
        SyncScheduler.triggerImmediateSync(applicationContext)
        FakePushService.generateAndRegisterToken(applicationContext)

        val productIdFromNotification = intent.getIntExtra("productId", -1).takeIf { it != -1 }

        setContent {
            OfflinefirstcatalogTheme {
                val viewModel: ProductViewModel = viewModel(
                    factory = ProductViewModelFactory(repository)
                )
                ProductScreen(
                    viewModel = viewModel,
                    highlightProductId = productIdFromNotification
                )
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}