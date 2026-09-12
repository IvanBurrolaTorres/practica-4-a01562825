package mx.tec.sabores.ui.state

import java.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.*
import retrofit2.HttpException

fun mensajeDe(e: HttpException): String {
    // Un cuerpo HTML, vacío o JSON no objeto tampoco debe romper la pantalla de error.
    val body = e.response()?.errorBody()?.string()
    val message = body?.let { runCatching {
        (Json.parseToJsonElement(it) as? JsonObject)?.get("error")?.let { value ->
            (value as? JsonPrimitive)?.contentOrNull
        }
    }.getOrNull() }
    return when (e.code()) {
        401 -> "Falta una matrícula válida en X-Alumno. Revisa Network.alumno."
        403 -> message ?: "Esa reseña no es tuya."
        404 -> "Ese elemento ya no existe. Actualiza la lista."
        422 -> message ?: "Los datos no son válidos."
        else -> "El servidor respondió ${e.code()}. Inténtalo de nuevo."
    }
}

suspend fun <T> pedir(block: suspend () -> T): UiState<T> = try {
    UiState.Exito(block())
} catch (e: IOException) {
    UiState.Error("No hay conexión. Revisa tu internet y vuelve a intentar.")
} catch (e: HttpException) {
    UiState.Error(mensajeDe(e))
} catch (e: SerializationException) {
    UiState.Error("La respuesta del servidor no tiene el formato esperado.")
}
// CancellationException se propaga: cancelar una carga no es un error de red.
