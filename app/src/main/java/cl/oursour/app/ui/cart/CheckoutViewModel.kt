package cl.oursour.app.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.data.remote.dto.OrderDto
import cl.oursour.app.data.remote.dto.PaymentRequest
import cl.oursour.app.data.repository.CartRepository
import cl.oursour.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckoutUi(
    val step: Int = 1,                 // 1 = dirección, 2 = pago, 3 = confirmación
    val order: OrderDto? = null,
    val loading: Boolean = false,
    val error: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)

class CheckoutViewModel(private val repo: CartRepository) : ViewModel() {
    private val _ui = MutableStateFlow(CheckoutUi())
    val ui = _ui.asStateFlow()

    fun confirmAddress(street: String, city: String) = execute(nextStep = 2) { repo.checkout(street, city) }

    fun pay(card: PaymentRequest) {
        val id = _ui.value.order?.id ?: return
        execute(nextStep = 3) { repo.pay(id, card) }    // 402 si la tarjeta termina en 0002
    }

    private fun execute(nextStep: Int, call: suspend () -> ApiResult<OrderDto>) {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null, fieldErrors = emptyMap()) }
            when (val r = call()) {
                is ApiResult.Success -> _ui.update { it.copy(loading = false, order = r.data, step = nextStep) }
                is ApiResult.Error -> _ui.update {
                    it.copy(loading = false, error = r.message, fieldErrors = r.fieldErrors)
                }
            }
        }
    }
}
