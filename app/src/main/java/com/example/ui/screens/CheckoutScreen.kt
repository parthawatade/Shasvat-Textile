package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.AddressEntity
import com.example.data.model.DeliveryMethod
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.network.GatewayConfigStatus
import com.example.ui.components.PriceBreakdownCard
import com.example.ui.components.formatInr
import com.example.ui.theme.PaleGoldContainer
import com.example.ui.theme.SageContainer
import com.example.ui.theme.SageSuccess
import com.example.ui.theme.SubtleGold
import com.example.viewmodel.CartUiState
import com.example.viewmodel.CheckoutUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    checkoutState: CheckoutUiState,
    cartState: CartUiState,
    savedAddresses: List<AddressEntity>,
    gatewayConfig: GatewayConfigStatus,
    isPinRushEligible: Boolean,
    onUpdateState: ((CheckoutUiState) -> CheckoutUiState) -> Unit,
    onApplySavedAddress: (AddressEntity) -> Unit,
    onCheckPinCode: (String) -> Unit,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onSelectDeliveryMethod: (DeliveryMethod) -> Unit,
    onPlaceOrder: () -> Unit,
    onTrackOrder: (String) -> Unit,
    onViewInvoice: (String) -> Unit,
    onContinueShopping: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stepTitles = listOf(
        "Step 1: Customer Information",
        "Step 2: Delivery Address",
        "Step 3: Delivery Method",
        "Step 4: Payment Method",
        "Step 5: Order Summary",
        "Step 6: Order Confirmed"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stepTitles.getOrElse(checkoutState.currentStep) { "Checkout" }) },
                navigationIcon = {
                    if (checkoutState.currentStep < 5) {
                        IconButton(onClick = {
                            if (checkoutState.currentStep == 0) onBack() else onPrevStep()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
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
            LinearProgressIndicator(
                progress = { (checkoutState.currentStep + 1) / 6f },
                modifier = Modifier.fillMaxWidth()
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                when (checkoutState.currentStep) {
                    // STEP 1: CUSTOMER INFORMATION
                    0 -> {
                        item {
                            OutlinedTextField(
                                value = checkoutState.customerName,
                                onValueChange = { v -> onUpdateState { it.copy(customerName = v) } },
                                label = { Text("Full Name *") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_name_input")
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = checkoutState.customerMobile,
                                onValueChange = { v ->
                                    onUpdateState { it.copy(customerMobile = v.filter { c -> c.isDigit() }.take(10)) }
                                },
                                label = { Text("10-Digit Mobile Number *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_mobile_input")
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = checkoutState.customerEmail,
                                onValueChange = { v -> onUpdateState { it.copy(customerEmail = v) } },
                                label = { Text("Email Address *") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_email_input")
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = checkoutState.alternateMobile,
                                onValueChange = { v ->
                                    onUpdateState { it.copy(alternateMobile = v.filter { c -> c.isDigit() }.take(10)) }
                                },
                                label = { Text("Alternate Mobile Number (Optional)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // STEP 2: DELIVERY ADDRESS
                    1 -> {
                        if (savedAddresses.isNotEmpty()) {
                            item {
                                Text("Quick Select Saved Address", style = MaterialTheme.typography.titleSmall)
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    savedAddresses.forEach { addr ->
                                        FilterChip(
                                            selected = checkoutState.pinCode == addr.pinCode &&
                                                checkoutState.houseNumber == addr.houseNumber,
                                            onClick = { onApplySavedAddress(addr) },
                                            label = { Text("${addr.label}: ${addr.houseNumber}, ${addr.area}, ${addr.city} (${addr.pinCode})") }
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            OutlinedTextField(
                                value = checkoutState.houseNumber,
                                onValueChange = { v -> onUpdateState { it.copy(houseNumber = v) } },
                                label = { Text("House / Flat Number *") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_house_input")
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = checkoutState.building,
                                onValueChange = { v -> onUpdateState { it.copy(building = v) } },
                                label = { Text("Building / Apartment") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = checkoutState.street,
                                onValueChange = { v -> onUpdateState { it.copy(street = v) } },
                                label = { Text("Street / Road *") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_street_input")
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = checkoutState.area,
                                onValueChange = { v -> onUpdateState { it.copy(area = v) } },
                                label = { Text("Area / Locality *") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("checkout_area_input")
                            )
                        }
                        item {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedTextField(
                                    value = checkoutState.city,
                                    onValueChange = { v -> onUpdateState { it.copy(city = v) } },
                                    label = { Text("City *") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("checkout_city_input")
                                )
                                OutlinedTextField(
                                    value = checkoutState.state,
                                    onValueChange = { v -> onUpdateState { it.copy(state = v) } },
                                    label = { Text("State *") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("checkout_state_input")
                                )
                            }
                        }
                        item {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = checkoutState.pinCode,
                                    onValueChange = { v ->
                                        val clean = v.filter { c -> c.isDigit() }.take(6)
                                        onUpdateState { it.copy(pinCode = clean) }
                                        if (clean.length == 6) onCheckPinCode(clean)
                                    },
                                    label = { Text("6-Digit PIN Code *") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("checkout_pincode_input")
                                )
                            }
                        }
                        item {
                            OutlinedTextField(
                                value = checkoutState.deliveryInstructions,
                                onValueChange = { v -> onUpdateState { it.copy(deliveryInstructions = v) } },
                                label = { Text("Delivery Instructions (Optional)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    onUpdateState { it.copy(saveAddressForFuture = !it.saveAddressForFuture) }
                                }
                            ) {
                                Checkbox(
                                    checked = checkoutState.saveAddressForFuture,
                                    onCheckedChange = { c -> onUpdateState { it.copy(saveAddressForFuture = c) } }
                                )
                                Text("Save this address to my account")
                            }
                        }
                    }

                    // STEP 3: DELIVERY METHOD
                    2 -> {
                        item {
                            Text(
                                text = "Delivery PIN Code: ${checkoutState.pinCode}",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(
                                    1.dp,
                                    if (checkoutState.deliveryMethod == DeliveryMethod.STANDARD) SubtleGold
                                    else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onUpdateState { it.copy(deliveryMethod = DeliveryMethod.STANDARD) }
                                        onSelectDeliveryMethod(DeliveryMethod.STANDARD)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = checkoutState.deliveryMethod == DeliveryMethod.STANDARD,
                                        onClick = {
                                            onUpdateState { it.copy(deliveryMethod = DeliveryMethod.STANDARD) }
                                            onSelectDeliveryMethod(DeliveryMethod.STANDARD)
                                        }
                                    )
                                    Column {
                                        Text("Standard Delivery", style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = "${cartState.deliverySettings.standardEstimateText} • ${if (cartState.priceBreakdown.isFreeStandardDeliveryApplied) "FREE" else formatInr(cartState.deliverySettings.standardCharge)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(
                                    1.dp,
                                    if (checkoutState.deliveryMethod == DeliveryMethod.RUSH) SubtleGold
                                    else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = isPinRushEligible) {
                                        onUpdateState { it.copy(deliveryMethod = DeliveryMethod.RUSH) }
                                        onSelectDeliveryMethod(DeliveryMethod.RUSH)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = checkoutState.deliveryMethod == DeliveryMethod.RUSH,
                                        onClick = {
                                            onUpdateState { it.copy(deliveryMethod = DeliveryMethod.RUSH) }
                                            onSelectDeliveryMethod(DeliveryMethod.RUSH)
                                        },
                                        enabled = isPinRushEligible
                                    )
                                    Column {
                                        Text(
                                            text = "Rush Delivery (Within 2 Hours)",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (isPinRushEligible) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        val rushNote = if (isPinRushEligible) {
                                            val totalDeliveryWithRush = (if (cartState.priceBreakdown.isFreeStandardDeliveryApplied) 0 else cartState.deliverySettings.standardCharge) + cartState.deliverySettings.rushExtraCharge
                                            "Eligible for PIN ${checkoutState.pinCode}! Additional +${formatInr(cartState.deliverySettings.rushExtraCharge)} (Total delivery: ${formatInr(totalDeliveryWithRush)})"
                                        } else {
                                            "Not eligible for PIN ${checkoutState.pinCode}. Available only in Rush-serviceable PIN areas."
                                        }
                                        Text(
                                            text = rushNote,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (isPinRushEligible) SageSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // STEP 4: PAYMENT METHOD
                    3 -> {
                        item {
                            Surface(
                                color = PaleGoldContainer,
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Lock, contentDescription = null)
                                    Text(
                                        text = "Security Guarantee: Shasvat Textiles never stores card numbers, CVVs, or API secret keys in the app. Online payments route through Razorpay/Cashfree server verification.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        items(PaymentMethod.entries) { method ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(
                                    1.dp,
                                    if (checkoutState.paymentMethod == method) SubtleGold
                                    else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("payment_method_${method.key}")
                                    .clickable { onUpdateState { it.copy(paymentMethod = method) } }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = checkoutState.paymentMethod == method,
                                        onClick = { onUpdateState { it.copy(paymentMethod = method) } }
                                    )
                                    Column {
                                        Text(method.label, style = MaterialTheme.typography.titleSmall)
                                        val subtitle = if (method == PaymentMethod.CASH_ON_DELIVERY) {
                                            "Pay in cash/UPI upon physical delivery. Confirmed by Admin."
                                        } else {
                                            "Processed securely via Razorpay / Cashfree Payment Gateway."
                                        }
                                        Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }

                        if (checkoutState.paymentMethod != PaymentMethod.CASH_ON_DELIVERY) {
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text("Payment Gateway Configuration", style = MaterialTheme.typography.titleSmall)
                                        Text(
                                            text = "Razorpay Key ID: ${gatewayConfig.razorpayKeyPreview}\nVerification Server: ${if (gatewayConfig.paymentBackendConfigured) "Configured" else "Not configured (Set RAZORPAY_KEY_ID & PAYMENT_BACKEND_URL in AI Studio Secrets)"}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text("Gateway Sandbox Test Mode", style = MaterialTheme.typography.titleSmall)
                                                Text(
                                                    text = "Use test credentials (e.g. success@razorpay or failure@razorpay) when live backend is not connected.",
                                                    style = MaterialTheme.typography.bodySmall
                                                )
                                            }
                                            Switch(
                                                checked = checkoutState.useGatewayTestMode,
                                                onCheckedChange = { checked ->
                                                    onUpdateState { it.copy(useGatewayTestMode = checked) }
                                                }
                                            )
                                        }
                                        if (checkoutState.useGatewayTestMode) {
                                            OutlinedTextField(
                                                value = checkoutState.gatewayTestCredential,
                                                onValueChange = { v -> onUpdateState { it.copy(gatewayTestCredential = v) } },
                                                label = { Text("Test UPI / Gateway Token (e.g. success@razorpay)") },
                                                singleLine = true,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .testTag("checkout_test_credential_input")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // STEP 5: ORDER SUMMARY
                    4 -> {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("Deliver To", style = MaterialTheme.typography.titleSmall)
                                    Text("${checkoutState.customerName} • ${checkoutState.customerMobile}", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = "${checkoutState.houseNumber}, ${checkoutState.building}, ${checkoutState.street}, ${checkoutState.area}, ${checkoutState.city}, ${checkoutState.state} - ${checkoutState.pinCode}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    Text("Payment: ${checkoutState.paymentMethod.label}", style = MaterialTheme.typography.labelLarge)
                                    Text("Delivery: ${checkoutState.deliveryMethod.label}", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }

                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Items (${cartState.totalActiveCount})", style = MaterialTheme.typography.titleSmall)
                                    cartState.activeItems.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${item.product.name} × ${item.cartItem.quantity}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(formatInr(item.lineTotal), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            PriceBreakdownCard(breakdown = cartState.priceBreakdown)
                        }
                    }

                    // STEP 6: ORDER CONFIRMATION
                    5 -> {
                        val order = checkoutState.placedOrder
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SageContainer),
                                border = BorderStroke(1.dp, SageSuccess)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = "Order Confirmed",
                                        tint = SageSuccess,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Text("Order Placed Successfully!", style = MaterialTheme.typography.headlineSmall)
                                    if (order != null) {
                                        Text(
                                            text = "Order ID: ${order.orderNumber}",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Amount: ${formatInr(order.finalPayableAmount)} • ${PaymentStatus.fromKey(order.paymentStatus).label}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Estimated Delivery: ${order.estimatedDelivery}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Button(
                                                onClick = { onTrackOrder(order.id) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                                    .testTag("confirmation_track_order")
                                            ) {
                                                Icon(Icons.Filled.TrackChanges, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Track Order")
                                            }
                                            OutlinedButton(
                                                onClick = { onViewInvoice(order.id) },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                                    .testTag("confirmation_view_invoice")
                                            ) {
                                                Icon(Icons.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("GST Invoice")
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = onContinueShopping,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp)
                                        ) {
                                            Text("Continue Shopping")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                checkoutState.errorMessage?.let { err ->
                    item {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer,
                            shape = MaterialTheme.shapes.small,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = err,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }
            }

            if (checkoutState.currentStep < 5) {
                Surface(
                    tonalElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (checkoutState.currentStep > 0) {
                            OutlinedButton(
                                onClick = onPrevStep,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text("Previous")
                            }
                        }
                        if (checkoutState.currentStep < 4) {
                            Button(
                                onClick = onNextStep,
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                                    .testTag("checkout_next_button")
                            ) {
                                Text("Continue")
                            }
                        } else {
                            Button(
                                onClick = onPlaceOrder,
                                enabled = !checkoutState.isSubmitting,
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                                    .testTag("checkout_place_order_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SubtleGold,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                )
                            ) {
                                if (checkoutState.isSubmitting) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                } else {
                                    Text(
                                        text = "Place Order (${formatInr(cartState.priceBreakdown.finalPayableAmount)})",
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
}
