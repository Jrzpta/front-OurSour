package cl.oursour.app.ui.recipes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
fun RecipesScreen(
    vm: RecipesViewModel,
    onOpen: (Long) -> Unit,
    onBack: () -> Unit,
    onGoCart: () -> Unit
) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.events.collect { snackbar.showSnackbar(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recetas para ti") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = onGoCart) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Carro")
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
            is UiState.Error -> ErrorView(s.message, onRetry = vm::search, modifier = Modifier.padding(padding))
            is UiState.Success -> LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(16.dp)) {
                s.data.message?.let { msg -> item { Text(msg, modifier = Modifier.padding(bottom = 8.dp)) } }
                items(s.data.recipes, key = { it.id }) { r -> RecipeCard(r, onClick = { onOpen(r.id) }) }
                if (s.data.suggestedProducts.isNotEmpty()) {
                    item {
                        Text("Productos sugeridos", style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp))
                    }
                    items(s.data.suggestedProducts, key = { "p" + it.id }) { p ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text(p.name, style = MaterialTheme.typography.titleMedium)
                                Text(p.description, style = MaterialTheme.typography.bodySmall)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(p.price.toClp(), style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f))
                                    Button(
                                        onClick = { vm.addSuggested(p.id) },
                                        enabled = p.active && p.stock > 0
                                    ) { Text(if (p.stock > 0) "Agregar" else "Sin stock") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
