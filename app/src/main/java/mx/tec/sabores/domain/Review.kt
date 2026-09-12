package mx.tec.sabores.domain

data class Review(
    val restaurantId: Int,
    val stars: Int,
    val comment: String,
    val id: Int = 0,
    val author: String = ""
)
