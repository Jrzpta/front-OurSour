package cl.oursour.app.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.data.remote.dto.RecipeSearchResponse
import cl.oursour.app.data.repository.CartRepository
import cl.oursour.app.data.repository.IngredientRepository
import cl.oursour.app.data.repository.RecipeRepository
import cl.oursour.app.ui.common.UiState
import cl.oursour.app.ui.common.toClp
import cl.oursour.app.ui.common.toUiState
import cl.oursour.app.util.ApiResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class RecipesViewModel(
    private val recipes: RecipeRepository,
    private val ingredients: IngredientRepository,
    private val cart: CartRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<RecipeSearchResponse>>(UiState.Loading)
    val state = _state.asStateFlow()
    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init { search() }

    fun search() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val ids = ingredients.localSelection.first()
            if (ids.isEmpty()) {
                _state.value = UiState.Error("Primero declara al menos un ingrediente.")
                return@launch
            }
            _state.value = recipes.search(ids).toUiState()
        }
    }

    /** Flujo alternativo CU-02: agregar un producto sugerido directamente al carro. */
    fun addSuggested(productId: Long) {
        viewModelScope.launch {
            when (val r = cart.add(productId)) {
                is ApiResult.Success -> _events.emit("Agregado. Total del carro: ${r.data.total.toClp()}")
                is ApiResult.Error -> _events.emit(r.message)
            }
        }
    }
}
