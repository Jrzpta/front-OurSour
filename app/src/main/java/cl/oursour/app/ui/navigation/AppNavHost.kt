package cl.oursour.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.oursour.app.di.AppContainer
import cl.oursour.app.ui.auth.AuthViewModel
import cl.oursour.app.ui.auth.LoginScreen
import cl.oursour.app.ui.ingredients.IngredientsScreen
import cl.oursour.app.ui.ingredients.IngredientsViewModel
import cl.oursour.app.ui.recipes.RecipeDetailViewModel
import cl.oursour.app.ui.recipes.RecipesViewModel
import kotlinx.coroutines.flow.first

@Composable
fun AppNavHost(container: AppContainer) {
    val nav = rememberNavController()

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

    NavHost(navController = nav, startDestination = startDestination) {

        composable(Routes.LOGIN) {
            val vm: AuthViewModel = viewModel { AuthViewModel(container.authRepository) }
            LoginScreen(
                vm = vm,
                onLoggedIn = { isAdmin ->
                    nav.navigate(if (isAdmin) Routes.ADMIN_PRODUCTS else Routes.INGREDIENTS) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoRegister = { nav.navigate(Routes.REGISTER) }
            )
        }

        composable(Routes.INGREDIENTS) {
            val vm: IngredientsViewModel = viewModel { IngredientsViewModel(container.ingredientRepository) }
            IngredientsScreen(vm, onSearch = { nav.navigate(Routes.RECIPES) })
        }

        composable(Routes.RECIPES) {
            val vm: RecipesViewModel = viewModel {
                RecipesViewModel(container.recipeRepository, container.ingredientRepository, container.cartRepository)
            }
            RecipesScreen(vm, onOpen = { id -> nav.navigate(Routes.recipeDetail(id)) }, onBack = { nav.popBackStack() })
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

        // ... CART, CHECKOUT, ORDERS, ADMIN_PRODUCTS, ADMIN_METRICS siguen el mismo patrón
    }
}


