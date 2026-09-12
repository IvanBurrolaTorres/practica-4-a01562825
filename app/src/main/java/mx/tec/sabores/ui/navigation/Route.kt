package mx.tec.sabores.ui.navigation

object Route {
    const val HOME = "home"
    const val MY_REVIEWS = "myReviews"
    const val DETAIL = "detail/{restaurantId}"
    const val EDIT_REVIEW = "edit/{restaurantId}/{reviewId}"
    const val ARG_REVIEW_ID = "reviewId"
    const val NEW_REVIEW = "review/{restaurantId}"

    const val ARG_RESTAURANT_ID = "restaurantId"

    fun editReview(restaurantId: Int, reviewId: Int) = "edit/$restaurantId/$reviewId"
    fun detail(id: Int) = "detail/$id"
    fun newReview(id: Int) = "review/$id"
}
