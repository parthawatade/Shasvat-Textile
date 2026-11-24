package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.OrderStatus
import com.example.data.model.OrderStatusStep
import com.example.data.model.PriceBreakdown
import com.example.data.model.Product
import com.example.ui.theme.DarkEspresso
import com.example.ui.theme.PaleGoldContainer
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SageContainer
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SubtleGold
import com.example.ui.theme.TerracottaRed
import com.example.ui.theme.WarmBeige
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatInr(amount: Int): String {
    val fmt = NumberFormat.getNumberInstance(Locale("en", "IN"))
    return "₹${fmt.format(amount)}"
}

fun formatInrDouble(amount: Double): String {
    val fmt = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }
    return "₹${fmt.format(amount)}"
}

fun formatDateTime(millis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    return sdf.format(Date(millis))
}

fun resolveProductDrawable(name: String?): Int {
    return when (name) {
        "img_hero_solapur_loom" -> R.drawable.img_hero_solapur_loom
        "img_cat_satranji" -> R.drawable.img_cat_satranji
        "img_cat_towels" -> R.drawable.img_cat_towels
        "img_cat_apron_napkin" -> R.drawable.img_cat_apron_napkin
        "img_cat_chadar" -> R.drawable.img_cat_chadar
        else -> R.drawable.img_cat_chadar
    }
}

@Composable
fun ShasvatLogo(
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    darkBackground: Boolean = false
) {
    val titleColor = if (darkBackground) PureWhite else MaterialTheme.colorScheme.primary
    val accentColor = SubtleGold
    val subtitleColor = if (darkBackground) WarmBeige else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Woven Solapuri diamond & shuttle emblem drawn crisply in Compose Canvas
        Box(
            modifier = Modifier
                .size(if (compact) 36.dp else 42.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkEspresso),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(if (compact) 26.dp else 30.dp)) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val cy = h / 2f

                // Outer diamond weave
                val outer = Path().apply {
                    moveTo(cx, h * 0.08f)
                    lineTo(w * 0.92f, cy)
                    lineTo(cx, h * 0.92f)
                    lineTo(w * 0.08f, cy)
                    close()
                }
                drawPath(outer, color = accentColor, style = Stroke(width = 2.2f))

                // Inner diamond
                val inner = Path().apply {
                    moveTo(cx, h * 0.25f)
                    lineTo(w * 0.75f, cy)
                    lineTo(cx, h * 0.75f)
                    lineTo(w * 0.25f, cy)
                    close()
                }
                drawPath(inner, color = WarmBeige, style = Stroke(width = 1.6f))

                // Loom shuttle diagonal line
                drawLine(
                    color = accentColor,
                    start = Offset(w * 0.24f, h * 0.76f),
                    end = Offset(w * 0.76f, h * 0.24f),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }

        Column {
            Text(
                text = "SHASVAT TEXTILES",
                fontFamily = PlayfairDisplayFontFamily,
                fontWeight = FontWeight.Bold,
                style = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!compact) {
                Text(
                    text = "SOLAPURI HERITAGE LOOM",
                    style = MaterialTheme.typography.labelSmall,
                    color = subtitleColor,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ProductThumbnail(
    product: Product,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val uploadedFirst = product.uploadedImageUris.firstOrNull()
    val drawableRes = resolveProductDrawable(product.imageDrawables.firstOrNull())
    if (!uploadedFirst.isNullOrBlank()) {
        AsyncImage(
            model = uploadedFirst,
            contentDescription = product.name,
            modifier = modifier,
            contentScale = contentScale,
            error = painterResource(id = drawableRes),
            placeholder = painterResource(id = drawableRes)
        )
    } else {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = product.name,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
fun ProductMediaItem(
    imageSource: Any,
    contentDescription: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    when (imageSource) {
        is String -> {
            if (imageSource.startsWith("content://") || imageSource.startsWith("file://") || imageSource.startsWith("http")) {
                AsyncImage(
                    model = imageSource,
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = contentScale
                )
            } else {
                Image(
                    painter = painterResource(id = resolveProductDrawable(imageSource)),
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = contentScale
                )
            }
        }
        is Int -> {
            Image(
                painter = painterResource(id = imageSource),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    isWishlisted: Boolean,
    onProductClick: () -> Unit,
    onAddToCart: () -> Unit,
    onToggleWishlist: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}")
            .clickable(onClick = onProductClick),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                ProductThumbnail(
                    product = product,
                    modifier = Modifier.fillMaxSize()
                )

                // Top badges
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        product.discountPercent?.let { pct ->
                            Surface(
                                color = TerracottaRed,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "$pct% OFF",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PureWhite,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (product.isBestSeller) {
                            Surface(
                                color = PaleGoldContainer,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "Best Seller",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = DarkEspresso,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        color = PureWhite.copy(alpha = 0.9f),
                        shape = CircleShape,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        IconButton(
                            onClick = onToggleWishlist,
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("wishlist_btn_${product.id}")
                        ) {
                            Icon(
                                imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = if (isWishlisted) "Remove from wishlist" else "Add to wishlist",
                                tint = if (isWishlisted) TerracottaRed else DarkEspresso,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.categoryName,
                    style = MaterialTheme.typography.labelSmall,
                    color = SubtleGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (product.reviewCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            color = SageContainer,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = String.format(Locale.US, "%.1f", product.averageRating),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SageSuccess
                                )
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = SageSuccess,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                        Text(
                            text = "(${product.reviewCount})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formatInr(product.price),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (product.originalPrice != null && product.originalPrice > product.price) {
                        Text(
                            text = formatInr(product.originalPrice),
                            style = MaterialTheme.typography.bodySmall.copy(textDecoration = TextDecoration.LineThrough),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!product.isInStock) {
                    Text(
                        text = "Currently Out of Stock",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                } else if (product.stockQuantity != null) {
                    Text(
                        text = "${product.stockQuantity} in stock",
                        style = MaterialTheme.typography.labelSmall,
                        color = SageSuccess
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onAddToCart,
                    enabled = product.isInStock,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("add_cart_btn_${product.id}"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.AddShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (product.isInStock) "Add to Cart" else "Out of Stock",
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun RatingStars(
    rating: Double,
    modifier: Modifier = Modifier,
    starSize: Dp = 16.dp
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..5) {
            val filled = rating >= i - 0.25
            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = null,
                tint = if (filled) SubtleGold else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(starSize)
            )
        }
    }
}

@Composable
fun PriceBreakdownCard(
    breakdown: PriceBreakdown,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Transparent Price Breakdown",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            BreakdownRow("Product Subtotal", formatInr(breakdown.productSubtotal))

            if (breakdown.appliedOffer != null) {
                val offerDesc = if (breakdown.offerDiscount > 0) {
                    "-${formatInr(breakdown.offerDiscount)}"
                } else {
                    "Applied"
                }
                BreakdownRow(
                    label = breakdown.appliedOffer.title,
                    value = offerDesc,
                    valueColor = SageSuccess
                )
            }

            BreakdownRow(
                label = "Standard Delivery",
                value = if (breakdown.standardDeliveryCharge == 0) "FREE" else formatInr(breakdown.standardDeliveryCharge),
                valueColor = if (breakdown.standardDeliveryCharge == 0) SageSuccess else MaterialTheme.colorScheme.onSurface
            )

            if (breakdown.rushDeliveryCharge > 0) {
                BreakdownRow(
                    label = "Rush Delivery (Within 2 Hours)",
                    value = "+${formatInr(breakdown.rushDeliveryCharge)}",
                    valueColor = SubtleGold
                )
            }

            BreakdownRow(
                label = "Shopping Bag Charge (Once per order)",
                value = formatInr(breakdown.shoppingBagCharge)
            )

            if (breakdown.selectedFreeGift != null) {
                BreakdownRow(
                    label = "Free Gift: ${breakdown.selectedFreeGift.name}",
                    value = "FREE",
                    valueColor = SageSuccess
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Final Payable Amount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = formatInr(breakdown.finalPayableAmount),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocalShipping,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Estimated Delivery: ${breakdown.estimatedDeliveryText}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}

@Composable
fun OrderTimelineView(
    currentStatus: OrderStatus,
    history: List<OrderStatusStep>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (currentStatus == OrderStatus.CANCELLED) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Order Cancelled",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    history.lastOrNull { it.statusKey == OrderStatus.CANCELLED.key }?.let { step ->
                        Text(
                            text = "${formatDateTime(step.timestamp)} — ${step.note}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            return@Column
        }

        val steps = OrderStatus.progressionOrder
        steps.forEachIndexed { idx, status ->
            val reached = currentStatus.stepIndex >= status.stepIndex
            val matchingHistory = history.lastOrNull { it.statusKey == status.key }
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = if (reached) SageSuccess else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(22.dp)
                    )
                    if (idx < steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(26.dp)
                                .background(
                                    if (currentStatus.stepIndex > status.stepIndex) SageSuccess
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = status.label,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (reached) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (matchingHistory != null) {
                        Text(
                            text = "${formatDateTime(matchingHistory.timestamp)}${if (matchingHistory.note.isNotBlank()) " • ${matchingHistory.note}" else ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun ZoomableImageViewerDialog(
    imageSource: Any,
    title: String,
    onDismiss: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.94f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                ProductMediaItem(
                    imageSource = imageSource,
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 4f)
                                if (scale > 1f) {
                                    offsetX += pan.x
                                    offsetY += pan.y
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    if (scale > 1.2f) {
                                        scale = 1f
                                        offsetX = 0f
                                        offsetY = 0f
                                    } else {
                                        scale = 2.4f
                                    }
                                }
                            )
                        }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$title (Pinch or double-tap to zoom)",
                        style = MaterialTheme.typography.labelLarge,
                        color = PureWhite,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close zoom view",
                            tint = PureWhite
                        )
                    }
                }
            }
        }
    }
}
