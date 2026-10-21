package cl.oursour.app.data.remote.dto

import retrofit2.http.*

interface OurSourApi {

    // ---------- Autenticación (públicos) ----------
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("api/auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    // ---------- CU-01 Ingredientes ----------
    @GET("api/ingredients")
    suspend fun ingredients(@Query("category") category: IngredientCategory? = null): List<IngredientDto>

    @POST("api/ingredients")
    suspend fun createIngredient(@Body body: IngredientCreateRequest): IngredientDto

    @GET("api/me/ingredients")
    suspend fun myIngredients(): List<IngredientDto>

    @PUT("api/me/ingredients")
    suspend fun saveMyIngredients(@Body body: IngredientIdsRequest): List<IngredientDto>

    // ---------- CU-02 / CU-03 Recetas ----------
    @POST("api/recipes/search")
    suspend fun searchRecipes(@Body body: RecipeSearchRequest): RecipeSearchResponse

    @GET("api/recipes/{id}")
    suspend fun recipeDetail(
        @Path("id") id: Long,
        @Query("available") available: List<Long>? = null
    ): RecipeDetailDto

    // ---------- Productos (catálogo público) ----------
    @GET("api/products")
    suspend fun products(): List<ProductDto>

    @GET("api/products/{id}")
    suspend fun product(@Path("id") id: Long): ProductDto

    // ---------- CU-04 Carro ----------
    @GET("api/cart")
    suspend fun cart(): OrderDto

    @POST("api/cart/items")
    suspend fun addToCart(@Body body: CartItemRequest): OrderDto

    @PUT("api/cart/items/{itemId}")
    suspend fun updateCartItem(@Path("itemId") itemId: Long, @Body body: UpdateQuantityRequest): OrderDto

    @DELETE("api/cart/items/{itemId}")
    suspend fun removeCartItem(@Path("itemId") itemId: Long): OrderDto

    @POST("api/cart/checkout")
    suspend fun checkout(@Body body: CheckoutRequest): OrderDto

    // ---------- Pedidos y pago sandbox ----------
    @GET("api/orders")
    suspend fun myOrders(): List<OrderDto>

    @GET("api/orders/{id}")
    suspend fun order(@Path("id") id: Long): OrderDto

    @POST("api/orders/{id}/pay")
    suspend fun pay(@Path("id") id: Long, @Body body: PaymentRequest): OrderDto

    // ---------- CU-05 / CU-06 Administrador (rol ADMIN) ----------
    @GET("api/admin/products")
    suspend fun adminProducts(): List<ProductDto>

    @POST("api/admin/products")
    suspend fun createProduct(@Body body: ProductRequest): ProductDto

    @PUT("api/admin/products/{id}")
    suspend fun updateProduct(@Path("id") id: Long, @Body body: ProductRequest): ProductDto

    @DELETE("api/admin/products/{id}")
    suspend fun deactivateProduct(@Path("id") id: Long): Unit   // 204 sin cuerpo; si falla lanza HttpException

    @GET("api/admin/metrics")
    suspend fun metrics(@Query("days") days: Int = 30): MetricsResponse
}
