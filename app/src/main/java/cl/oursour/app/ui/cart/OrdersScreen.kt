package cl.oursour.app.ui.cart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.oursour.app.ui.common.ErrorView
import cl.oursour.app.ui.common.UiState
import cl.oursour.app.ui.common.toClp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(vm: OrdersViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsState()
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Mis pedidos") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
            }
        )
    }) { padding ->
        when (val s = state) {
            is UiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Error -> ErrorView(s.message, onRetry = vm::load, modifier = Modifier.padding(padding))
            is UiState.Success ->
                if (s.data.isEmpty()) Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Text("Aún no tienes pedidos.")
                } else LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp)) {
                    items(s.data, key = { it.id }) { o ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Pedido #${o.id} · ${o.status}", style = MaterialTheme.typography.titleMedium)
                                o.createdAt?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                                o.items.forEach { Text("${it.quantity} × ${it.productName}") }
                                Text("Total: ${o.total.toClp()}", style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                }
        }
    }
}
