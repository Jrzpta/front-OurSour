package cl.oursour.app.ui.recipes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cl.oursour.app.data.remote.dto.RecipeMatchDto

@Composable
fun RecipeCard(recipe: RecipeMatchDto, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(recipe.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text("${recipe.matchPercentage}%", style = MaterialTheme.typography.titleMedium)
            }
            recipe.description?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { recipe.matchPercentage / 100f },
                modifier = Modifier.fillMaxWidth()
            )
            if (recipe.missingIngredients.isNotEmpty()) {
                Text("Te falta: " + recipe.missingIngredients.joinToString(),
                    style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 6.dp))
            }
            if (recipe.needsOurSourProduct && recipe.product != null) {
                AssistChip(onClick = onClick, label = { Text("Necesitas ${recipe.product.name}") })
            }
        }
    }
}
