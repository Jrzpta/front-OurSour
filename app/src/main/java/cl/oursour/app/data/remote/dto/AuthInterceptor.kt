package cl.oursour.app.data.remote.dto

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val tokenProvider: () -> String?,
    private val onUnauthorized: () -> Unit
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val isAuthCall = original.url.encodedPath.startsWith("/api/auth/")
        val token = tokenProvider()

        val request = if (token != null && !isAuthCall) {
            original.newBuilder().header("Authorization", "Bearer $token").build()
        } else original

        val response = chain.proceed(request)
        // Token vencido (dura 24 h) o inválido -> forzar nuevo login
        if (response.code == 401 && !isAuthCall) onUnauthorized()
        return response
    }
}
