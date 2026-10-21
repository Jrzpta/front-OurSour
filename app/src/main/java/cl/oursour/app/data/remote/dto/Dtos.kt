package cl.oursour.app.data.remote.dto

// ---------- Autenticación ----------
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val name: String, val email: String, val password: String)
data class AuthResponse(
    val token: String,
    val tokenType: String,     // siempre "Bearer"
    val userId: Long,
    val name: String,
    val email: String,
    val role: String           // "USER" o "ADMIN"
)

// ---------- Ingredientes (CU-01) ----------
enum class IngredientCategory { PRODUCTO_OUR_SOUR, DESTILADO, LICOR, FRUTA, JUGO, MIXER, OTRO }

data class IngredientDto(val id: Long, val name: String, val category: IngredientCategory)
data class IngredientCreateRequest(val name: String, val category: IngredientCategory)
data class IngredientIdsRequest(val ingredientIds: List<Long>)
data class RecipeSearchRequest(val ingredientIds: List<Long>)

// ---------- Productos (CU-03, CU-05) ----------
data class ProductDto(
    val id: Long, val name: String, val description: String,
    val price: Int,            // CLP sin decimales
    val stock: Int, val active: Boolean
)
data class ProductRequest(
    val name: String, val description: String,
    val price: Int, val stock: Int, val active: Boolean? = true
)

// ---------- Recetas (CU-02, CU-03) ----------
data class RecipeMatchDto(
    val id: Long, val title: String, val description: String?,
    val matchPercentage: Int,
    val missingIngredients: List<String>,
    val needsOurSourProduct: Boolean,
    val product: ProductDto?   // puede venir null
)
data class RecipeSearchResponse(
    val recipes: List<RecipeMatchDto>,
    val suggestedProducts: List<ProductDto>,
    val message: String?
)
data class RecipeIngredientDto(
    val ingredientId: Long, val name: String, val category: IngredientCategory,
    val quantity: String, val available: Boolean
)
data class RecipeDetailDto(
    val id: Long, val title: String, val description: String?,
    val ingredients: List<RecipeIngredientDto>,
    val steps: List<String>,
    val matchPercentage: Int,
    val missingIngredients: List<String>,
    val needsOurSourProduct: Boolean,
    val product: ProductDto?
)

// ---------- Carro y pedidos (CU-04) ----------
data class CartItemRequest(val productId: Long, val quantity: Int = 1)
data class UpdateQuantityRequest(val quantity: Int)
data class CheckoutRequest(val street: String, val city: String)
data class PaymentRequest(
    val cardNumber: String,    // 16 dígitos
    val cardHolder: String,
    val expiry: String,        // formato MM/AA
    val cvv: String            // 3 o 4 dígitos
)
data class OrderItemDto(
    val itemId: Long, val productId: Long, val productName: String,
    val unitPrice: Int, val quantity: Int, val subtotal: Int
)
data class OrderDto(
    val id: Long,
    val status: String,        // EN_PREPARACION | SOLICITADO | PAGADO
    val items: List<OrderItemDto>,
    val subtotal: Int, val shippingCost: Int, val total: Int,
    val street: String?, val city: String?,
    val createdAt: String?,    // LocalDateTime llega como texto ISO
    val paymentReference: String?
)

// ---------- Métricas (CU-06) ----------
data class MetricItem(val label: String, val count: Long)
data class MetricsResponse(
    val days: Int, val totalSearches: Long,
    val topIngredients: List<MetricItem>,
    val topRecipes: List<MetricItem>,
    val topCombinations: List<MetricItem>,
    val message: String?
)

// ---------- Error estándar del backend ----------
data class ApiErrorDto(
    val timestamp: String?, val status: Int, val error: String?,
    val message: String?, val details: Map<String, String>?
)
