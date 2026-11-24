package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CategoryEntity
import com.example.data.model.OfferEntity
import com.example.data.model.Product
import com.example.data.model.ReviewEntity
import com.example.ui.components.ProductCard
import com.example.ui.components.RatingStars
import com.example.ui.components.ShasvatLogo
import com.example.ui.components.formatDateTime
import com.example.ui.components.resolveProductDrawable
import com.example.ui.theme.DarkEspresso
import com.example.ui.theme.DeepWalnut
import com.example.ui.theme.PaleGoldContainer
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SubtleGold
import com.example.ui.theme.WarmBeige

@Composable
fun HomeScreen(
    categories: List<CategoryEntity>,
    products: List<Product>,
    offers: List<OfferEntity>,
    reviews: List<ReviewEntity>,
    wishlistIds: Set<String>,
    cartCount: Int,
    unreadNotificationCount: Int,
    isAdmin: Boolean,
    onSearchSubmit: (String) -> Unit,
    onShopNow: () -> Unit,
    onCategoryClick: (String) -> Unit,
    onProductClick: (String) -> Unit,
    onAddToCart: (String) -> Unit,
    onToggleWishlist: (String) -> Unit,
    onOpenWishlist: () -> Unit,
    onOpenCart: () -> Unit,
    onOpenWholesale: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAdmin: () -> Unit,
    onSubscribeNewsletter: (String) -> Unit,
    onOpenAccount: () -> Unit,
    onOpenOrders: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchInput by remember { mutableStateOf("") }
    var newsletterEmail by remember { mutableStateOf("") }

    val trending = remember(products) { products.filter { it.isTrending }.ifEmpty { products } }
    val bestSellers = remember(products) { products.filter { it.isBestSeller }.ifEmpty { products } }
    val newArrivals = remember(products) { products.filter { it.isNewArrival }.ifEmpty { products } }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Professional Header with Logo, Wishlist, Cart, Notifications & Search Bar
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShasvatLogo(compact = false)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isAdmin) {
                                IconButton(
                                    onClick = onOpenAdmin,
                                    modifier = Modifier.testTag("home_admin_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.AdminPanelSettings,
                                        contentDescription = "Open Admin Dashboard",
                                        tint = SubtleGold
                                    )
                                }
                            }
                            IconButton(
                                onClick = onOpenNotifications,
                                modifier = Modifier.testTag("home_notifications_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadNotificationCount > 0) {
                                            Badge { Text("$unreadNotificationCount") }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Notifications,
                                        contentDescription = "Notifications"
                                    )
                                }
                            }
                            IconButton(
                                onClick = onOpenWishlist,
                                modifier = Modifier.testTag("home_wishlist_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (wishlistIds.isNotEmpty()) {
                                            Badge { Text("${wishlistIds.size}") }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = "Wishlist"
                                    )
                                }
                            }
                            IconButton(
                                onClick = onOpenCart,
                                modifier = Modifier.testTag("home_cart_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (cartCount > 0) {
                                            Badge { Text("$cartCount") }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ShoppingCart,
                                        contentDescription = "Shopping Cart"
                                    )
                                }
                            }
                        }
                    }

                    // 2. Search Bar
                    OutlinedTextField(
                        value = searchInput,
                        onValueChange = { searchInput = it },
                        placeholder = {
                            Text(
                                text = stringResource(R.string.search_hint),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            Button(
                                onClick = { onSearchSubmit(searchInput) },
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .testTag("home_search_submit"),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("Search", style = MaterialTheme.typography.labelMedium)
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input")
                    )
                }
            }
        }

        // 3. Hero Banner with Textile Visuals, Official Marathi Tagline, Description & Shop Now CTA
        item {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                val isWide = maxWidth >= 680.dp
                Card(
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = CardDefaults.cardColors(containerColor = DeepWalnut),
                    border = BorderStroke(1.dp, SubtleGold.copy(alpha = 0.5f))
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isWide) 280.dp else 210.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_hero_solapur_loom),
                                contentDescription = "Authentic Solapuri textiles",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                DeepWalnut.copy(alpha = 0.25f),
                                                DeepWalnut.copy(alpha = 0.92f)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    color = SubtleGold,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Text(
                                        text = "OFFICIAL HERITAGE STORE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = DeepWalnut,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Text(
                                    text = stringResource(R.string.brand_tagline_marathi),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = PureWhite,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.brand_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = WarmBeige
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = onShopNow,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("home_shop_now_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = SubtleGold,
                                        contentColor = DeepWalnut
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.shop_now),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                OutlinedButton(
                                    onClick = onOpenWholesale,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("home_wholesale_button"),
                                    border = BorderStroke(1.dp, SubtleGold),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmBeige)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Business,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Bulk / Wholesale", maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Featured Categories (All 6 Separate Categories)
        item {
            SectionHeader(
                title = "Featured Categories",
                actionLabel = "View All",
                onAction = onShopNow
            )
            Spacer(modifier = Modifier.height(10.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories, key = { it.id }) { cat ->
                    Card(
                        modifier = Modifier
                            .width(156.dp)
                            .testTag("home_category_${cat.id}")
                            .clickable { onCategoryClick(cat.name) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column {
                            Image(
                                painter = painterResource(id = resolveProductDrawable(cat.drawableName)),
                                contentDescription = cat.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(104.dp)
                            )
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Explore",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SubtleGold
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Special Offers Banner Strip
        if (offers.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Special Offers & Savings",
                    actionLabel = "Shop & Save",
                    onAction = onShopNow
                )
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(offers.filter { it.isActive }, key = { it.id }) { offer ->
                        Card(
                            modifier = Modifier
                                .width(260.dp)
                                .clickable(onClick = onShopNow),
                            colors = CardDefaults.cardColors(containerColor = PaleGoldContainer),
                            border = BorderStroke(1.dp, SubtleGold)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.LocalOffer,
                                        contentDescription = null,
                                        tint = DarkEspresso,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = offer.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = DeepWalnut,
                                        maxLines = 2
                                    )
                                }
                                Text(
                                    text = offer.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkEspresso,
                                    maxLines = 3
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Trending Products
        item {
            ProductHorizontalSection(
                title = "Trending Products",
                products = trending,
                wishlistIds = wishlistIds,
                onSeeAll = onShopNow,
                onProductClick = onProductClick,
                onAddToCart = onAddToCart,
                onToggleWishlist = onToggleWishlist
            )
        }

        // 7. Best Sellers
        item {
            ProductHorizontalSection(
                title = "Best Sellers",
                products = bestSellers,
                wishlistIds = wishlistIds,
                onSeeAll = onShopNow,
                onProductClick = onProductClick,
                onAddToCart = onAddToCart,
                onToggleWishlist = onToggleWishlist
            )
        }

        // 8. New Arrivals
        item {
            ProductHorizontalSection(
                title = "New Arrivals",
                products = newArrivals,
                wishlistIds = wishlistIds,
                onSeeAll = onShopNow,
                onProductClick = onProductClick,
                onAddToCart = onAddToCart,
                onToggleWishlist = onToggleWishlist
            )
        }

        // 9. Why Choose Shasvat Textiles
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Why Choose Shasvat Textiles",
                    style = MaterialTheme.typography.headlineSmall
                )
                val pillars = listOf(
                    "Authentic Solapuri Loom Craft" to "Direct sourcing from trusted Solapur weaving traditions for genuine chaddars, towels, rugs, and cotton goods.",
                    "Transparent Retail & Bulk Pricing" to "Clear pricing with automatic offer evaluation, single ₹20 bag charge per order, and dedicated wholesale quotes.",
                    "Fast Delivery & Live Tracking" to "Standard delivery in 24h–2 days (free above ₹2,000) plus 2-hour Rush Delivery for eligible PIN codes.",
                    "GST Tax Invoices & Secure Checkout" to "Instant itemized GST invoices and secure COD or gateway-verified UPI/card checkout."
                )
                pillars.forEach { (title, desc) ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = SubtleGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 10. Customer Reviews Section (Real DB reviews only, never fake reviews)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Customer Reviews",
                    style = MaterialTheme.typography.headlineSmall
                )
                if (reviews.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "No customer reviews yet",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Verified customer ratings and reviews will appear here as orders are reviewed. Open any product to submit your review.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    reviews.take(4).forEach { rev ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
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
                                Text(
                                    text = "${rev.productName} — ${rev.title}",
                                    style = MaterialTheme.typography.labelLarge
                                )
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

        // 11. Newsletter Subscription
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Subscribe for New Arrivals & Offers",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Receive updates on new Solapuri weaves, seasonal discounts, and wholesale rate alerts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = newsletterEmail,
                        onValueChange = { newsletterEmail = it },
                        label = { Text("Email address") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("newsletter_email_input")
                    )
                    Button(
                        onClick = {
                            onSubscribeNewsletter(newsletterEmail)
                            if (newsletterEmail.contains("@")) newsletterEmail = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("newsletter_submit_button")
                    ) {
                        Text("Subscribe to Updates")
                    }
                }
            }
        }

        // 12. Professional Footer (Strictly NO physical shop addresses or store locations)
        item {
            Surface(
                color = DeepWalnut,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShasvatLogo(compact = true, darkBackground = true)
                    Text(
                        text = stringResource(R.string.brand_tagline_marathi),
                        style = MaterialTheme.typography.bodySmall,
                        color = SubtleGold,
                        fontStyle = FontStyle.Italic
                    )
                    HorizontalDivider(color = PureWhite.copy(alpha = 0.15f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Shopping", style = MaterialTheme.typography.titleSmall, color = PureWhite)
                            FooterLink("All Products", onShopNow)
                            FooterLink("Wishlist", onOpenWishlist)
                            FooterLink("Shopping Cart", onOpenCart)
                            FooterLink("Wholesale / Bulk Orders", onOpenWholesale)
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Customer Care", style = MaterialTheme.typography.titleSmall, color = PureWhite)
                            FooterLink("My Account", onOpenAccount)
                            FooterLink("Track My Orders", onOpenOrders)
                            FooterLink("Notifications", onOpenNotifications)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ReceiptLong,
                                    contentDescription = null,
                                    tint = SubtleGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "GST Invoices Supported",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = WarmBeige
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = PureWhite.copy(alpha = 0.15f))
                    Text(
                        text = "© 2026 SHASVAT TEXTILES • Authentic Solapuri Textiles Online Storefront",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmBeige.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun FooterLink(label: String, onClick: () -> Unit) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        color = WarmBeige,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp)
    )
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onAction)
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ProductHorizontalSection(
    title: String,
    products: List<Product>,
    wishlistIds: Set<String>,
    onSeeAll: () -> Unit,
    onProductClick: (String) -> Unit,
    onAddToCart: (String) -> Unit,
    onToggleWishlist: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = title, actionLabel = "See All", onAction = onSeeAll)
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products, key = { "${title}_${it.id}" }) { product ->
                ProductCard(
                    product = product,
                    isWishlisted = product.id in wishlistIds,
                    onProductClick = { onProductClick(product.id) },
                    onAddToCart = { onAddToCart(product.id) },
                    onToggleWishlist = { onToggleWishlist(product.id) },
                    modifier = Modifier.width(210.dp)
                )
            }
        }
    }
}
