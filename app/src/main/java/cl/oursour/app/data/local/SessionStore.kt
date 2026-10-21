package cl.oursour.app.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import cl.oursour.app.data.remote.dto.AuthResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "oursour_session")

data class Session(val token: String, val name: String, val role: String) {
    val isAdmin: Boolean get() = role == "ADMIN"
}

class SessionStore(private val context: Context) {

    private object Keys {
        val TOKEN = stringPreferencesKey("token")
        val NAME = stringPreferencesKey("name")
        val ROLE = stringPreferencesKey("role")
        val INGREDIENTS = stringSetPreferencesKey("my_ingredient_ids")
    }

    /** Sesión actual (null si no hay login). */
    val session: Flow<Session?> = context.dataStore.data.map { p ->
        val token = p[Keys.TOKEN] ?: return@map null
        Session(token, p[Keys.NAME].orEmpty(), p[Keys.ROLE] ?: "USER")
    }

    /** Solo el token, lo usa el interceptor HTTP. */
    val token: Flow<String?> = context.dataStore.data.map { it[Keys.TOKEN] }

    suspend fun save(auth: AuthResponse) {
        context.dataStore.edit {
            it[Keys.TOKEN] = auth.token
            it[Keys.NAME] = auth.name
            it[Keys.ROLE] = auth.role
        }
    }

    suspend fun clear() = context.dataStore.edit { it.clear() }

    // ---- CU-01: ingredientes guardados LOCALMENTE ----
    val localIngredientIds: Flow<Set<Long>> = context.dataStore.data.map { p ->
        p[Keys.INGREDIENTS].orEmpty().mapNotNull { it.toLongOrNull() }.toSet()
    }

    suspend fun saveLocalIngredients(ids: Set<Long>) {
        context.dataStore.edit { it[Keys.INGREDIENTS] = ids.map { id -> id.toString() }.toSet() }
    }
}
