package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BusinessSettingsEntity
import com.example.data.model.DeliveryMethod
import com.example.data.model.JsonCodec
import com.example.data.model.NotificationEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.OrderTimelineView
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatInr
import com.example.ui.components.formatInrDouble
import com.example.ui.theme.PaleGoldContainer
import com.example.ui.theme.SageContainer
import com.example.ui.theme.SageSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersListScreen(
    orders: List<OrderEntity>,
    onBack: () -> Unit,
    onOpenOrder: (String) -> Unit,
    onBrowseProducts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("My Orders (${orders.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    title = "No Orders Yet",
                    subtitle = "Once you place an order, you can track its live status across all 8 stages and download GST invoices here.",
                    actionLabel = "Start Shopping",
                    onAction = onBrowseProducts
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders, key = { it.id }) { order ->
                    val status = OrderStatus.fromKey(order.orderStatus)
                    val payStatus = PaymentStatus.fromKey(order.paymentStatus)
                    val items = JsonCodec.jsonToOrderItems(order.itemsJson)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("order_card_${order.id}")
                            .clickable { onOpenOrder(order.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(order.orderNumber, style = MaterialTheme.typography.titleMedium)
                                Surface(
                                    color = when (status) {
                                        OrderStatus.DELIVERED -> SageContainer
                                        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                                        else -> PaleGoldContainer
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = status.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = when (status) {
                                            OrderStatus.DELIVERED -> SageSuccess
                                            OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
                                            else -> MaterialTheme.colorScheme.onSecondaryContainer
                                        },
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text(
                                text = formatDateTime(order.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = items.joinToString { "${it.productName} ×${it.quantity}" },
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = payStatus.label,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = formatInr(order.finalPayableAmount),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    order: OrderEntity?,
    onBack: () -> Unit,
    onCancelOrder: (String) -> Unit,
    onOpenInvoice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(order?.orderNumber ?: "Order Tracking") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (order == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Order not found")
            }
            return@Scaffold
        }

        val currentStatus = OrderStatus.fromKey(order.orderStatus)
        val history = JsonCodec.jsonToStatusHistory(order.statusHistoryJson)
        val items = JsonCodec.jsonToOrderItems(order.itemsJson)
        val address = JsonCodec.jsonToAddress(order.shippingAddressJson)
        val canCancel = currentStatus.stepIndex in 0 until OrderStatus.SHIPPED.stepIndex

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Order ${order.orderNumber}", style = MaterialTheme.typography.titleLarge)
                        Text("Placed on ${formatDateTime(order.createdAt)}", style = MaterialTheme.typography.bodySmall)
                        Text(
                            text = "Delivery Method: ${DeliveryMethod.fromKey(order.deliveryMethod).label}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Estimated Delivery: ${order.estimatedDelivery}",
                            style = MaterialTheme.typography.labelLarge,
                            color = SageSuccess
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text(
                            text = "Payment: ${PaymentMethod.fromKey(order.paymentMethod).label}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Payment Status: ${PaymentStatus.fromKey(order.paymentStatus).label}",
                            style = MaterialTheme.typography.labelLarge
                        )
                        if (order.paymentReference.isNotBlank()) {
                            Text(
                                text = "Reference: ${order.paymentReference}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Live 8-status Tracking Timeline
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Order Status Timeline", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Live status updates appear automatically as the store updates your order.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OrderTimelineView(currentStatus = currentStatus, history = history)
                    }
                }
            }

            // Products Ordered & Price Breakdown
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Items Ordered", style = MaterialTheme.typography.titleMedium)
                        items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.productName} × ${item.quantity}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(formatInr(item.lineTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        if (!order.freeGiftProductName.isNullOrBlank()) {
                            Text(
                                text = "Free Gift Included: ${order.freeGiftProductName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = SageSuccess
                            )
                        }
                        HorizontalDivider()
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", style = MaterialTheme.typography.bodySmall)
                            Text(formatInr(order.subtotal), style = MaterialTheme.typography.bodySmall)
                        }
                        if (order.discountAmount > 0) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(order.appliedOfferTitle ?: "Offer Discount", style = MaterialTheme.typography.bodySmall, color = SageSuccess)
                                Text("-${formatInr(order.discountAmount)}", style = MaterialTheme.typography.bodySmall, color = SageSuccess)
                            }
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Delivery Charge", style = MaterialTheme.typography.bodySmall)
                            Text(if (order.deliveryCharge == 0) "FREE" else formatInr(order.deliveryCharge), style = MaterialTheme.typography.bodySmall)
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Shopping Bag Charge", style = MaterialTheme.typography.bodySmall)
                            Text(formatInr(order.shoppingBagCharge), style = MaterialTheme.typography.bodySmall)
                        }
                        HorizontalDivider()
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Paid / Payable", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(formatInr(order.finalPayableAmount), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Delivery Address
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Delivery Address", style = MaterialTheme.typography.titleSmall)
                        Text("${order.customerName} • ${order.customerMobile}", style = MaterialTheme.typography.bodyMedium)
                        Text(address.formattedSingleLine(), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Actions: GST Invoice + Cancel
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { onOpenInvoice(order.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("order_view_gst_invoice_button")
                    ) {
                        Icon(Icons.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View & Print GST Tax Invoice")
                    }
                    if (canCancel) {
                        OutlinedButton(
                            onClick = { onCancelOrder(order.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(Icons.Filled.Cancel, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cancel Order", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GstInvoiceScreen(
    order: OrderEntity?,
    businessSettings: BusinessSettingsEntity,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("GST Tax Invoice") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (order == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Order invoice not found")
            }
            return@Scaffold
        }

        val items = JsonCodec.jsonToOrderItems(order.itemsJson)
        val address = JsonCodec.jsonToAddress(order.shippingAddressJson)
        val isIntraState = order.igstAmount <= 0.0

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = businessSettings.businessName.ifBlank { "SHASVAT TEXTILES" },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "GSTIN: ${businessSettings.gstin.ifBlank { "Not configured (Set in Admin -> GST Settings)" }}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "Origin State: ${businessSettings.stateName}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Surface(color = PaleGoldContainer, shape = RoundedCornerShape(6.dp)) {
                                Text(
                                    text = "TAX INVOICE",
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        HorizontalDivider()

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Invoice No:", style = MaterialTheme.typography.labelSmall)
                                Text(order.invoiceNumber, style = MaterialTheme.typography.titleSmall)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Invoice Date:", style = MaterialTheme.typography.labelSmall)
                                Text(formatDateTime(order.createdAt), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Text("Order Reference: ${order.orderNumber}", style = MaterialTheme.typography.bodySmall)

                        HorizontalDivider()

                        Text("Billed & Shipped To", style = MaterialTheme.typography.titleSmall)
                        Text("${order.customerName} • ${order.customerMobile}", style = MaterialTheme.typography.bodyMedium)
                        Text(order.customerEmail, style = MaterialTheme.typography.bodySmall)
                        Text(address.formattedSingleLine(), style = MaterialTheme.typography.bodySmall)

                        HorizontalDivider()

                        Text("Itemized Summary", style = MaterialTheme.typography.titleSmall)
                        items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.productName} (Qty: ${item.quantity} × ${formatInr(item.unitPrice)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(formatInr(item.lineTotal), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        HorizontalDivider()

                        InvoiceRow("Taxable Value (Excl. GST)", formatInrDouble(order.taxableAmount))
                        if (isIntraState) {
                            InvoiceRow(
                                "CGST (${order.gstRatePercent / 2.0}%)",
                                formatInrDouble(order.cgstAmount)
                            )
                            InvoiceRow(
                                "SGST (${order.gstRatePercent / 2.0}%)",
                                formatInrDouble(order.sgstAmount)
                            )
                        } else {
                            InvoiceRow(
                                "IGST (${order.gstRatePercent}%)",
                                formatInrDouble(order.igstAmount)
                            )
                        }
                        InvoiceRow("Delivery Charge (Included)", formatInr(order.deliveryCharge))
                        InvoiceRow("Shopping Bag Charge (Included)", formatInr(order.shoppingBagCharge))
                        if (order.discountAmount > 0) {
                            InvoiceRow("Discount Applied", "-${formatInr(order.discountAmount)}")
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Grand Total (Incl. GST)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                text = formatInr(order.finalPayableAmount),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { printGstInvoice(context, order, businessSettings) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("print_gst_invoice_button")
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print / PDF")
                    }
                    OutlinedButton(
                        onClick = { shareInvoiceSummary(context, order, businessSettings) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share")
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

private fun printGstInvoice(
    context: Context,
    order: OrderEntity,
    business: BusinessSettingsEntity
) {
    val items = JsonCodec.jsonToOrderItems(order.itemsJson)
    val addr = JsonCodec.jsonToAddress(order.shippingAddressJson)
    val itemRows = items.joinToString("") {
        "<tr><td>${it.productName}</td><td>${it.quantity}</td><td>₹${it.unitPrice}</td><td>₹${it.lineTotal}</td></tr>"
    }
    val taxRows = if (order.igstAmount <= 0.0) {
        "<p>CGST (${order.gstRatePercent / 2}%): ₹${order.cgstAmount} | SGST (${order.gstRatePercent / 2}%): ₹${order.sgstAmount}</p>"
    } else {
        "<p>IGST (${order.gstRatePercent}%): ₹${order.igstAmount}</p>"
    }

    val html = """
        <html><body style="font-family:sans-serif;padding:24px;">
        <h2>${business.businessName} - TAX INVOICE</h2>
        <p><strong>GSTIN:</strong> ${business.gstin.ifBlank { "Not configured" }} | <strong>State:</strong> ${business.stateName}</p>
        <p><strong>Invoice No:</strong> ${order.invoiceNumber} | <strong>Order ID:</strong> ${order.orderNumber}</p>
        <hr/>
        <p><strong>Customer:</strong> ${order.customerName} (${order.customerMobile})<br/>
        <strong>Ship To:</strong> ${addr.formattedSingleLine()}</p>
        <table border="1" cellspacing="0" cellpadding="8" width="100%">
        <tr><th>Product</th><th>Qty</th><th>Rate</th><th>Total</th></tr>
        $itemRows
        </table>
        <p><strong>Taxable Value:</strong> ₹${order.taxableAmount}</p>
        $taxRows
        <p><strong>Delivery:</strong> ₹${order.deliveryCharge} | <strong>Shopping Bag:</strong> ₹${order.shoppingBagCharge} | <strong>Discount:</strong> -₹${order.discountAmount}</p>
        <h3>Grand Total: ₹${order.finalPayableAmount}</h3>
        </body></html>
    """.trimIndent()

    val webView = WebView(context)
    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            val adapter = webView.createPrintDocumentAdapter("Invoice_${order.invoiceNumber}")
            printManager?.print("Invoice_${order.invoiceNumber}", adapter, PrintAttributes.Builder().build())
        }
    }
    webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
}

private fun shareInvoiceSummary(
    context: Context,
    order: OrderEntity,
    business: BusinessSettingsEntity
) {
    val text = buildString {
        appendLine("${business.businessName} — GST Tax Invoice")
        appendLine("Invoice No: ${order.invoiceNumber}")
        appendLine("Order ID: ${order.orderNumber}")
        if (business.gstin.isNotBlank()) appendLine("GSTIN: ${business.gstin}")
        appendLine("Customer: ${order.customerName}")
        appendLine("Taxable Amount: ₹${order.taxableAmount}")
        if (order.igstAmount <= 0.0) {
            appendLine("CGST: ₹${order.cgstAmount} | SGST: ₹${order.sgstAmount}")
        } else {
            appendLine("IGST: ₹${order.igstAmount}")
        }
        appendLine("Grand Total: ₹${order.finalPayableAmount}")
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Tax Invoice ${order.invoiceNumber}")
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, "Share GST Invoice"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    notifications: List<NotificationEntity>,
    onMarkAllRead: () -> Unit,
    onOpenOrder: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        onMarkAllRead()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Notifications (${notifications.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    title = "No Notifications Yet",
                    subtitle = "Order confirmations, payment updates, order tracking alerts, and cancellations will appear here."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(notifications, key = { it.id }) { notif ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = notif.orderId.isNotBlank()) {
                                onOpenOrder(notif.orderId)
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Filled.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(notif.title, style = MaterialTheme.typography.titleSmall)
                                Text(notif.message, style = MaterialTheme.typography.bodySmall)
                                Text(
                                    text = "${formatDateTime(notif.createdAt)} • ${notif.channelStatus}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
