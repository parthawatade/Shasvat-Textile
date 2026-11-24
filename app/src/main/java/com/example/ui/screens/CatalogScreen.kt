package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CatalogFilterState
import com.example.data.model.CategoryEntity
import com.example.data.model.DeliverySpeedFilter
import com.example.data.model.PriceBracket
import com.example.data.model.Product
import com.example.data.model.SortOption
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.ProductCard
import com.example.ui.components.resolveProductDrawable

@Composable
fun CategoriesScreen(
    categories: List<CategoryEntity>,
    products: List<Product>,
    onCategorySelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val countByCategory = remember(products) {
        products.groupingBy { it.categoryName }.eachCount()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Shop by Category",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "Browse authentic Solapuri textile categories directly from Shasvat Textiles.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            val cols = when {
                maxWidth >= 840.dp -> 3
                maxWidth >= 520.dp -> 2
                else -> 2
            }
            LazyVerticalGrid(
                columns = GridCells.Fixed(cols),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(categories, key = { it.id }) { cat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_tile_${cat.id}")
                            .clickable { onCategorySelect(cat.name) },
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
                                    .height(130.dp)
                            )
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(cat.name, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    text = "${countByCategory[cat.name] ?: 0} item(s)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = cat.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CatalogScreen(
    products: List<Product>,
    categories: List<CategoryEntity>,
    availableBrands: List<String>,
    filterState: CatalogFilterState,
    wishlistIds: Set<String>,
    onQueryChange: (String) -> Unit,
    onToggleCategory: (String) -> Unit,
    onTogglePriceBracket: (PriceBracket) -> Unit,
    onCustomPriceRange: (Int?, Int?) -> Unit,
    onToggleBrand: (String) -> Unit,
    onMinRating: (Int?) -> Unit,
    onDeliverySpeed: (DeliverySpeedFilter?) -> Unit,
    onSortOption: (SortOption) -> Unit,
    onClearAllFilters: () -> Unit,
    onProductClick: (String) -> Unit,
    onAddToCart: (String) -> Unit,
    onToggleWishlist: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = filterState.query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Search name, category, brand, keywords...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search products") },
                    trailingIcon = {
                        if (filterState.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("catalog_search_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("catalog_filters_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (filterState.activeFilterCount > 0) {
                                    Badge { Text("${filterState.activeFilterCount}") }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.FilterList, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Filters")
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        OutlinedButton(
                            onClick = { showSortMenu = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .testTag("catalog_sort_button")
                        ) {
                            Icon(Icons.Filled.Sort, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(filterState.sortOption.label, maxLines = 1)
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = {
                                        onSortOption(option)
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    if (filterState.activeFilterCount > 0 || filterState.query.isNotBlank()) {
                        TextButton(
                            onClick = onClearAllFilters,
                            modifier = Modifier.testTag("catalog_clear_filters_button")
                        ) {
                            Text("Clear")
                        }
                    }
                }

                // Quick Category Strip
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories, key = { it.id }) { cat ->
                        val selected = cat.name in filterState.selectedCategories
                        FilterChip(
                            selected = selected,
                            onClick = { onToggleCategory(cat.name) },
                            label = { Text(cat.name) }
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${products.size} product(s) found",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (filterState.activeFilterCount > 0) {
                Text(
                    text = "Clear All Filters",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onClearAllFilters)
                )
            }
        }

        if (products.isEmpty()) {
            Box(modifier = Modifier.padding(16.dp)) {
                EmptyStateCard(
                    title = "No matching products",
                    subtitle = "Try adjusting your search keywords or clearing active filters.",
                    actionLabel = "Clear All Filters",
                    onAction = onClearAllFilters
                )
            }
        } else {
            BoxWithConstraints(modifier = Modifier.weight(1f)) {
                val columns = when {
                    maxWidth >= 900.dp -> 4
                    maxWidth >= 600.dp -> 3
                    else -> 2
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        ProductCard(
                            product = product,
                            isWishlisted = product.id in wishlistIds,
                            onProductClick = { onProductClick(product.id) },
                            onAddToCart = { onAddToCart(product.id) },
                            onToggleWishlist = { onToggleWishlist(product.id) }
                        )
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        var minInput by remember(filterState.customMinPrice) {
            mutableStateOf(filterState.customMinPrice?.toString() ?: "")
        }
        var maxInput by remember(filterState.customMaxPrice) {
            mutableStateOf(filterState.customMaxPrice?.toString() ?: "")
        }

        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            sheetState = sheetState
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 28.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filter Products", style = MaterialTheme.typography.headlineSmall)
                        TextButton(onClick = {
                            minInput = ""
                            maxInput = ""
                            onClearAllFilters()
                        }) {
                            Text("Clear All Filters")
                        }
                    }
                }

                // Category
                item {
                    Text("CATEGORY", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = cat.name in filterState.selectedCategories,
                                onClick = { onToggleCategory(cat.name) },
                                label = { Text(cat.name) }
                            )
                        }
                    }
                }

                item { HorizontalDivider() }

                // Price Brackets + Custom Price Range
                item {
                    Text("PRICE", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PriceBracket.entries.forEach { bracket ->
                            FilterChip(
                                selected = bracket in filterState.selectedPriceBrackets,
                                onClick = { onTogglePriceBracket(bracket) },
                                label = { Text(bracket.label) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = minInput,
                            onValueChange = {
                                minInput = it.filter { c -> c.isDigit() }
                                onCustomPriceRange(minInput.toIntOrNull(), maxInput.toIntOrNull())
                            },
                            label = { Text("Min ₹") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = maxInput,
                            onValueChange = {
                                maxInput = it.filter { c -> c.isDigit() }
                                onCustomPriceRange(minInput.toIntOrNull(), maxInput.toIntOrNull())
                            },
                            label = { Text("Max ₹") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item { HorizontalDivider() }

                // Brand multi-select
                item {
                    Text("BRAND", style = MaterialTheme.typography.labelLarge)
                    availableBrands.forEach { brand ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onToggleBrand(brand) },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = brand in filterState.selectedBrands,
                                onCheckedChange = { onToggleBrand(brand) }
                            )
                            Text(brand, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                item { HorizontalDivider() }

                // Rating
                item {
                    Text("CUSTOMER RATING", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(4, 3, 2).forEach { stars ->
                            FilterChip(
                                selected = filterState.minRating == stars,
                                onClick = {
                                    onMinRating(if (filterState.minRating == stars) null else stars)
                                },
                                label = { Text("$stars★ & above") }
                            )
                        }
                    }
                }

                item { HorizontalDivider() }

                // Delivery Speed
                item {
                    Text("DELIVERY SPEED", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        DeliverySpeedFilter.entries.forEach { speed ->
                            FilterChip(
                                selected = filterState.deliverySpeed == speed,
                                onClick = {
                                    onDeliverySpeed(if (filterState.deliverySpeed == speed) null else speed)
                                },
                                label = { Text(speed.label) }
                            )
                        }
                    }
                }

                item {
                    Button(
                        onClick = { showFilterSheet = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("apply_filters_button")
                    ) {
                        Text("Show ${products.size} Product(s)")
                    }
                }
            }
        }
    }
}
