package mx.tec.sabores.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import mx.tec.sabores.ui.components.*
import mx.tec.sabores.ui.screens.*
import mx.tec.sabores.ui.state.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaboresApp() {
    val nav = rememberNavController()
    val model: SaboresViewModel = viewModel()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val root = MenuItem.entries.any { it.route == route }
    Scaffold(
        topBar = {
            if (root) TopAppBar(
                title = { Text(if (route == Route.MY_REVIEWS) "Mis reseñas" else "Sabores en red") },
                actions = {
                    IconButton(onClick = {
                        if (route == Route.MY_REVIEWS) model.cargarMisResenas() else model.cargarRestaurantes()
                    }) { Icon(Icons.Default.Refresh, contentDescription = "Actualizar") }
                }
            )
        },
        bottomBar = {
            if (root) NavigationBar {
                MenuItem.entries.forEach { item ->
                    NavigationBarItem(selected = route == item.route,
                        onClick = { nav.navigate(item.route) {
                            popUpTo(Route.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        } },
                        icon = { Icon(item.icon, contentDescription = null) },
                        label = { Text(item.label) })
                }
            }
        }
    ) { padding ->
        NavHost(nav, startDestination = Route.HOME, modifier = Modifier.padding(padding)) {
            composable(Route.HOME) {
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { model.cargarRestaurantes() }
                EstadoView(model.restaurantes, model::cargarRestaurantes) { data ->
                    RestaurantListScreen(data, { nav.navigate(Route.detail(it)) })
                }
            }
            composable(Route.MY_REVIEWS) {
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { model.cargarMisResenas() }
                EstadoView(model.mias, model::cargarMisResenas) { data -> MyReviewsScreen(data) }
            }
            composable(Route.DETAIL,
                arguments = listOf(navArgument(Route.ARG_RESTAURANT_ID) { type = NavType.IntType })) { destination ->
                val id = destination.arguments?.getInt(Route.ARG_RESTAURANT_ID) ?: return@composable
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { model.cargarDetalle(id) }
                EstadoView(model.detalle, { model.cargarDetalle(id) }, { nav.popBackStack() }) { detail ->
                    RestaurantDetailScreen(detail.restaurant, detail.summary, detail.reviews,
                        onWriteReviewClick = { nav.navigate(Route.newReview(id)) },
                        onBack = { nav.popBackStack() })
                }
            }
            composable(Route.NEW_REVIEW,
                arguments = listOf(navArgument(Route.ARG_RESTAURANT_ID) { type = NavType.IntType })) { destination ->
                val id = destination.arguments?.getInt(Route.ARG_RESTAURANT_ID) ?: return@composable
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { model.cargarDetalle(id) }
                val form: NewReviewViewModel = viewModel()
                LaunchedEffect(form.uiState.savedReviewId) {
                    if (form.uiState.savedReviewId != null) nav.popBackStack()
                }
                EstadoView(model.detalle, { model.cargarDetalle(id) }, { nav.popBackStack() }) { detail ->
                    NewReviewScreen(detail.restaurant, form.uiState, form::onStarsChange, form::onCommentChange,
                        onSave = { form.publicar(id) }, onCancel = { nav.popBackStack() })
                }
            }
        }
    }
}

@Composable
private fun <T> EstadoView(state: UiState<T>, retry: () -> Unit, back: (() -> Unit)? = null,
    content: @Composable (T) -> Unit) {
    when (state) {
        UiState.Cargando -> CargandoView()
        is UiState.Error -> ErrorView(state.mensaje, retry, back)
        is UiState.Exito -> content(state.datos)
    }
}
