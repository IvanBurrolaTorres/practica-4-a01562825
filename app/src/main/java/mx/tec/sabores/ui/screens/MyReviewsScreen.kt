package mx.tec.sabores.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.tec.sabores.domain.Review
import mx.tec.sabores.ui.components.ReviewCard
import mx.tec.sabores.ui.state.MyReviewItem

@Composable
fun MyReviewsScreen(items: List<MyReviewItem>, alumno: String, busyReviewId: Int?,
    onEdit: (Review) -> Unit, onDelete: (Review) -> Unit, modifier: Modifier = Modifier) {
    if (items.isEmpty()) {
        Box(modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Text("Todavía no has reseñado ningún lugar.")
        }
        return
    }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        items(items, key = { it.review.id }) { item ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(item.restaurantName, style = MaterialTheme.typography.titleMedium)
                ReviewCard(item.review, item.review.author.equals(alumno, ignoreCase = true),
                    busy = busyReviewId != null,
                    onEdit = { onEdit(item.review) }, onDelete = { onDelete(item.review) })
            }
        }
    }
}
