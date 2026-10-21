package cl.oursour.app.di

import android.content.Context

import cl.oursour.app.data.local.SessionStore
import cl.oursour.app.data.remote.dto.AuthInterceptor
import cl.oursour.app.data.remote.dto.OurSourApi
import cl.oursour.app.data.repository.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val sessionStore = SessionStore(context)

    /** Se emite cuando el backend responde 401: la UI vuelve al Login. */
    val sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val okHttp = OkHttpClient.Builder()
        .addInterceptor(
            AuthInterceptor(
                // OkHttp corre en un hilo de fondo, por eso runBlocking es aceptable aquí
                tokenProvider = { runBlocking { sessionStore.token.first() } },
                onUnauthorized = {
                    appScope.launch { sessionStore.clear(); sessionExpired.emit(Unit) }
                }
            )
        )
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY   // ver peticiones en Logcat
        })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    val api: OurSourApi = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/")
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OurSourApi::class.java)

    // Repositorios (una sola instancia para toda la app)
    val authRepository = AuthRepository(api, sessionStore)
    val ingredientRepository = IngredientRepository(api, sessionStore)
    val recipeRepository = RecipeRepository(api)
    val cartRepository = CartRepository(api)
    val adminRepository = AdminRepository(api)
}
