package mx.tec.sabores.data.remote

import mx.tec.sabores.domain.Restaurant
import mx.tec.sabores.domain.Review

fun RestaurantDto.toDomain() = Restaurant(id, name, cuisine, address, description, priceLevel, emoji)
fun ReviewDto.toDomain() = Review(
    id = id, restaurantId = restaurantId, author = author, stars = stars, comment = comment
)
