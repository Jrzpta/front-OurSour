package cl.oursour.app.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val INGREDIENTS = "ingredients"            // CU-01
    const val RECIPES = "recipes"                    // CU-02
    const val RECIPE_DETAIL = "recipe/{id}"          // CU-03
    fun recipeDetail(id: Long) = "recipe/$id"
    const val CART = "cart"                          // CU-04
    const val CHECKOUT = "checkout"
    const val ORDERS = "orders"
    const val ADMIN_PRODUCTS = "admin/products"      // CU-05
    const val ADMIN_METRICS = "admin/metrics"        // CU-06
}
