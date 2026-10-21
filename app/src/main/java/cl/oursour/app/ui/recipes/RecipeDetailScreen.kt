package cl.oursour.app.ui.recipes

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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
import cl.oursour.app.data.remote.dto.RecipeDetailDto
import cl.oursour.app.ui.common.ErrorView
import cl.oursour.app.ui.common.UiState
import cl.oursour.app.ui.common.toClp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(vm: RecipeDetailViewModel, onBack: () -> Unit, onGoCart: () -> Unit) {
    val state by vm.state.collectAsState()
    val adding by vm.adding.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.events.collect { snackbar.showSnackbar(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Receta") },
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
            is UiState.Error -> ErrorView(s.message, onRetry = vm::load, modifier = Modifier.padding(padding))
            is UiState.Success -> RecipeDetailContent(
                d = s.data, adding = adding, onAdd = vm::addToCart,
                padding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp
                )
            )
        }
    }
}

@Composable
fun RecipeDetailContent(d: RecipeDetailDto, adding: Boolean, onAdd: (Long) -> Unit, padding: PaddingValues) {
    LazyColumn(contentPadding = padding) {
        item {
            Column {
                Text(d.title, style = MaterialTheme.typography.headlineSmall)
                d.description?.let { Text(it) }
                Text("Compatibilidad: ${d.matchPercentage}%")
            }
        }
        item { Text("Ingredientes", style = MaterialTheme.typography.titleMedium) }
        items(d.ingredients) { ing ->
            ListItem(
                headlineContent = { Text(ing.name) },
                supportingContent = { Text(ing.quantity) },
                leadingContent = {
                    Icon(
                        if (ing.available) Icons.Default.CheckCircle else Icons.Default.RemoveCircleOutline,
                        contentDescription = if (ing.available) "Disponible" else "Falta"
                    )
                }
            )
        }
        item { Text("Preparación", style = MaterialTheme.typography.titleMedium) }
        items(d.steps) { step -> Text(step, modifier = Modifier.padding(vertical = 4.dp)) }

        d.product?.let { p ->                       // el producto puede ser null
            item {
                Card(Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(p.name, style = MaterialTheme.typography.titleMedium)
                        Text(p.description, style = MaterialTheme.typography.bodySmall)
                        Text(p.price.toClp(), style = MaterialTheme.typography.titleLarge)
                        Button(
                            onClick = { onAdd(p.id) },
                            enabled = !adding && p.active && p.stock > 0,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        ) { Text(if (p.stock > 0) "Agregar al carro" else "Sin stock") }
                    }
                }
            }
        }
    }
}
