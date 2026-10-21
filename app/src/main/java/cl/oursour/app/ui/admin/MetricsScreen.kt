package cl.oursour.app.ui.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import cl.oursour.app.data.remote.dto.MetricItem
import cl.oursour.app.ui.common.ErrorView
import cl.oursour.app.ui.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetricsScreen(vm: MetricsViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsState()
    val days by vm.days.collectAsState()

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Métricas") },
            navigationIcon = {
                IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
            }
        )
    }) { padding ->
        Column(Modifier.padding(padding)) {
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7, 30, 90).forEach { d ->
                    FilterChip(selected = days == d, onClick = { vm.setDays(d) }, label = { Text("$d días") })
                }
            }
            when (val s = state) {
                is UiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                is UiState.Error -> ErrorView(s.message, onRetry = vm::load)
                is UiState.Success -> {
                    val m = s.data
                    if (m.message != null) {
                        Text("No hay datos suficientes en el período consultado.", modifier = Modifier.padding(16.dp))
                    } else LazyColumn(contentPadding = PaddingValues(16.dp)) {
                        item { Text("Búsquedas totales: ${m.totalSearches}", style = MaterialTheme.typography.titleLarge) }
                        item { MetricList("Ingredientes más usados", m.topIngredients) }
                        item { MetricList("Recetas más vistas", m.topRecipes) }
                        item { MetricList("Combinaciones frecuentes", m.topCombinations) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricList(title: String, items: List<MetricItem>) {
    Column(Modifier.padding(top = 16.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        val max = (items.maxOfOrNull { it.count } ?: 1L).coerceAtLeast(1L)
        items.forEach { it ->
            Row(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(it.label, modifier = Modifier.weight(1f))
                Text("${it.count}")
            }
            LinearProgressIndicator(progress = { it.count.toFloat() / max }, modifier = Modifier.fillMaxWidth())
        }
    }
}
