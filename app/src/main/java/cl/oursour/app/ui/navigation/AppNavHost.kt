package cl.oursour.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.oursour.app.di.AppContainer
import cl.oursour.app.ui.admin.AdminProductsScreen
import cl.oursour.app.ui.admin.AdminProductsViewModel
import cl.oursour.app.ui.admin.MetricsScreen
import cl.oursour.app.ui.admin.MetricsViewModel
import cl.oursour.app.ui.auth.AuthViewModel
import cl.oursour.app.ui.auth.LoginScreen
import cl.oursour.app.ui.auth.RegisterScreen
import cl.oursour.app.ui.cart.CartScreen
import cl.oursour.app.ui.cart.CartViewModel
import cl.oursour.app.ui.cart.CheckoutScreen
import cl.oursour.app.ui.cart.CheckoutViewModel
import cl.oursour.app.ui.cart.OrdersScreen
import cl.oursour.app.ui.cart.OrdersViewModel
import cl.oursour.app.ui.ingredients.IngredientsScreen
import cl.oursour.app.ui.ingredients.IngredientsViewModel
import cl.oursour.app.ui.recipes.RecipeDetailScreen
import cl.oursour.app.ui.recipes.RecipeDetailViewModel
import cl.oursour.app.ui.recipes.RecipesScreen
import cl.oursour.app.ui.recipes.RecipesViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(container: AppContainer) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()

    // 1) Decidir pantalla inicial según la sesión guardada
    var start by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val s = container.sessionStore.session.first()
        start = when {
            s == null -> Routes.LOGIN
            s.isAdmin -> Routes.ADMIN_PRODUCTS
            else -> Routes.INGREDIENTS
        }
    }
    // 2) Si el token vence (401) -> volver al Login
    LaunchedEffect(Unit) {
        container.sessionExpired.collect {
            nav.navigate(Routes.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } }
        }
    }
    val startDestination = start ?: return   // mientras lee DataStore no dibuja nada

    val goHome: (Boolean) -> Unit = { isAdmin ->
        nav.navigate(if (isAdmin) Routes.ADMIN_PRODUCTS else Routes.INGREDIENTS) {
            popUpTo(nav.graph.id) { inclusive = true }
        }
    }

    NavHost(navController = nav, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            val vm: AuthViewModel = viewModel { AuthViewModel(container.authRepository) }
            LoginScreen(vm = vm, onLoggedIn = goHome, onGoRegister = { nav.navigate(Routes.REGISTER) })
        }

        composable(Routes.REGISTER) {
            val vm: AuthViewModel = viewModel { AuthViewModel(container.authRepository) }
            RegisterScreen(vm = vm, onRegistered = goHome, onBack = { nav.popBackStack() })
        }

        composable(Routes.INGREDIENTS) {
            val vm: IngredientsViewModel = viewModel { IngredientsViewModel(container.ingredientRepository) }
            IngredientsScreen(
                vm,
                onSearch = { nav.navigate(Routes.RECIPES) },
                onLogout = {
                    scope.launch {
                        container.authRepository.logout()
                        nav.navigate(Routes.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } }
                    }
                }
            )
        }

        composable(Routes.RECIPES) {
            val vm: RecipesViewModel = viewModel {
                RecipesViewModel(container.recipeRepository, container.ingredientRepository, container.cartRepository)
            }
            RecipesScreen(
                vm,
                onOpen = { id -> nav.navigate(Routes.recipeDetail(id)) },
                onBack = { nav.popBackStack() },
                onGoCart = { nav.navigate(Routes.CART) }
            )
        }

        composable(
            Routes.RECIPE_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            val id = entry.arguments!!.getLong("id")
            val vm: RecipeDetailViewModel = viewModel {
                RecipeDetailViewModel(id, container.recipeRepository,
                    container.ingredientRepository, container.cartRepository)
            }
            RecipeDetailScreen(vm, onBack = { nav.popBackStack() }, onGoCart = { nav.navigate(Routes.CART) })
        }

        composable(Routes.CART) {
            val vm: CartViewModel = viewModel { CartViewModel(container.cartRepository) }
            CartScreen(
                vm,
                onBack = { nav.popBackStack() },
                onCheckout = { nav.navigate(Routes.CHECKOUT) },
                onOrders = { nav.navigate(Routes.ORDERS) }
            )
        }

        composable(Routes.CHECKOUT) {
            val vm: CheckoutViewModel = viewModel { CheckoutViewModel(container.cartRepository) }
            CheckoutScreen(
                vm,
                onBack = { nav.popBackStack() },
                onDone = {
                    nav.navigate(Routes.INGREDIENTS) { popUpTo(Routes.INGREDIENTS) { inclusive = true } }
                }
            )
        }

        composable(Routes.ORDERS) {
            val vm: OrdersViewModel = viewModel { OrdersViewModel(container.cartRepository) }
            OrdersScreen(vm, onBack = { nav.popBackStack() })
        }

        composable(Routes.ADMIN_PRODUCTS) {
            val vm: AdminProductsViewModel = viewModel { AdminProductsViewModel(container.adminRepository) }
            AdminProductsScreen(
                vm,
                onMetrics = { nav.navigate(Routes.ADMIN_METRICS) },
                onLogout = {
                    scope.launch {
                        container.authRepository.logout()
                        nav.navigate(Routes.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } }
                    }
                }
            )
        }

        composable(Routes.ADMIN_METRICS) {
            val vm: MetricsViewModel = viewModel { MetricsViewModel(container.adminRepository) }
            MetricsScreen(vm, onBack = { nav.popBackStack() })
        }
    }
}
