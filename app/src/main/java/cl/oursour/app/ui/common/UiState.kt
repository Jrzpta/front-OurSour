package cl.oursour.app.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cl.oursour.app.data.remote.dto.IngredientCategory
import cl.oursour.app.util.ApiResult

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

fun <T> ApiResult<T>.toUiState(): UiState<T> = when (this) {
    is ApiResult.Success -> UiState.Success(data)
    is ApiResult.Error -> UiState.Error(message)
}

// ui/common/Formatters.kt
fun Int.toClp(): String =
    java.text.NumberFormat.getCurrencyInstance(java.util.Locale.forLanguageTag("es-CL")).format(this)

fun IngredientCategory.label(): String = when (this) {
    IngredientCategory.PRODUCTO_OUR_SOUR -> "Productos Our Sour"
    IngredientCategory.DESTILADO -> "Destilados"
    IngredientCategory.LICOR -> "Licores"
    IngredientCategory.FRUTA -> "Frutas"
    IngredientCategory.JUGO -> "Jugos"
    IngredientCategory.MIXER -> "Mixers"
    IngredientCategory.OTRO -> "Otros"
}

// ui/common/ErrorView.kt  (pantalla de error con botón "Reintentar")
@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) { Text("Reintentar") }
    }
}
