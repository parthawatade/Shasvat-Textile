package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCartCheckout
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CartItemDetail
import com.example.data.model.DeliveryMethod
import com.example.data.model.Product
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.PriceBreakdownCard
import com.example.ui.components.ProductCard
import com.example.ui.components.ProductThumbnail
import com.example.ui.components.formatInr
import com.example.ui.theme.PaleGoldContainer
import com.example.ui.theme.SageContainer
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SubtleGold
import com.example.viewmodel.CartUiState

@Composable
fun CartScreen(
    cartState: CartUiState,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onSaveForLater: (String) -> Unit,
    onMoveToCart: (String) -> Unit,
    onSelectOffer: (String?) -> Unit,
    onSelectFreeGift: (String) -> Unit,
    onCheckPinCode: (String) -> Unit,
    onSelectDeliveryMethod: (DeliveryMethod) -> Unit,
    onProductClick: (String) -> Unit,
    onBrowseProducts: () -> Unit,
    onProceedToCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var pinInput by remember(cartState.checkedPinCode) { mutableStateOf(cartState.checkedPinCode) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (cartState.activeItems.isEmpty() && cartState.savedForLaterItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    title = stringResource(R.string.empty_cart_title),
                    subtitle = stringResource(R.string.empty_cart_subtitle),
                    actionLabel = "Browse Solapuri Textiles",
                    onAction = onBrowseProducts
                )
            }
            return
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Shopping Cart (${cartState.totalActiveCount})",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            if (cartState.activeItems.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No items in active cart",
                        subtitle = "Move saved items back to your cart below or browse more textiles.",
                        actionLabel = "Browse Products",
                        onAction = onBrowseProducts
                    )
                }
            } else {
                items(cartState.activeItems, key = { it.cartItem.id }) { detail ->
                    CartItemRow(
                        detail = detail,
                        isSavedForLater = false,
                        onProductClick = { onProductClick(detail.product.id) },
                        onUpdateQuantity = { qty -> onUpdateQuantity(detail.cartItem.id, qty) },
                        onSecondaryAction = { onSaveForLater(detail.cartItem.id) },
                        onRemove = { onRemoveItem(detail.cartItem.id) }
                    )
                }

                // Delivery PIN Code & Speed Selection
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.LocalShipping, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text("Delivery PIN Code & Speed", style = MaterialTheme.typography.titleMedium)
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = pinInput,
                                    onValueChange = { pinInput = it.filter { c -> c.isDigit() }.take(6) },
                                    label = { Text("6-digit Delivery PIN Code") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("cart_pin_input")
                                )
                                Button(
                                    onClick = { onCheckPinCode(pinInput) },
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("cart_pin_check_button")
                                ) {
                                    Text("Check")
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectDeliveryMethod(DeliveryMethod.STANDARD) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = cartState.selectedDeliveryMethod == DeliveryMethod.STANDARD,
                                    onClick = { onSelectDeliveryMethod(DeliveryMethod.STANDARD) }
                                )
                                Column {
                                    Text(
                                        text = "Standard Delivery (${cartState.deliverySettings.standardEstimateText})",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    val stdNote = if (cartState.priceBreakdown.isFreeStandardDeliveryApplied) {
                                        "FREE on this order"
                                    } else {
                                        "${formatInr(cartState.deliverySettings.standardCharge)} (Free above ${formatInr(cartState.deliverySettings.freeDeliveryMinSubtotal)})"
                                    }
                                    Text(stdNote, style = MaterialTheme.typography.bodySmall, color = SageSuccess)
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = cartState.isPinRushEligible) {
                                        onSelectDeliveryMethod(DeliveryMethod.RUSH)
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = cartState.selectedDeliveryMethod == DeliveryMethod.RUSH,
                                    onClick = { onSelectDeliveryMethod(DeliveryMethod.RUSH) },
                                    enabled = cartState.isPinRushEligible
                                )
                                Column {
                                    Text(
                                        text = "Rush Delivery (${cartState.deliverySettings.rushEstimateText})",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (cartState.isPinRushEligible) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val rushDesc = when {
                                        cartState.checkedPinCode.length != 6 ->
                                            "Enter your 6-digit PIN code above to check Rush Delivery eligibility (+${formatInr(cartState.deliverySettings.rushExtraCharge)})."
                                        cartState.isPinRushEligible ->
                                            "Eligible for PIN ${cartState.checkedPinCode}! Additional ${formatInr(cartState.deliverySettings.rushExtraCharge)} charge."
                                        else ->
                                            "Not available for PIN ${cartState.checkedPinCode}."
                                    }
                                    Text(
                                        text = rushDesc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (cartState.isPinRushEligible) SubtleGold else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Offers & Discounts Selector
                item {
                    Card(
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
                                Icon(Icons.Filled.LocalOffer, contentDescription = null, tint = SubtleGold)
                                Text("Available Offers (Non-Stackable)", style = MaterialTheme.typography.titleMedium)
                            }
                            Text(
                                text = "Highest eligible offer is selected automatically. Tap any eligible offer to switch.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            cartState.offerEvaluations.forEach { eval ->
                                val isApplied = cartState.priceBreakdown.appliedOffer?.id == eval.offer.id
                                Surface(
                                    color = when {
                                        isApplied -> PaleGoldContainer
                                        eval.isEligible -> SageContainer.copy(alpha = 0.45f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                    },
                                    shape = MaterialTheme.shapes.small,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isApplied) SubtleGold else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = eval.isEligible) {
                                            onSelectOffer(eval.offer.id)
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        RadioButton(
                                            selected = isApplied,
                                            onClick = { onSelectOffer(eval.offer.id) },
                                            enabled = eval.isEligible
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(eval.offer.title, style = MaterialTheme.typography.titleSmall)
                                            Text(eval.offer.description, style = MaterialTheme.typography.bodySmall)
                                            Text(
                                                text = if (eval.isEligible) {
                                                    if (isApplied) "Applied" else "Eligible — Tap to apply"
                                                } else {
                                                    "Add ${formatInr(eval.amountShort)} more to unlock"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (eval.isEligible) SageSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Offer 5 Free Gift Picker (< ₹800)
                if (cartState.priceBreakdown.isFreeGiftEligible && cartState.eligibleFreeGifts.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = PaleGoldContainer),
                            border = BorderStroke(1.dp, SubtleGold)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.CardGiftcard, contentDescription = null)
                                    Text("Select Your Free Gift (Under ₹800)", style = MaterialTheme.typography.titleMedium)
                                }
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(cartState.eligibleFreeGifts, key = { it.id }) { gift ->
                                        FilterChip(
                                            selected = cartState.selectedFreeGift?.id == gift.id,
                                            onClick = { onSelectFreeGift(gift.id) },
                                            label = { Text("${gift.name} (${formatInr(gift.price)})") }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Transparent Price Breakdown
                item {
                    PriceBreakdownCard(breakdown = cartState.priceBreakdown)
                }
            }

            // Saved for Later Section
            if (cartState.savedForLaterItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Saved for Later (${cartState.savedForLaterItems.size})",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                items(cartState.savedForLaterItems, key = { "saved_${it.cartItem.id}" }) { detail ->
                    CartItemRow(
                        detail = detail,
                        isSavedForLater = true,
                        onProductClick = { onProductClick(detail.product.id) },
                        onUpdateQuantity = {},
                        onSecondaryAction = { onMoveToCart(detail.cartItem.id) },
                        onRemove = { onRemoveItem(detail.cartItem.id) }
                    )
                }
            }
        }

        if (cartState.activeItems.isNotEmpty()) {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Payable", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = formatInr(cartState.priceBreakdown.finalPayableAmount),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onProceedToCheckout,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("proceed_to_checkout_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SubtleGold,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        )
                    ) {
                        Icon(Icons.Filled.ShoppingCartCheckout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Checkout", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemRow(
    detail: CartItemDetail,
    isSavedForLater: Boolean,
    onProductClick: () -> Unit,
    onUpdateQuantity: (Int) -> Unit,
    onSecondaryAction: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProductThumbnail(
                    product = detail.product,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onProductClick)
                )
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = detail.product.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.clickable(onClick = onProductClick)
                    )
                    Text(
                        text = detail.product.categoryName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val variant = listOfNotNull(
                        detail.cartItem.selectedColor.takeIf { it.isNotBlank() },
                        detail.cartItem.selectedSize.takeIf { it.isNotBlank() }
                    ).joinToString(" • ")
                    if (variant.isNotBlank()) {
                        Text(variant, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(
                        text = "${formatInr(detail.product.price)} × ${detail.cartItem.quantity} = ${formatInr(detail.lineTotal)}",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!isSavedForLater) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { onUpdateQuantity(detail.cartItem.quantity - 1) },
                            modifier = Modifier.size(38.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                        }
                        Text(
                            text = "${detail.cartItem.quantity}",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        OutlinedButton(
                            onClick = { onUpdateQuantity(detail.cartItem.quantity + 1) },
                            modifier = Modifier.size(38.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase")
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onSecondaryAction) {
                        Icon(Icons.Filled.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isSavedForLater) "Move to Cart" else "Save for Later")
                    }
                    IconButton(onClick = onRemove) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Remove item",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistScreen(
    wishlistProducts: List<Product>,
    onProductClick: (String) -> Unit,
    onMoveToCart: (String) -> Unit,
    onRemoveFromWishlist: (String) -> Unit,
    onBrowseProducts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "My Wishlist (${wishlistProducts.size})",
            style = MaterialTheme.typography.headlineMedium
        )

        if (wishlistProducts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    title = stringResource(R.string.empty_wishlist_title),
                    subtitle = stringResource(R.string.empty_wishlist_subtitle),
                    actionLabel = "Explore Collection",
                    onAction = onBrowseProducts
                )
            }
        } else {
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val cols = if (maxWidth >= 600.dp) 3 else 2
                LazyVerticalGrid(
                    columns = GridCells.Fixed(cols),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(wishlistProducts, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            isWishlisted = true,
                            onProductClick = { onProductClick(product.id) },
                            onAddToCart = { onMoveToCart(product.id) },
                            onToggleWishlist = { onRemoveFromWishlist(product.id) }
                        )
                    }
                }
            }
        }
    }
}
