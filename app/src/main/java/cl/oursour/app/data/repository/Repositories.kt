package cl.oursour.app.data.repository

import cl.oursour.app.data.local.SessionStore
import cl.oursour.app.data.remote.dto.AuthResponse
import cl.oursour.app.data.remote.dto.CartItemRequest
import cl.oursour.app.data.remote.dto.CheckoutRequest
import cl.oursour.app.data.remote.dto.IngredientCategory
import cl.oursour.app.data.remote.dto.IngredientCreateRequest
import cl.oursour.app.data.remote.dto.IngredientDto
import cl.oursour.app.data.remote.dto.IngredientIdsRequest
import cl.oursour.app.data.remote.dto.LoginRequest
import cl.oursour.app.data.remote.dto.OurSourApi
import cl.oursour.app.data.remote.dto.PaymentRequest
import cl.oursour.app.data.remote.dto.ProductRequest
import cl.oursour.app.data.remote.dto.RecipeSearchRequest
import cl.oursour.app.data.remote.dto.RegisterRequest
import cl.oursour.app.data.remote.dto.UpdateQuantityRequest
import cl.oursour.app.util.ApiResult
import cl.oursour.app.util.safeApiCall
import kotlinx.coroutines.flow.Flow

class AuthRepository(private val api: OurSourApi, private val store: SessionStore) {
    suspend fun login(email: String, password: String): ApiResult<AuthResponse> {
        val r = safeApiCall { api.login(LoginRequest(email, password)) }
        if (r is ApiResult.Success) store.save(r.data)      // guarda el token
        return r
    }
    suspend fun register(name: String, email: String, password: String): ApiResult<AuthResponse> {
        val r = safeApiCall { api.register(RegisterRequest(name, email, password)) }
        if (r is ApiResult.Success) store.save(r.data)
        return r
    }
    suspend fun logout() = store.clear()
}

class IngredientRepository(private val api: OurSourApi, private val store: SessionStore) {
    val localSelection: Flow<Set<Long>> = store.localIngredientIds

    suspend fun catalog() = safeApiCall { api.ingredients() }
    suspend fun remoteSelection() = safeApiCall { api.myIngredients() }
    suspend fun create(name: String, category: IngredientCategory) =
        safeApiCall { api.createIngredient(IngredientCreateRequest(name, category)) }

    /** CU-01 paso 4: guarda local y además sincroniza con el backend. */
    suspend fun saveSelection(ids: Set<Long>): ApiResult<List<IngredientDto>> {
        store.saveLocalIngredients(ids)
        return safeApiCall { api.saveMyIngredients(IngredientIdsRequest(ids.toList())) }
    }
}

class RecipeRepository(private val api: OurSourApi) {
    suspend fun search(ids: Set<Long>) =
        safeApiCall { api.searchRecipes(RecipeSearchRequest(ids.toList())) }
    suspend fun detail(id: Long, available: Set<Long>) =
        safeApiCall { api.recipeDetail(id, available.toList().ifEmpty { null }) }
}

class CartRepository(private val api: OurSourApi) {
    suspend fun get() = safeApiCall { api.cart() }
    suspend fun add(productId: Long, qty: Int = 1) = safeApiCall { api.addToCart(
        CartItemRequest(
            productId,
            qty
        )
    ) }
    suspend fun update(itemId: Long, qty: Int) = safeApiCall { api.updateCartItem(itemId,
        UpdateQuantityRequest(qty)
    ) }
    suspend fun remove(itemId: Long) = safeApiCall { api.removeCartItem(itemId) }
    suspend fun checkout(street: String, city: String) = safeApiCall { api.checkout(
        CheckoutRequest(
            street,
            city
        )
    ) }
    suspend fun pay(orderId: Long, card: PaymentRequest) = safeApiCall { api.pay(orderId, card) }
    suspend fun orders() = safeApiCall { api.myOrders() }
}

class AdminRepository(private val api: OurSourApi) {
    suspend fun products() = safeApiCall { api.adminProducts() }
    suspend fun create(p: ProductRequest) = safeApiCall { api.createProduct(p) }
    suspend fun update(id: Long, p: ProductRequest) = safeApiCall { api.updateProduct(id, p) }
    suspend fun deactivate(id: Long) = safeApiCall { api.deactivateProduct(id) }
    suspend fun metrics(days: Int) = safeApiCall { api.metrics(days) }
}
