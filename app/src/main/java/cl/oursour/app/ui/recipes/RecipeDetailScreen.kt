package cl.oursour.app.ui.recipes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.oursour.app.data.remote.dto.RecipeDetailDto
import cl.oursour.app.ui.common.toClp

@Composable
fun RecipeDetailContent(d: RecipeDetailDto, adding: Boolean, onAdd: (Long) -> Unit, padding: PaddingValues) {
    LazyColumn(contentPadding = padding) {
        item {
            Text(d.title, style = MaterialTheme.typography.headlineSmall)
            d.description?.let { Text(it) }
            Text("Compatibilidad: ${d.matchPercentage}%")
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
