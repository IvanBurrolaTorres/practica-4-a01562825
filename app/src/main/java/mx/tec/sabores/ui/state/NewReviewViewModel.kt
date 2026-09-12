package mx.tec.sabores.ui.state

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.IOException
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import mx.tec.sabores.data.RestaurantRepository
import mx.tec.sabores.data.remote.Network
import mx.tec.sabores.domain.Review
import mx.tec.sabores.domain.ReviewError
import mx.tec.sabores.domain.ReviewValidator
import retrofit2.HttpException

data class NewReviewUiState(
    val stars: Int = 0,
    val comment: String = "",
    val guardando: Boolean = false,
    val errorAlGuardar: String? = null,
    val savedReviewId: Int? = null,
    val isEditing: Boolean = false
) {
    val commentError: ReviewError? =
        if (comment.isEmpty()) null else ReviewValidator.validateComment(comment)
    val canSave = ReviewValidator.isValid(stars, comment) && !guardando && savedReviewId == null
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
    private var original: Review? = null
    fun prepareEdit(review: Review) {
        if (original != null) return
        original = review
        uiState = NewReviewUiState(stars = review.stars, comment = review.comment, isEditing = true)
    }

    fun guardarCambios() {
        val previous = original ?: return
        if (!previous.author.equals(Network.alumno, ignoreCase = true)) {
            uiState = uiState.copy(errorAlGuardar = "Esa reseña no es tuya.")
            return
        }
        if (!uiState.canSave) return
        val draft = uiState
        val stars = draft.stars.takeIf { it != previous.stars }
        val comment = draft.comment.trim().takeIf { it != previous.comment }
        if (stars == null && comment == null) {
            uiState = uiState.copy(savedReviewId = previous.id)
            return
        }
        guardar { repository.editReview(previous.id, stars, comment) }
    }

    fun publicar(restaurantId: Int) {
        if (!uiState.canSave) return
        val draft = uiState
        guardar { repository.addReview(restaurantId, draft.stars, draft.comment) }
    }

    private fun guardar(operation: suspend () -> Review) {
        if (!uiState.canSave) return
        // La guarda cambia antes de lanzar: dos toques en el mismo frame tampoco duplican.
        uiState = uiState.copy(guardando = true, errorAlGuardar = null)
        viewModelScope.launch {
            try {
                val created = operation()
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
