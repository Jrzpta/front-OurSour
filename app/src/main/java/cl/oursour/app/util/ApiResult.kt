package cl.oursour.app.util

import cl.oursour.app.data.remote.dto.ApiErrorDto
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Error(
        val code: Int?,                                   // null = sin conexión
        val message: String,
        val fieldErrors: Map<String, String> = emptyMap() // errores por campo (HTTP 400)
    ) : ApiResult<Nothing>
}

private val gson = Gson()

/** Ejecuta una llamada a la API y transforma cualquier error en ApiResult.Error. */
suspend fun <T> safeApiCall(block: suspend () -> T): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (e: HttpException) {
    val body = e.response()?.errorBody()?.string()
    val apiError = runCatching { gson.fromJson(body, ApiErrorDto::class.java) }.getOrNull()
    val message = apiError?.message ?: when (e.code()) {
        401 -> "Tu sesión expiró. Inicia sesión nuevamente."
        403 -> "No tienes permisos para realizar esta acción."
        else -> "Error del servidor (${e.code()})."
    }
    ApiResult.Error(e.code(), message, apiError?.details.orEmpty())
} catch (e: IOException) {
    ApiResult.Error(null, "No hay conexión con el servidor. Revisa tu red e inténtalo de nuevo.")
}
