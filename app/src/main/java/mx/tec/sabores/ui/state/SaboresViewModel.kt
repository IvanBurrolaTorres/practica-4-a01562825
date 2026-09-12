package mx.tec.sabores.ui.state

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import mx.tec.sabores.data.RestaurantRepository
import mx.tec.sabores.domain.*

data class MyReviewItem(val restaurantName: String, val review: Review)
data class Detalle(val restaurant: Restaurant, val reviews: List<Review>) {
    val summary = RatingSummary.from(reviews)
}

class SaboresViewModel(private val repository: RestaurantRepository = RestaurantRepository()) : ViewModel() {
    var restaurantes by mutableStateOf<List<RestaurantEnLista>>(emptyList())
        private set
    var detalle by mutableStateOf<Detalle?>(null)
        private set
    var mias by mutableStateOf<List<MyReviewItem>>(emptyList())
        private set
    init { cargarRestaurantes() }
    fun cargarRestaurantes() { viewModelScope.launch { restaurantes = repository.getAllForList() } }
    fun cargarDetalle(id: Int) {
        viewModelScope.launch { detalle = Detalle(repository.getById(id), repository.getReviews(id)) }
    }
    fun cargarMisResenas() {
        viewModelScope.launch {
            val names = repository.getAll().associate { it.id to it.name }
            mias = repository.getMyReviews().map { MyReviewItem(names[it.restaurantId] ?: "Restaurante", it) }
        }
    }
}
