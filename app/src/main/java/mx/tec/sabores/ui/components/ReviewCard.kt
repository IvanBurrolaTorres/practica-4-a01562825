package mx.tec.sabores.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.tec.sabores.domain.Review

@Composable
fun ReviewCard(review: Review, own: Boolean, busy: Boolean, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(if (own) "Tu reseña · #${review.id}" else "${review.author} · #${review.id}",
                style = MaterialTheme.typography.labelMedium)
            StarsRow(review.stars)
            Text(review.comment, style = MaterialTheme.typography.bodyMedium)
            if (own) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit, enabled = !busy) { Text("Editar") }
                TextButton(onClick = onDelete, enabled = !busy) { Text(if (busy) "Eliminando…" else "Borrar") }
            }
        }
    }
}
