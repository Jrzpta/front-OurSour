package cl.oursour.app.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.data.remote.dto.OrderDto
import cl.oursour.app.data.repository.CartRepository
import cl.oursour.app.ui.common.UiState
import cl.oursour.app.ui.common.toUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrdersViewModel(private val repo: CartRepository) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<OrderDto>>>(UiState.Loading)
    val state = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value = UiState.Loading
            _state.value = repo.orders().toUiState()
        }
    }
}
