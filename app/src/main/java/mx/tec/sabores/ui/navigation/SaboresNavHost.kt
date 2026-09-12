package mx.tec.sabores.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import mx.tec.sabores.BuildConfig
import mx.tec.sabores.domain.Review
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
    val snackbar = remember { SnackbarHostState() }
    var pendingDelete by remember { mutableStateOf<Review?>(null) }
    var menuOpen by remember { mutableStateOf(false) }
    var permissionPrompt by remember { mutableStateOf(false) }
    LaunchedEffect(model.actionMessage) {
        model.actionMessage?.let { snackbar.showSnackbar(it); model.clearMessage() }
    }
    pendingDelete?.let { review ->
        AlertDialog(onDismissRequest = { pendingDelete = null },
            title = { Text("¿Borrar tu reseña?") },
            text = { Text("Se eliminará del servidor y del detalle del restaurante.") },
            confirmButton = { TextButton(onClick = { pendingDelete = null; model.borrar(review) }) { Text("Borrar") } },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancelar") } })
    }
    if (permissionPrompt && BuildConfig.DEBUG) {
        AlertDialog(onDismissRequest = { permissionPrompt = false },
            title = { Text("Comprobar permisos") },
            text = { Text("Demostración del laboratorio: solicitar el borrado de una reseña del profesor con tu matrícula. El servidor debe rechazarlo con HTTP 403.") },
            confirmButton = { TextButton(onClick = { permissionPrompt = false; model.comprobarPermisos() }) { Text("Comprobar") } },
            dismissButton = { TextButton(onClick = { permissionPrompt = false }) { Text("Cancelar") } })
    }
    model.permissionCheck?.let { state ->
        AlertDialog(onDismissRequest = model::closePermissionCheck,
            title = { Text("Resultado de permisos") },
            text = {
                when (state) {
                    UiState.Cargando -> Text("Consultando al servidor…")
                    is UiState.Exito -> Text(state.datos)
                    is UiState.Error -> Text(state.mensaje)
                }
            },
            confirmButton = { TextButton(onClick = model::closePermissionCheck, enabled = state != UiState.Cargando) { Text("Cerrar") } })
    }
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            if (root) TopAppBar(
                title = { Text(if (route == Route.MY_REVIEWS) "Mis reseñas" else "Sabores en red") },
                actions = {
                    IconButton(onClick = {
                        if (route == Route.MY_REVIEWS) model.cargarMisResenas() else model.cargarRestaurantes()
                    }) { Icon(Icons.Default.Refresh, contentDescription = "Actualizar") }
                    if (BuildConfig.DEBUG) Box {
                        IconButton(onClick = { menuOpen = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Opciones del laboratorio") }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(text = { Text("Comprobar permisos (403)") },
                                onClick = { menuOpen = false; permissionPrompt = true })
                        }
                    }
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
        NavHost(nav, startDestination = Route.HOME,
            modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
            composable(Route.HOME) {
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { model.cargarRestaurantes() }
                EstadoView(model.restaurantes, model::cargarRestaurantes) { data ->
                    RestaurantListScreen(data, { nav.navigate(Route.detail(it)) })
                }
            }
            composable(Route.MY_REVIEWS) {
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { model.cargarMisResenas() }
                EstadoView(model.mias, model::cargarMisResenas) { data ->
                    MyReviewsScreen(data, model.alumno, model.busyReviewId,
                        onEdit = { nav.navigate(Route.editReview(it.restaurantId, it.id)) },
                        onDelete = { pendingDelete = it })
                }
            }
            composable(Route.DETAIL,
                arguments = listOf(navArgument(Route.ARG_RESTAURANT_ID) { type = NavType.IntType })) { destination ->
                val id = destination.arguments?.getInt(Route.ARG_RESTAURANT_ID) ?: return@composable
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { model.cargarDetalle(id) }
                EstadoView(model.detalle, { model.cargarDetalle(id) }, { nav.popBackStack() }) { detail ->
                    RestaurantDetailScreen(detail.restaurant, detail.summary, detail.reviews,
                        onWriteReviewClick = { nav.navigate(Route.newReview(id)) },
                        onBack = { nav.popBackStack() }, alumno = model.alumno, busyReviewId = model.busyReviewId,
                        onEdit = { nav.navigate(Route.editReview(it.restaurantId, it.id)) },
                        onDelete = { pendingDelete = it })
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
            composable(Route.EDIT_REVIEW, arguments = listOf(
                navArgument(Route.ARG_RESTAURANT_ID) { type = NavType.IntType },
                navArgument(Route.ARG_REVIEW_ID) { type = NavType.IntType }
            )) { destination ->
                val restaurantId = destination.arguments?.getInt(Route.ARG_RESTAURANT_ID) ?: return@composable
                val reviewId = destination.arguments?.getInt(Route.ARG_REVIEW_ID) ?: return@composable
                val form: NewReviewViewModel = viewModel()
                LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { model.cargarDetalle(restaurantId) }
                LaunchedEffect(form.uiState.savedReviewId) {
                    if (form.uiState.savedReviewId != null) nav.popBackStack()
                }
                EstadoView(model.detalle, { model.cargarDetalle(restaurantId) }, { nav.popBackStack() }) { detail ->
                    val review = detail.reviews.firstOrNull { it.id == reviewId }
                    if (review == null) {
                        ErrorView("La reseña ya no existe.", { model.cargarDetalle(restaurantId) }, { nav.popBackStack() })
                    } else if (!review.author.equals(model.alumno, ignoreCase = true)) {
                        ErrorView("Esa reseña no es tuya.", { model.cargarDetalle(restaurantId) }, { nav.popBackStack() })
                    } else {
                        LaunchedEffect(reviewId) { form.prepareEdit(review) }
                        NewReviewScreen(detail.restaurant, form.uiState, form::onStarsChange, form::onCommentChange,
                            onSave = form::guardarCambios, onCancel = { nav.popBackStack() })
                    }
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
