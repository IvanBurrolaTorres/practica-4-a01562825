package mx.tec.sabores.ui.state

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import mx.tec.sabores.data.RestaurantRepository
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
