package cl.oursour.app.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.data.remote.dto.ProductDto
import cl.oursour.app.data.remote.dto.ProductRequest
import cl.oursour.app.data.repository.AdminRepository
import cl.oursour.app.ui.common.UiState
import cl.oursour.app.ui.common.toUiState
import cl.oursour.app.util.ApiResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminProductsViewModel(private val repo: AdminRepository) : ViewModel() {
    private val _state = MutableStateFlow<UiState<List<ProductDto>>>(UiState.Loading)
    val state = _state.asStateFlow()
    private val _events = MutableSharedFlow<String>()
    val events = _events.asSharedFlow()

    init { load() }

    fun load() {
        viewModelScope.launch { _state.value = repo.products().toUiState() }
    }

    /** id == null -> crear ; id != null -> editar */
    fun save(id: Long?, form: ProductRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            val r = if (id == null) repo.create(form) else repo.update(id, form)
            when (r) {
                is ApiResult.Success -> { _events.emit("Producto guardado"); load(); onDone() }
                is ApiResult.Error -> _events.emit(r.fieldErrors.values.firstOrNull() ?: r.message)
            }
        }
    }

    fun deactivate(id: Long) {
        viewModelScope.launch {
            when (val r = repo.deactivate(id)) {
                is ApiResult.Success -> { _events.emit("Producto desactivado"); load() }
                is ApiResult.Error -> _events.emit(r.message)
            }
        }
    }
    // Reactivar = save(id, form.copy(active = true)) porque el PUT acepta "active"
}

// Validación en el teléfono (espejo de las reglas del backend)
fun validateProduct(name: String, description: String, price: String, stock: String): Map<String, String> =
    buildMap {
        if (name.isBlank()) put("name", "El nombre es obligatorio")
        if (description.isBlank()) put("description", "La descripción es obligatoria")
        val p = price.toIntOrNull()
        if (p == null || p < 1) put("price", "El precio debe ser mayor a 0")
        val s = stock.toIntOrNull()
        if (s == null || s < 0) put("stock", "El stock no puede ser negativo")
    }
