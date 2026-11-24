package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.Product
import com.example.data.model.UserEntity
import com.example.data.model.WholesaleEnquiryEntity
import com.example.data.model.WholesaleStatus
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.formatDateTime
import com.example.ui.components.formatInr
import com.example.ui.theme.PaleGoldContainer
import com.example.ui.theme.SageContainer
import com.example.ui.theme.SageSuccess

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WholesaleScreen(
    products: List<Product>,
    enquiries: List<WholesaleEnquiryEntity>,
    activeUser: UserEntity?,
    onBack: () -> Unit,
    onSubmitEnquiry: (
        businessName: String,
        contactName: String,
        mobile: String,
        email: String,
        gstNumber: String,
        productId: String,
        quantity: Int,
        notes: String,
        onSuccess: () -> Unit
    ) -> Unit,
    onRespondToQuote: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var businessName by remember { mutableStateOf("") }
    var contactName by remember(activeUser) { mutableStateOf(activeUser?.fullName ?: "") }
    var mobile by remember(activeUser) { mutableStateOf(activeUser?.mobile ?: "") }
    var email by remember(activeUser) { mutableStateOf(activeUser?.email ?: "") }
    var gstNumber by remember { mutableStateOf("") }
    var selectedProductId by remember(products) { mutableStateOf(products.firstOrNull()?.id ?: "") }
    var quantityInput by remember { mutableStateOf("25") }
    var notes by remember { mutableStateOf("") }

    val selectedProduct = products.firstOrNull { it.id == selectedProductId }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Wholesale & Bulk Orders") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
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
                    colors = CardDefaults.cardColors(containerColor = PaleGoldContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Filled.Business, contentDescription = null)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Retailer & Wholesaler Bulk Portal", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "Submit bulk textile requirements with optional GSTIN. Admin sets custom wholesale pricing and sends quotations directly here.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Bulk Enquiry Form", style = MaterialTheme.typography.titleLarge)

                        OutlinedTextField(
                            value = businessName,
                            onValueChange = { businessName = it },
                            label = { Text("Business / Store Name *") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wholesale_business_input")
                        )
                        OutlinedTextField(
                            value = contactName,
                            onValueChange = { contactName = it },
                            label = { Text("Contact Person Name *") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wholesale_contact_input")
                        )
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it.filter { c -> c.isDigit() }.take(10) },
                            label = { Text("10-Digit Mobile Number *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wholesale_mobile_input")
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Business Email") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = gstNumber,
                            onValueChange = { gstNumber = it.uppercase().take(15) },
                            label = { Text("GST Number (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("Select Product", style = MaterialTheme.typography.titleSmall)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            products.forEach { prod ->
                                FilterChip(
                                    selected = selectedProductId == prod.id,
                                    onClick = { selectedProductId = prod.id },
                                    label = {
                                        val wPrice = prod.wholesalePrice?.let { " • Bulk ${formatInr(it)}" } ?: ""
                                        Text("${prod.name}$wPrice")
                                    }
                                )
                            }
                        }

                        selectedProduct?.let { prod ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (prod.wholesalePrice != null) {
                                        "Wholesale Rate Configured: ${formatInr(prod.wholesalePrice)} / unit (Retail: ${formatInr(prod.price)})"
                                    } else {
                                        "Retail Rate: ${formatInr(prod.price)} • Custom bulk quote will be prepared by Admin."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = quantityInput,
                            onValueChange = { quantityInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Bulk Quantity (Minimum 5 units) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wholesale_quantity_input")
                        )

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { notes = it },
                            label = { Text("Colour / Size / Packaging Notes (Optional)") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                onSubmitEnquiry(
                                    businessName,
                                    contactName,
                                    mobile,
                                    email,
                                    gstNumber,
                                    selectedProductId,
                                    quantityInput.toIntOrNull() ?: 0,
                                    notes
                                ) {
                                    notes = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("wholesale_submit_button")
                        ) {
                            Text("Submit Bulk Enquiry")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "My Wholesale Enquiries & Quotations (${enquiries.size})",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (enquiries.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No Wholesale Enquiries Yet",
                        subtitle = if (activeUser == null) "Sign in to track your wholesale enquiries and quotations across sessions."
                        else "Submit a bulk enquiry above to receive custom wholesale quotations."
                    )
                }
            } else {
                items(enquiries, key = { it.id }) { enq ->
                    val status = WholesaleStatus.fromKey(enq.status)
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(enq.businessName, style = MaterialTheme.typography.titleMedium)
                                Surface(
                                    color = if (status == WholesaleStatus.APPROVED) SageContainer else PaleGoldContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = status.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (status == WholesaleStatus.APPROVED) SageSuccess else MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            Text(
                                text = "${enq.productName} • Qty: ${enq.requestedQuantity} units",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (enq.quotedUnitPrice != null && enq.quotedTotalAmount != null) {
                                Text(
                                    text = "Quoted Price: ${formatInr(enq.quotedUnitPrice)} / unit • Total: ${formatInr(enq.quotedTotalAmount)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (enq.adminRemarks.isNotBlank()) {
                                Text("Admin Remarks: ${enq.adminRemarks}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(
                                text = "Submitted: ${formatDateTime(enq.createdAt)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (status == WholesaleStatus.QUOTED) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { onRespondToQuote(enq.id, true) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Accept Quote")
                                    }
                                    OutlinedButton(
                                        onClick = { onRespondToQuote(enq.id, false) },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Decline")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
