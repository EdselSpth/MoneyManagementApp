package ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun CategoryIcon(
    iconName: String,
    modifier: Modifier = Modifier,
    tint: Color = Color.White
) {
    val vector: ImageVector = when (iconName) {
        "Work" -> Icons.Default.Work
        "Laptop" -> Icons.Default.Laptop
        "TrendingUp" -> Icons.AutoMirrored.Filled.TrendingUp
        "Gift" -> Icons.Default.CardGiftcard
        "Restaurant" -> Icons.Default.Restaurant
        "Home" -> Icons.Default.Home
        "DirectionsTransit" -> Icons.Default.DirectionsTransit
        "Receipt" -> Icons.Default.Receipt
        "LocalHospital" -> Icons.Default.LocalHospital
        "Coffee" -> Icons.Default.LocalCafe
        "ShoppingBag" -> Icons.Default.ShoppingBag
        "Movie" -> Icons.Default.Movie
        "Palette" -> Icons.Default.Palette
        "Savings" -> Icons.Default.Savings
        "Star" -> Icons.Default.Star
        "Flight" -> Icons.Default.Flight
        "Shield" -> Icons.Default.Security
        else -> Icons.Default.Category
    }

    Icon(
        imageVector = vector,
        contentDescription = iconName,
        tint = tint,
        modifier = modifier
    )
}
