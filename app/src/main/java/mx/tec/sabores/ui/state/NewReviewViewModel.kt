package mx.tec.sabores.ui.state

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import mx.tec.sabores.data.RestaurantRepository
import mx.tec.sabores.domain.ReviewError
import mx.tec.sabores.domain.ReviewValidator
import retrofit2.HttpException

data class NewReviewUiState(
    val stars: Int = 0,
    val comment: String = "",
    val guardando: Boolean = false,
    val errorAlGuardar: String? = null,
    val savedReviewId: Int? = null
) {
    val commentError: ReviewError? =
        if (comment.isEmpty()) null else ReviewValidator.validateComment(comment)
    val canSave = (stars in 1..5) && !guardando && savedReviewId == null
    val charactersLeft = ReviewValidator.COMMENT_MAX - comment.trim().length
}

class NewReviewViewModel(private val repository: RestaurantRepository = RestaurantRepository()) : ViewModel() {
    var uiState by mutableStateOf(NewReviewUiState())
        private set
    fun onStarsChange(stars: Int) {
        if (!uiState.guardando) uiState = uiState.copy(stars = stars, errorAlGuardar = null)
    }
    fun onCommentChange(text: String) {
        if (!uiState.guardando && text.length <= ReviewValidator.COMMENT_MAX) {
            uiState = uiState.copy(comment = text, errorAlGuardar = null)
        }
    }
    fun publicar(restaurantId: Int) {
        if (!uiState.canSave) return
        val draft = uiState
        // La guarda cambia antes de lanzar: dos toques en el mismo frame tampoco duplican.
        uiState = draft.copy(guardando = true, errorAlGuardar = null)
        viewModelScope.launch {
            try {
                val created = repository.addReview(restaurantId, draft.stars, draft.comment)
                uiState = uiState.copy(guardando = false, savedReviewId = created.id)
            } catch (e: IOException) {
                uiState = uiState.copy(guardando = false,
                    errorAlGuardar = "No se pudo confirmar el envío. Revisa tu conexión y Mis reseñas antes de volver a publicar.")
            } catch (e: HttpException) {
                uiState = uiState.copy(guardando = false, errorAlGuardar = mensajeDe(e))
            } catch (e: SerializationException) {
                uiState = uiState.copy(guardando = false,
                    errorAlGuardar = "No se pudo leer la confirmación. Revisa Mis reseñas antes de reenviar.")
            }
        }
    }
}
