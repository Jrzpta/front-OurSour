package cl.oursour.app.ui.recipes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.data.remote.dto.RecipeDetailDto
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

class RecipeDetailViewModel(
    private val recipeId: Long,
    private val recipes: RecipeRepository,
    private val ingredients: IngredientRepository,
    private val cart: CartRepository
) : ViewModel() {
    private val _state = MutableStateFlow<UiState<RecipeDetailDto>>(UiState.Loading)
    val state = _state.asStateFlow()

    private val _adding = MutableStateFlow(false)
    val adding = _adding.asStateFlow()

    private val _events = MutableSharedFlow<String>()   // mensajes para Snackbar
    val events = _events.asSharedFlow()

    init { load() }   // se llama UNA vez: cada GET registra una "vista" en las métricas

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            val ids = ingredients.localSelection.first()
            _state.value = recipes.detail(recipeId, ids).toUiState()
        }
    }

    /** CU-04: "Agregar al carro" con cantidad 1. */
    fun addToCart(productId: Long) {
        if (_adding.value) return            // evita doble toque
        viewModelScope.launch {
            _adding.value = true
            when (val r = cart.add(productId, 1)) {
                is ApiResult.Success ->
                    _events.emit("Producto agregado al carro. Total: ${r.data.total.toClp()}")
                is ApiResult.Error -> _events.emit(r.message)   // ej. 409 stock insuficiente
            }
            _adding.value = false
        }
    }
}
