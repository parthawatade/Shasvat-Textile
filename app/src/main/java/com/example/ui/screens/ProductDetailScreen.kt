package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.model.Product
import com.example.data.model.ReviewEntity
import com.example.ui.components.ProductCard
import com.example.ui.components.ProductMediaItem
import com.example.ui.components.RatingStars
import com.example.ui.components.ZoomableImageViewerDialog
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatInr
import com.example.ui.theme.PaleGoldContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SageContainer
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SubtleGold
import com.example.ui.theme.TerracottaRed
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductDetailScreen(
    product: Product?,
    relatedProducts: List<Product>,
    reviews: List<ReviewEntity>,
    isWishlisted: Boolean,
    wishlistIds: Set<String>,
    checkedPinCode: String,
    isPinRushEligible: Boolean,
    isSignedIn: Boolean,
    onBack: () -> Unit,
    onAddToCart: (quantity: Int, color: String, size: String) -> Unit,
    onBuyNow: (quantity: Int, color: String, size: String) -> Unit,
    onToggleWishlist: (String) -> Unit,
    onCheckPinCode: (String) -> Unit,
    onSubmitReview: (rating: Int, title: String, comment: String, onDone: (Boolean, String) -> Unit) -> Unit,
    onRelatedProductClick: (String) -> Unit,
    onOpenAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (product == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Product not found")
        }
        return
    }

    val mediaItems: List<Any> = remember(product) {
        (product.uploadedImageUris + product.imageDrawables).ifEmpty { listOf("img_cat_chadar") }
    }
    val pagerState = rememberPagerState(pageCount = { mediaItems.size })

    var selectedColor by remember(product.id, product.colors) {
        mutableStateOf(product.colors.firstOrNull() ?: "")
    }
    var selectedSize by remember(product.id, product.sizes) {
        mutableStateOf(product.sizes.firstOrNull() ?: "")
    }
    var quantity by remember(product.id) { mutableIntStateOf(1) }
    var pinInput by remember(checkedPinCode) { mutableStateOf(checkedPinCode) }
    var zoomImageSource by remember { mutableStateOf<Any?>(null) }
    var showReviewDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(product.name, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("product_detail_back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onToggleWishlist(product.id) },
                        modifier = Modifier.testTag("product_detail_wishlist")
                    ) {
                        Icon(
                            imageVector = if (isWishlisted) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Wishlist",
                            tint = if (isWishlisted) TerracottaRed else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            // Sticky Add to Cart & Buy Now bar
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onAddToCart(quantity, selectedColor, selectedSize) },
                        enabled = product.isInStock,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_add_to_cart_button")
                    ) {
                        Icon(Icons.Filled.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add to Cart")
                    }
                    Button(
                        onClick = { onBuyNow(quantity, selectedColor, selectedSize) },
                        enabled = product.isInStock,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_buy_now_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SubtleGold,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(Icons.Filled.FlashOn, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Buy Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Multiple Product Images + Zoom trigger
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val source = mediaItems[page]
                        ProductMediaItem(
                            imageSource = source,
                            contentDescription = "${product.name} image ${page + 1}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { zoomImageSource = source }
                        )
                    }

                    Surface(
                        color = PureWhite.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .clickable { zoomImageSource = mediaItems[pagerState.currentPage] }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.ZoomIn, contentDescription = "Zoom image", modifier = Modifier.size(16.dp))
                            Text("Tap to Zoom", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    if (mediaItems.size > 1) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            repeat(mediaItems.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(if (pagerState.currentPage == index) 10.dp else 7.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (pagerState.currentPage == index) SubtleGold
                                            else PureWhite.copy(alpha = 0.6f)
                                        )
                                )
                            }
                        }
                    }
                }
            }

            // 2. Title, Brand, Price, Stock, Rating
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = PaleGoldContainer,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = product.categoryName,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            text = "by ${product.brand}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.headlineMedium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = formatInr(product.price),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        if (product.originalPrice != null && product.originalPrice > product.price) {
                            Text(
                                text = formatInr(product.originalPrice),
                                style = MaterialTheme.typography.bodyLarge.copy(textDecoration = TextDecoration.LineThrough),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            product.discountPercent?.let { pct ->
                                Text(
                                    text = "$pct% OFF",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TerracottaRed
                                )
                            }
                        }
                    }

                    if (product.reviewCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RatingStars(rating = product.averageRating)
                            Text(
                                text = "${String.format(Locale.US, "%.1f", product.averageRating)} (${product.reviewCount} review(s))",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    } else {
                        Text(
                            text = "No ratings yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Stock Availability
                    Surface(
                        color = if (product.isInStock) SageContainer else MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        val stockLabel = when {
                            !product.isAvailable -> "Unavailable"
                            product.stockQuantity == 0 -> "Out of Stock"
                            product.stockQuantity != null -> "In Stock (${product.stockQuantity} units available)"
                            else -> "Available to Order (Stock count managed by Admin)"
                        }
                        Text(
                            text = stockLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (product.isInStock) SageSuccess else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 3. Colour & Size Variations (if configured by Admin)
            if (product.colors.isNotEmpty() || product.sizes.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (product.colors.isNotEmpty()) {
                            Text("Available Colours", style = MaterialTheme.typography.titleSmall)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                product.colors.forEach { color ->
                                    FilterChip(
                                        selected = selectedColor == color,
                                        onClick = { selectedColor = color },
                                        label = { Text(color) }
                                    )
                                }
                            }
                        }
                        if (product.sizes.isNotEmpty()) {
                            Text("Available Sizes", style = MaterialTheme.typography.titleSmall)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                product.sizes.forEach { size ->
                                    FilterChip(
                                        selected = selectedSize == size,
                                        onClick = { selectedSize = size },
                                        label = { Text(size) }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Quantity Selector
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quantity", style = MaterialTheme.typography.titleSmall)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.size(44.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease quantity")
                        }
                        Text(
                            text = "$quantity",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        OutlinedButton(
                            onClick = {
                                val max = product.stockQuantity ?: 99
                                if (quantity < max) quantity++
                            },
                            modifier = Modifier.size(44.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase quantity")
                        }
                    }
                }
            }

            // 5. Delivery Estimate & PIN Code Checker
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text("Delivery Estimate", style = MaterialTheme.typography.titleSmall)
                        }
                        Text(
                            text = "Standard Delivery: ${product.deliveryEstimate} (₹50; FREE above ₹2,000)",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = { pinInput = it.filter { c -> c.isDigit() }.take(6) },
                                label = { Text("Check Delivery PIN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("detail_pin_input")
                            )
                            Button(
                                onClick = { onCheckPinCode(pinInput) },
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("detail_pin_check_button")
                            ) {
                                Text("Check")
                            }
                        }
                        if (checkedPinCode.length == 6) {
                            Text(
                                text = if (isPinRushEligible && product.isRushEligible) {
                                    "PIN $checkedPinCode: Rush Delivery (Within 2 hours, +₹250) is AVAILABLE."
                                } else {
                                    "PIN $checkedPinCode: Standard Delivery available. Rush Delivery not available for this PIN."
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isPinRushEligible) SageSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 6. Product Description, Fabric Details & Specifications
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Product Description", style = MaterialTheme.typography.titleMedium)
                        Text(product.description, style = MaterialTheme.typography.bodyMedium)

                        HorizontalDivider()

                        Text("Fabric Information", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = product.fabricDetails.ifBlank { "Fabric details have not been configured yet (editable via Admin Dashboard)." },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text("Product Specifications", style = MaterialTheme.typography.titleSmall)
                        Text(
                            text = product.specifications.ifBlank { "Specifications have not been configured yet (editable via Admin Dashboard)." },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 7. Customer Reviews
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Customer Reviews (${reviews.size})", style = MaterialTheme.typography.titleLarge)
                        OutlinedButton(
                            onClick = {
                                if (isSignedIn) showReviewDialog = true else onOpenAccount()
                            },
                            modifier = Modifier.testTag("write_review_button")
                        ) {
                            Icon(Icons.Filled.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isSignedIn) "Write Review" else "Sign in to Review")
                        }
                    }

                    if (reviews.isEmpty()) {
                        Text(
                            text = "No reviews yet for ${product.name}. Customers who purchase this product receive a Verified Buyer badge on their review.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        reviews.forEach { rev ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(rev.reviewerName, style = MaterialTheme.typography.titleSmall)
                                        if (rev.isVerifiedBuyer) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.CheckCircle,
                                                    contentDescription = null,
                                                    tint = SageSuccess,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Text(
                                                    text = "Verified Buyer",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = SageSuccess
                                                )
                                            }
                                        }
                                    }
                                    RatingStars(rating = rev.rating.toDouble())
                                    Text(rev.title, style = MaterialTheme.typography.labelLarge)
                                    Text(rev.comment, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = formatDateTime(rev.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 8. Related Products
            if (relatedProducts.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Related Products",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(relatedProducts, key = { it.id }) { rel ->
                                ProductCard(
                                    product = rel,
                                    isWishlisted = rel.id in wishlistIds,
                                    onProductClick = { onRelatedProductClick(rel.id) },
                                    onAddToCart = { onAddToCart(1, "", "") },
                                    onToggleWishlist = { onToggleWishlist(rel.id) },
                                    modifier = Modifier.width(196.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    zoomImageSource?.let { src ->
        ZoomableImageViewerDialog(
            imageSource = src,
            title = product.name,
            onDismiss = { zoomImageSource = null }
        )
    }

    if (showReviewDialog) {
        var rating by remember { mutableIntStateOf(5) }
        var title by remember { mutableStateOf("") }
        var comment by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = { Text("Review ${product.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (i in 1..5) {
                            IconButton(onClick = { rating = i }) {
                                Icon(
                                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                    contentDescription = "$i stars",
                                    tint = SubtleGold
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Headline") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        label = { Text("Your review") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    error?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSubmitReview(rating, title, comment) { ok, msg ->
                        if (ok) showReviewDialog = false else error = msg
                    }
                }) {
                    Text("Submit Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) { Text("Cancel") }
            }
        )
    }
}
