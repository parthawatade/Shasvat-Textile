package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.BusinessSettingsEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.DeliverySettingsEntity
import com.example.data.model.EligiblePinCodeEntity
import com.example.data.model.NewsletterEntity
import com.example.data.model.OfferEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.Product
import com.example.data.model.ProductEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.WholesaleEnquiryEntity
import com.example.data.model.WholesaleStatus
import com.example.data.network.GatewayConfigStatus
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatInr
import com.example.ui.theme.DarkEspresso
import com.example.ui.theme.PaleGoldContainer
import com.example.ui.theme.SageContainer
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SubtleGold
import com.example.viewmodel.AdminReportsSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    activeUser: UserEntity?,
    reports: AdminReportsSummary,
    products: List<Product>,
    categories: List<CategoryEntity>,
    orders: List<OrderEntity>,
    orderSearchQuery: String,
    users: List<UserEntity>,
    deliverySettings: DeliverySettingsEntity,
    eligiblePinCodes: List<EligiblePinCodeEntity>,
    offers: List<OfferEntity>,
    wholesaleEnquiries: List<WholesaleEnquiryEntity>,
    businessSettings: BusinessSettingsEntity,
    newsletterSubscribers: List<NewsletterEntity>,
    gatewayConfig: GatewayConfigStatus,
    onBack: () -> Unit,
    onOrderSearchChange: (String) -> Unit,
    onImportImages: suspend (List<Uri>) -> List<String>,
    onSaveProduct: (ProductEntity) -> Unit,
    onDeleteProduct: (String) -> Unit,
    onSaveCategory: (CategoryEntity) -> Unit,
    onUpdateOrderStatus: (String, OrderStatus, String, PaymentStatus?) -> Unit,
    onOpenOrderInvoice: (String) -> Unit,
    onToggleUserActive: (String, Boolean) -> Unit,
    onSaveDeliverySettings: (DeliverySettingsEntity) -> Unit,
    onSavePinCode: (String, String, Boolean) -> Unit,
    onDeletePinCode: (String) -> Unit,
    onSaveOffer: (OfferEntity) -> Unit,
    onDeleteOffer: (String) -> Unit,
    onUpdateWholesale: (String, Int?, WholesaleStatus, String) -> Unit,
    onSaveBusinessSettings: (BusinessSettingsEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    // Role-based access guard: customers can NEVER access Admin functionality
    if (activeUser == null || activeUser.role != UserRole.ADMIN.key) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Restricted Access") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    title = "Admin Role Required",
                    subtitle = "Customer accounts cannot access Shasvat Textiles store administration tools."
                )
            }
        }
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Reports", "Products", "Orders", "Customers", "Delivery", "Offers", "Wholesale", "GST & Setup")

    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showAddProduct by remember { mutableStateOf(false) }
    var editingOrder by remember { mutableStateOf<OrderEntity?>(null) }
    var editingOffer by remember { mutableStateOf<OfferEntity?>(null) }
    var showAddOffer by remember { mutableStateOf(false) }
    var editingWholesale by remember { mutableStateOf<WholesaleEnquiryEntity?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 12.dp) {
                tabs.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label) },
                        modifier = Modifier.testTag("admin_tab_$label")
                    )
                }
            }

            when (selectedTab) {
                // 0: REPORTS
                0 -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text("Store Performance & Reports", style = MaterialTheme.typography.headlineSmall)
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricTile("Total Sales", formatInr(reports.totalSalesAmount), Modifier.weight(1f))
                            MetricTile("Total Orders", "${reports.totalOrders}", Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricTile("Pending", "${reports.pendingOrders}", Modifier.weight(1f))
                            MetricTile("Delivered", "${reports.completedOrders}", Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricTile("COD Orders", "${reports.codOrders}", Modifier.weight(1f))
                            MetricTile("Online Orders", "${reports.onlineOrders}", Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricTile("Wholesale Inquiries", "${reports.totalWholesaleEnquiries}", Modifier.weight(1f))
                            MetricTile("Newsletter", "${reports.newsletterCount}", Modifier.weight(1f))
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Best-Selling Products", style = MaterialTheme.typography.titleMedium)
                                if (reports.bestSellingProducts.isEmpty()) {
                                    Text("No order sales recorded yet.", style = MaterialTheme.typography.bodySmall)
                                } else {
                                    val maxUnits = reports.bestSellingProducts.maxOf { it.second }.coerceAtLeast(1)
                                    reports.bestSellingProducts.forEach { (prodName, units) ->
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text(prodName, style = MaterialTheme.typography.bodySmall)
                                                Text("$units unit(s)", style = MaterialTheme.typography.labelMedium)
                                            }
                                            Canvas(modifier = Modifier.fillMaxWidth().height(10.dp)) {
                                                drawRoundRect(
                                                    color = PaleGoldContainer,
                                                    size = size,
                                                    cornerRadius = CornerRadius(8f, 8f)
                                                )
                                                drawRoundRect(
                                                    color = DarkEspresso,
                                                    size = Size(size.width * (units.toFloat() / maxUnits), size.height),
                                                    cornerRadius = CornerRadius(8f, 8f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("Low-Stock Alerts (≤ 5 units)", style = MaterialTheme.typography.titleMedium)
                                if (reports.lowStockProducts.isEmpty()) {
                                    Text(
                                        text = "No low-stock alerts. Edit products to configure stock quantities.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    reports.lowStockProducts.forEach { p ->
                                        Text(
                                            text = "${p.name}: ${p.stockQuantity} unit(s) remaining",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 1: PRODUCTS & CATEGORIES
                1 -> {
                    var newCatName by remember { mutableStateOf("") }
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Button(
                                onClick = { showAddProduct = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("admin_add_product_button")
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add New Product")
                            }
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                OutlinedTextField(
                                    value = newCatName,
                                    onValueChange = { newCatName = it },
                                    label = { Text("New Category Name") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    enabled = newCatName.isNotBlank(),
                                    onClick = {
                                        onSaveCategory(
                                            CategoryEntity(
                                                id = "",
                                                name = newCatName.trim(),
                                                description = "Solapuri textile collection",
                                                drawableName = "img_cat_chadar",
                                                sortOrder = categories.size + 1
                                            )
                                        )
                                        newCatName = ""
                                    },
                                    modifier = Modifier.height(52.dp)
                                ) { Text("Add Cat") }
                            }
                        }

                        items(products, key = { it.id }) { prod ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(prod.name, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "${prod.categoryName} • Retail ${formatInr(prod.price)}${prod.wholesalePrice?.let { " • Wholesale ${formatInr(it)}" } ?: ""}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "Stock: ${prod.stockQuantity?.toString() ?: "Not set"} • Photos: ${prod.uploadedImageUris.size} uploaded",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row {
                                        IconButton(onClick = { editingProduct = prod }) {
                                            Icon(Icons.Filled.Edit, contentDescription = "Edit ${prod.name}")
                                        }
                                        IconButton(onClick = { onDeleteProduct(prod.id) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete ${prod.name}", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 2: ORDERS
                2 -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = orderSearchQuery,
                            onValueChange = onOrderSearchChange,
                            label = { Text("Search orders by ID, customer, mobile, status") },
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (orders.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "No Matching Orders",
                                subtitle = "Customer orders will appear here for confirmation, tracking updates, and GST invoice access."
                            )
                        }
                    } else {
                        items(orders, key = { it.id }) { order ->
                            val status = OrderStatus.fromKey(order.orderStatus)
                            val payStatus = PaymentStatus.fromKey(order.paymentStatus)
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(order.orderNumber, style = MaterialTheme.typography.titleMedium)
                                        Text(formatInr(order.finalPayableAmount), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text("${order.customerName} • ${order.customerMobile}", style = MaterialTheme.typography.bodySmall)
                                    Text("Status: ${status.label} | Payment: ${payStatus.label}", style = MaterialTheme.typography.labelMedium)
                                    Text(formatDateTime(order.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { editingOrder = order },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("admin_manage_order_${order.id}")
                                        ) {
                                            Text("Update Status")
                                        }
                                        OutlinedButton(
                                            onClick = { onOpenOrderInvoice(order.id) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Invoice")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 3: CUSTOMERS
                3 -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(users, key = { it.id }) { usr ->
                        val userOrderCount = orders.count { it.userId == usr.id }
                        val userSpend = orders.filter { it.userId == usr.id && it.orderStatus != OrderStatus.CANCELLED.key }
                            .sumOf { it.finalPayableAmount }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("${usr.fullName} (${usr.role})", style = MaterialTheme.typography.titleSmall)
                                    Text("${usr.email} • ${usr.mobile}", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = "$userOrderCount order(s) • Total Spend: ${formatInr(userSpend)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SageSuccess
                                    )
                                }
                                if (usr.id != activeUser.id) {
                                    Switch(
                                        checked = usr.isActive,
                                        onCheckedChange = { onToggleUserActive(usr.id, it) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 4: DELIVERY & PIN CODES
                4 -> {
                    var stdCharge by remember(deliverySettings) { mutableStateOf(deliverySettings.standardCharge.toString()) }
                    var freeThreshold by remember(deliverySettings) { mutableStateOf(deliverySettings.freeDeliveryMinSubtotal.toString()) }
                    var rushCharge by remember(deliverySettings) { mutableStateOf(deliverySettings.rushExtraCharge.toString()) }
                    var stdEst by remember(deliverySettings) { mutableStateOf(deliverySettings.standardEstimateText) }
                    var rushEst by remember(deliverySettings) { mutableStateOf(deliverySettings.rushEstimateText) }
                    var newPin by remember { mutableStateOf("") }
                    var newArea by remember { mutableStateOf("") }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Delivery Charges & Estimates", style = MaterialTheme.typography.titleMedium)
                                    OutlinedTextField(value = stdCharge, onValueChange = { stdCharge = it.filter { c -> c.isDigit() } }, label = { Text("Standard Delivery ₹") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = freeThreshold, onValueChange = { freeThreshold = it.filter { c -> c.isDigit() } }, label = { Text("Free Standard Delivery Above ₹") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = rushCharge, onValueChange = { rushCharge = it.filter { c -> c.isDigit() } }, label = { Text("Rush Delivery Extra Charge ₹") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = stdEst, onValueChange = { stdEst = it }, label = { Text("Standard Estimate Text") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = rushEst, onValueChange = { rushEst = it }, label = { Text("Rush Estimate Text") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                    Button(
                                        onClick = {
                                            onSaveDeliverySettings(
                                                DeliverySettingsEntity(
                                                    id = 1,
                                                    standardCharge = stdCharge.toIntOrNull() ?: 50,
                                                    freeDeliveryMinSubtotal = freeThreshold.toIntOrNull() ?: 2000,
                                                    rushExtraCharge = rushCharge.toIntOrNull() ?: 250,
                                                    standardEstimateText = stdEst.trim(),
                                                    rushEstimateText = rushEst.trim()
                                                )
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Save Delivery Settings") }
                                }
                            }
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Add Rush-Eligible PIN Code", style = MaterialTheme.typography.titleMedium)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = newPin,
                                            onValueChange = { newPin = it.filter { c -> c.isDigit() }.take(6) },
                                            label = { Text("6-Digit PIN") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = newArea,
                                            onValueChange = { newArea = it },
                                            label = { Text("Service Zone Label") },
                                            singleLine = true,
                                            modifier = Modifier.weight(1.4f)
                                        )
                                    }
                                    Button(
                                        enabled = newPin.length == 6 && newArea.isNotBlank(),
                                        onClick = {
                                            onSavePinCode(newPin, newArea, true)
                                            newPin = ""
                                            newArea = ""
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Add Rush-Eligible PIN Code") }
                                }
                            }
                        }

                        items(eligiblePinCodes, key = { it.pinCode }) { pin ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("${pin.pinCode} — ${pin.areaLabel}", style = MaterialTheme.typography.titleSmall)
                                        Text(if (pin.isRushAvailable) "Rush Delivery Enabled" else "Rush Disabled", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Switch(
                                            checked = pin.isRushAvailable,
                                            onCheckedChange = { onSavePinCode(pin.pinCode, pin.areaLabel, it) }
                                        )
                                        IconButton(onClick = { onDeletePinCode(pin.pinCode) }) {
                                            Icon(Icons.Filled.Delete, contentDescription = "Delete PIN", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 5: OFFERS
                5 -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Button(
                            onClick = { showAddOffer = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create New Offer")
                        }
                    }
                    items(offers, key = { it.id }) { offer ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(offer.title, style = MaterialTheme.typography.titleSmall)
                                    Text(offer.description, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text = "Min Order: >${formatInr(offer.minOrderAmount)} • Active: ${offer.isActive}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (offer.isActive) SageSuccess else MaterialTheme.colorScheme.error
                                    )
                                }
                                Row {
                                    IconButton(onClick = { editingOffer = offer }) {
                                        Icon(Icons.Filled.Edit, contentDescription = "Edit offer")
                                    }
                                    IconButton(onClick = { onDeleteOffer(offer.id) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete offer", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }

                // 6: WHOLESALE
                6 -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (wholesaleEnquiries.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "No Wholesale Enquiries",
                                subtitle = "Bulk enquiries submitted by retailers and wholesalers will appear here."
                            )
                        }
                    } else {
                        items(wholesaleEnquiries, key = { it.id }) { enq ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(enq.businessName, style = MaterialTheme.typography.titleMedium)
                                        Text(WholesaleStatus.fromKey(enq.status).label, style = MaterialTheme.typography.labelMedium, color = SubtleGold)
                                    }
                                    Text("${enq.contactName} • ${enq.mobile}", style = MaterialTheme.typography.bodySmall)
                                    if (enq.gstNumber.isNotBlank()) {
                                        Text("GSTIN: ${enq.gstNumber}", style = MaterialTheme.typography.labelSmall)
                                    }
                                    Text("${enq.productName} × ${enq.requestedQuantity} units", style = MaterialTheme.typography.titleSmall)
                                    Button(
                                        onClick = { editingWholesale = enq },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Prepare Quote / Update Status")
                                    }
                                }
                            }
                        }
                    }
                }

                // 7: GST & GATEWAY/NOTIFICATION SETUP
                7 -> {
                    var bizName by remember(businessSettings) { mutableStateOf(businessSettings.businessName) }
                    var gstin by remember(businessSettings) { mutableStateOf(businessSettings.gstin) }
                    var stateName by remember(businessSettings) { mutableStateOf(businessSettings.stateName) }
                    var gstRate by remember(businessSettings) { mutableStateOf(businessSettings.defaultGstRatePercent.toString()) }
                    var invoicePrefix by remember(businessSettings) { mutableStateOf(businessSettings.invoicePrefix) }
                    var upiId by remember(businessSettings) { mutableStateOf(businessSettings.upiMerchantId) }

                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("GST Invoice Configuration (No Physical Address Required)", style = MaterialTheme.typography.titleMedium)
                                    OutlinedTextField(value = bizName, onValueChange = { bizName = it }, label = { Text("Registered Business Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = gstin, onValueChange = { gstin = it.uppercase().take(15) }, label = { Text("GSTIN (Optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = stateName, onValueChange = { stateName = it }, label = { Text("Seller State (for CGST/SGST vs IGST)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = gstRate, onValueChange = { gstRate = it }, label = { Text("Applicable GST Rate (%)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = invoicePrefix, onValueChange = { invoicePrefix = it }, label = { Text("Invoice Prefix") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                    OutlinedTextField(value = upiId, onValueChange = { upiId = it }, label = { Text("Merchant UPI VPA (Optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                                    Button(
                                        onClick = {
                                            onSaveBusinessSettings(
                                                businessSettings.copy(
                                                    businessName = bizName.trim(),
                                                    gstin = gstin.trim(),
                                                    stateName = stateName.trim(),
                                                    defaultGstRatePercent = gstRate.toDoubleOrNull() ?: 5.0,
                                                    invoicePrefix = invoicePrefix.trim().ifBlank { "ST-INV" },
                                                    upiMerchantId = upiId.trim()
                                                )
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) { Text("Save GST & Business Settings") }
                                }
                            }
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = PaleGoldContainer),
                                border = BorderStroke(1.dp, SubtleGold)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Icon(Icons.Filled.Lock, contentDescription = null)
                                        Text("Payment & Notification Integration Status", style = MaterialTheme.typography.titleSmall)
                                    }
                                    Text("• Razorpay Key ID: ${gatewayConfig.razorpayKeyPreview}", style = MaterialTheme.typography.bodySmall)
                                    Text("• Payment Backend Server: ${if (gatewayConfig.paymentBackendConfigured) "Configured" else "Not configured (Set PAYMENT_BACKEND_URL in AI Studio Secrets)"}", style = MaterialTheme.typography.bodySmall)
                                    Text("• Email/SMS/WhatsApp Webhook: ${if (gatewayConfig.notificationWebhookConfigured) "Configured" else "Not configured (Set NOTIFICATION_WEBHOOK_URL in AI Studio Secrets)"}", style = MaterialTheme.typography.bodySmall)
                                    Text("• Newsletter Subscribers: ${newsletterSubscribers.size}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddProduct || editingProduct != null) {
        ProductEditorDialog(
            existing = editingProduct,
            categories = categories,
            onImportImages = onImportImages,
            onDismiss = { showAddProduct = false; editingProduct = null },
            onSave = { entity ->
                onSaveProduct(entity)
                showAddProduct = false
                editingProduct = null
            }
        )
    }

    editingOrder?.let { order ->
        AdminOrderUpdateDialog(
            order = order,
            onDismiss = { editingOrder = null },
            onSave = { newStatus, note, newPayStatus ->
                onUpdateOrderStatus(order.id, newStatus, note, newPayStatus)
                editingOrder = null
            }
        )
    }

    if (showAddOffer || editingOffer != null) {
        OfferEditorDialog(
            existing = editingOffer,
            onDismiss = { showAddOffer = false; editingOffer = null },
            onSave = { offer ->
                onSaveOffer(offer)
                showAddOffer = false
                editingOffer = null
            }
        )
    }

    editingWholesale?.let { enq ->
        AdminWholesaleQuoteDialog(
            enquiry = enq,
            onDismiss = { editingWholesale = null },
            onSave = { price, status, remarks ->
                onUpdateWholesale(enq.id, price, status, remarks)
                editingWholesale = null
            }
        )
    }
}

@Composable
private fun MetricTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}
