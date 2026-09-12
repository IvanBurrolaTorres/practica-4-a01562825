package mx.tec.sabores.data

import mx.tec.sabores.data.remote.*
import mx.tec.sabores.domain.*

class RestaurantRepository(private val api: SaboresApi = Network.api) {
    suspend fun getAll(): List<Restaurant> = api.getRestaurants().map { it.toDomain() }
    suspend fun getAllForList(): List<RestaurantEnLista> =
        api.getRestaurants().map { RestaurantEnLista(it.toDomain(), it.toSummary()) }
    suspend fun getById(id: Int): Restaurant = api.getRestaurant(id).toDomain()
    suspend fun getReviews(restaurantId: Int): List<Review> =
        api.getReviews(restaurantId).map { it.toDomain() }
    suspend fun getMyReviews(): List<Review> = api.getMyReviews().map { it.toDomain() }
    suspend fun addReview(restaurantId: Int, stars: Int, comment: String): Review =
        api.createReview(NewReviewBody(restaurantId, stars, comment.trim())).toDomain()
}

