package cl.oursour.app.ui.ingredients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.data.remote.dto.IngredientCategory
import cl.oursour.app.data.remote.dto.IngredientDto
import cl.oursour.app.data.repository.IngredientRepository
import cl.oursour.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IngredientsUi(
    val loading: Boolean = true,
    val catalog: Map<IngredientCategory, List<IngredientDto>> = emptyMap(),
    val selected: Set<Long> = emptySet(),
    val error: String? = null,      // error de carga (muestra ErrorView)
    val message: String? = null     // mensaje corto (Snackbar)
)

class IngredientsViewModel(private val repo: IngredientRepository) : ViewModel() {
    private val _ui = MutableStateFlow(IngredientsUi())
    val ui: StateFlow<IngredientsUi> = _ui.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            val local = repo.localSelection.first()            // selección guardada en el teléfono
            when (val r = repo.catalog()) {
                is ApiResult.Success -> _ui.update {
                    it.copy(loading = false, catalog = r.data.groupBy { i -> i.category }, selected = local)
                }
                is ApiResult.Error -> _ui.update { it.copy(loading = false, error = r.message) }
            }
        }
    }

    fun toggle(id: Long) = _ui.update { s ->
        s.copy(selected = if (id in s.selected) s.selected - id else s.selected + id)
    }

    /** Pasos 3-5 del CU-01: confirmar, guardar y avisar. */
    fun save(onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            val msg = when (val r = repo.saveSelection(_ui.value.selected)) {
                is ApiResult.Success -> "Ingredientes guardados (${r.data.size})"
                is ApiResult.Error -> "Guardado en el teléfono. No se pudo sincronizar: ${r.message}"
            }
            _ui.update { it.copy(message = msg) }
            onSaved()
        }
    }

    /** Flujo alternativo CU-01: agregar ingrediente que no está en el listado. */
    fun addManual(name: String, category: IngredientCategory) {
        viewModelScope.launch {
            when (val r = repo.create(name.trim(), category)) {
                is ApiResult.Success -> _ui.update { s ->
                    val list = (s.catalog[category].orEmpty() + r.data).sortedBy { it.name }
                    s.copy(
                        catalog = s.catalog + (category to list),
                        selected = s.selected + r.data.id,
                        message = "Ingrediente \"${r.data.name}\" agregado"
                    )
                }
                is ApiResult.Error -> _ui.update { it.copy(message = r.message) } // ej. 409 "ya existe"
            }
        }
    }

    fun messageShown() = _ui.update { it.copy(message = null) }
}
