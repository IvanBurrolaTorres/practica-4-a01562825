package mx.tec.sabores.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mx.tec.sabores.domain.RatingSummary
import mx.tec.sabores.domain.Restaurant
import mx.tec.sabores.domain.Review
import mx.tec.sabores.ui.components.RatingLabel
import mx.tec.sabores.ui.components.ReviewCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    restaurant: Restaurant,
    summary: RatingSummary,
    reviews: List<Review>,
    onWriteReviewClick: () -> Unit,
    onBack: () -> Unit,
    alumno: String,
    busyReviewId: Int?,
    onEdit: (Review) -> Unit,
    onDelete: (Review) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(restaurant.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onWriteReviewClick,
                icon = { Icon(Icons.Default.Star, contentDescription = null) },
                text = { Text("Escribir reseña") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(restaurant.emoji, style = MaterialTheme.typography.displayLarge)
                Text(
                    "${restaurant.cuisine} · ${restaurant.priceLabel}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                RatingLabel(summary)
                Spacer(Modifier.height(12.dp))
                Text(restaurant.description, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(4.dp))
                Text(restaurant.address, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(20.dp))
                Text("Reseñas", style = MaterialTheme.typography.titleLarge)
            }

            if (reviews.isEmpty()) {
                item {
                    Text(
                        "Nadie ha reseñado este lugar. Sé la primera persona.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(reviews, key = { it.id }) { review ->
                    ReviewCard(review, review.author.equals(alumno, ignoreCase = true),
                        busy = busyReviewId != null,
                        onEdit = { onEdit(review) }, onDelete = { onDelete(review) })
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}
