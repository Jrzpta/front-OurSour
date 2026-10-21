package cl.oursour.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.data.remote.dto.MetricsResponse
import cl.oursour.app.data.repository.AdminRepository
import cl.oursour.app.ui.common.UiState
import cl.oursour.app.ui.common.toUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MetricsViewModel(private val repo: AdminRepository) : ViewModel() {
    private val _days = MutableStateFlow(30)
    val days = _days.asStateFlow()
    private val _state = MutableStateFlow<UiState<MetricsResponse>>(UiState.Loading)
    val state = _state.asStateFlow()

    init { load() }

    fun setDays(d: Int) { _days.value = d; load() }    // chips: 7 / 30 / 90 días

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = repo.metrics(_days.value).toUiState()
        }
    }
}

// En la pantalla:
//  - Si data.message != null  -> mostrar "No hay datos suficientes en el período consultado."
//  - Si no: tarjeta con data.totalSearches y tres listas (topIngredients, topRecipes, topCombinations)
//    donde cada fila muestra label + count y una barra proporcional al máximo de la lista.
