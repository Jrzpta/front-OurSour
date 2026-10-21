package cl.oursour.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.oursour.app.data.repository.AuthRepository
import cl.oursour.app.util.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUi(
    val loading: Boolean = false,
    val error: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)

class AuthViewModel(private val repo: AuthRepository) : ViewModel() {
    private val _ui = MutableStateFlow(AuthUi())
    val ui: StateFlow<AuthUi> = _ui.asStateFlow()

    fun login(email: String, password: String, onSuccess: (isAdmin: Boolean) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _ui.update { it.copy(error = "Completa correo y contraseña") }
            return
        }
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null, fieldErrors = emptyMap()) }
            when (val r = repo.login(email.trim(), password)) {
                is ApiResult.Success -> {
                    _ui.update { it.copy(loading = false) }
                    onSuccess(r.data.role == "ADMIN")
                }
                is ApiResult.Error -> _ui.update {
                    it.copy(loading = false, error = r.message, fieldErrors = r.fieldErrors)
                }
            }
        }
    }
    // register(name, email, password, onSuccess) es idéntico pero llama a repo.register(...)
}
