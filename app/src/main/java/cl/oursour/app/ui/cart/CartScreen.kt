package cl.oursour.app.ui.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.oursour.app.ui.common.ErrorView
import cl.oursour.app.ui.common.UiState
import cl.oursour.app.ui.common.toClp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(vm: CartViewModel, onBack: () -> Unit, onCheckout: () -> Unit, onOrders: () -> Unit) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.events.collect { snackbar.showSnackbar(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi carro") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Error -> ErrorView(s.message, onRetry = vm::load, modifier = Modifier.padding(padding))
            is UiState.Success -> {
                val order = s.data
                Column(Modifier.padding(padding).padding(16.dp)) {
                    if (order.items.isEmpty()) {
                        Text("Tu carro está vacío.", modifier = Modifier.weight(1f))
                    } else {
                        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 8.dp)) {
                            items(order.items, key = { it.itemId }) { item ->
                                Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(item.productName, style = MaterialTheme.typography.titleMedium)
                                        Text("${item.unitPrice.toClp()} c/u")
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { vm.decrease(item) }) {
                                                Icon(Icons.Default.Remove, contentDescription = "Disminuir")
                                            }
                                            Text("${item.quantity}")
                                            IconButton(onClick = { vm.increase(item) }) {
                                                Icon(Icons.Default.Add, contentDescription = "Aumentar")
                                            }
                                            Text(item.subtotal.toClp(), modifier = Modifier.weight(1f))
                                            IconButton(onClick = { vm.remove(item) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Quitar")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Text("Subtotal: ${order.subtotal.toClp()}")
                        Text("Envío: ${order.shippingCost.toClp()}")
                        Text("Total: ${order.total.toClp()}", style = MaterialTheme.typography.titleLarge)
                    }
                    Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = onOrders, modifier = Modifier.weight(1f)) { Text("Mis pedidos") }
                        Button(onClick = onCheckout, enabled = order.items.isNotEmpty(), modifier = Modifier.weight(1f)) {
                            Text("Pagar")
                        }
                    }
                }
            }
        }
    }
}
