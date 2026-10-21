package cl.oursour.app.ui.admin

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.oursour.app.data.remote.dto.ProductDto
import cl.oursour.app.data.remote.dto.ProductRequest
import cl.oursour.app.ui.common.ErrorView
import cl.oursour.app.ui.common.UiState
import cl.oursour.app.ui.common.toClp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProductsScreen(vm: AdminProductsViewModel, onMetrics: () -> Unit, onLogout: () -> Unit) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    // editing = producto en edición; creating = diálogo de alta
    var editing by remember { mutableStateOf<ProductDto?>(null) }
    var creating by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { vm.events.collect { snackbar.showSnackbar(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos") },
                actions = {
                    IconButton(onClick = onMetrics) { Icon(Icons.Default.BarChart, contentDescription = "Métricas") }
                    IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Salir") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = { creating = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo producto")
            }
        }
    ) { padding ->
        when (val s = state) {
            is UiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            is UiState.Error -> ErrorView(s.message, onRetry = vm::load, modifier = Modifier.padding(padding))
            is UiState.Success -> LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp)) {
                items(s.data, key = { it.id }) { p ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(p.name + if (!p.active) " (inactivo)" else "", style = MaterialTheme.typography.titleMedium)
                            Text(p.description, style = MaterialTheme.typography.bodySmall)
                            Text("${p.price.toClp()} · stock ${p.stock}")
                            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                                TextButton(onClick = { editing = p }) { Text("Editar") }
                                if (p.active) TextButton(onClick = { vm.deactivate(p.id) }) { Text("Desactivar") }
                            }
                        }
                    }
                }
            }
        }
    }

    if (creating || editing != null) {
        ProductFormDialog(
            initial = editing,
            onDismiss = { creating = false; editing = null },
            onSave = { form ->
                vm.save(editing?.id, form) { creating = false; editing = null }
            }
        )
    }
}

@Composable
private fun ProductFormDialog(initial: ProductDto?, onDismiss: () -> Unit, onSave: (ProductRequest) -> Unit) {
    var name by remember { mutableStateOf(initial?.name.orEmpty()) }
    var description by remember { mutableStateOf(initial?.description.orEmpty()) }
    var price by remember { mutableStateOf(initial?.price?.toString().orEmpty()) }
    var stock by remember { mutableStateOf(initial?.stock?.toString().orEmpty()) }
    var active by remember { mutableStateOf(initial?.active ?: true) }
    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nuevo producto" else "Editar producto") },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, singleLine = true,
                    isError = "name" in errors, supportingText = { errors["name"]?.let { Text(it) } })
                OutlinedTextField(description, { description = it }, label = { Text("Descripción") },
                    isError = "description" in errors, supportingText = { errors["description"]?.let { Text(it) } })
                OutlinedTextField(price, { price = it.filter(Char::isDigit) }, label = { Text("Precio (CLP)") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = "price" in errors, supportingText = { errors["price"]?.let { Text(it) } })
                OutlinedTextField(stock, { stock = it.filter(Char::isDigit) }, label = { Text("Stock") },
                    singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = "stock" in errors, supportingText = { errors["stock"]?.let { Text(it) } })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Activo", modifier = Modifier.weight(1f))
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                errors = validateProduct(name, description, price, stock)
                if (errors.isEmpty()) {
                    onSave(ProductRequest(name.trim(), description.trim(), price.toInt(), stock.toInt(), active))
                }
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
