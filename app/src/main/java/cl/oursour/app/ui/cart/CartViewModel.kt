package cl.oursour.app.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.data.remote.dto.OrderDto
import cl.oursour.app.data.remote.dto.OrderItemDto
import cl.oursour.app.data.repository.CartRepository
import cl.oursour.app.ui.common.UiState
import cl.oursour.app.util.ApiResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(private val repo: CartRepository) : ViewModel() {
    private val _state = MutableStateFlow<UiState<OrderDto>>(UiState.Loading)
    val state = _state.asStateFlow()
    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init { load() }

    fun load() = execute { repo.get() }
    fun increase(item: OrderItemDto) = execute { repo.update(item.itemId, item.quantity + 1) }
    fun decrease(item: OrderItemDto) {
        if (item.quantity > 1) execute { repo.update(item.itemId, item.quantity - 1) }
        else remove(item)                                 // el mínimo que acepta el backend es 1
    }
    fun remove(item: OrderItemDto) = execute { repo.remove(item.itemId) }

    /** Todas las operaciones del carro devuelven el carro completo actualizado. */
    private fun execute(call: suspend () -> ApiResult<OrderDto>) {
        viewModelScope.launch {
            when (val r = call()) {
                is ApiResult.Success -> _state.value = UiState.Success(r.data)
                is ApiResult.Error ->
                    if (_state.value is UiState.Success) _events.emit(r.message)   // ej. stock
                    else _state.value = UiState.Error(r.message)
            }
        }
    }
}
