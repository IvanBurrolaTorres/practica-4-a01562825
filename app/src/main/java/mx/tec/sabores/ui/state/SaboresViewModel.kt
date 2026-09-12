package mx.tec.sabores.ui.state

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import mx.tec.sabores.data.RestaurantRepository
import mx.tec.sabores.data.remote.Network
import mx.tec.sabores.BuildConfig
import mx.tec.sabores.domain.*

data class MyReviewItem(val restaurantName: String, val review: Review)
data class Detalle(val restaurant: Restaurant, val reviews: List<Review>) {
    val summary = RatingSummary.from(reviews)
}

class SaboresViewModel(private val repository: RestaurantRepository = RestaurantRepository()) : ViewModel() {
    var restaurantes by mutableStateOf<UiState<List<RestaurantEnLista>>>(UiState.Cargando)
        private set
    var detalle by mutableStateOf<UiState<Detalle>>(UiState.Cargando)
        private set
    var mias by mutableStateOf<UiState<List<MyReviewItem>>>(UiState.Cargando)
        private set
    val alumno = Network.alumno
    var busyReviewId by mutableStateOf<Int?>(null)
        private set
    var actionMessage by mutableStateOf<String?>(null)
        private set
    var permissionCheck by mutableStateOf<UiState<String>?>(null)
        private set
    private var currentDetailId: Int? = null

    fun clearMessage() { actionMessage = null }
    fun closePermissionCheck() {
        if (permissionCheck != UiState.Cargando) permissionCheck = null
    }

    fun borrar(review: Review) {
        if (busyReviewId != null) return
        if (!review.author.equals(alumno, ignoreCase = true)) {
            actionMessage = "Esa reseña no es tuya."
            return
        }
        busyReviewId = review.id
        viewModelScope.launch {
            try {
                when (val result = pedir { repository.deleteReview(review.id) }) {
                    is UiState.Exito -> if (result.datos) {
                        actionMessage = "Reseña eliminada."
                        cargarMisResenas()
                        cargarRestaurantes()
                        currentDetailId?.let { cargarDetalle(it) }
                    } else actionMessage = "HTTP 403: esa reseña no es tuya."
                    is UiState.Error -> actionMessage = result.mensaje
                    UiState.Cargando -> Unit
                }
            } finally { busyReviewId = null }
        }
    }

    /** Demostración exigida por el laboratorio, visible solo en la versión debug. */
    fun comprobarPermisos() {
        if (!BuildConfig.DEBUG || permissionCheck == UiState.Cargando) return
        permissionCheck = UiState.Cargando
        viewModelScope.launch {
            permissionCheck = pedir {
                val review = repository.getReviews(1).firstOrNull { it.author == "profesor" }
                if (review == null) "No hay una reseña del profesor disponible para esta comprobación."
                else if (!repository.deleteReview(review.id)) {
                    "HTTP 403: esa reseña no es tuya.\nReseña #${review.id}, autor: ${review.author}.\nEl servidor rechazó el borrado y la reseña se conserva."
                } else "El servidor permitió el borrado; revisa sus reglas de autorización."
            }
        }
    }

    private var listJob: Job? = null
    private var detailJob: Job? = null
    private var mineJob: Job? = null

    fun cargarRestaurantes() {
        listJob?.cancel()
        listJob = viewModelScope.launch {
            restaurantes = UiState.Cargando
            restaurantes = pedir { repository.getAllForList() }
        }
    }
    fun cargarDetalle(id: Int) {
        currentDetailId = id
        detailJob?.cancel()
        detailJob = viewModelScope.launch {
            detalle = UiState.Cargando
            detalle = pedir {
                coroutineScope {
                    val restaurant = async { repository.getById(id) }
                    val reviews = async { repository.getReviews(id) }
                    Detalle(restaurant.await(), reviews.await())
                }
            }
        }
    }
    fun cargarMisResenas() {
        mineJob?.cancel()
        mineJob = viewModelScope.launch {
            mias = UiState.Cargando
            mias = pedir {
                coroutineScope {
                    val restaurants = async { repository.getAll() }
                    val reviews = async { repository.getMyReviews() }
                    val names = restaurants.await().associate { it.id to it.name }
                    reviews.await().map { MyReviewItem(names[it.restaurantId] ?: "Restaurante", it) }
                }
            }
        }
    }
}
