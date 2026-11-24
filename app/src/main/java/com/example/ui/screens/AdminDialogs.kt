package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryEntity
import com.example.data.model.JsonCodec
import com.example.data.model.OfferEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentStatus
import com.example.data.model.Product
import com.example.data.model.ProductEntity
import com.example.data.model.WholesaleEnquiryEntity
import com.example.data.model.WholesaleStatus
import com.example.ui.components.ProductMediaItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProductEditorDialog(
    existing: Product?,
    categories: List<CategoryEntity>,
    onImportImages: suspend (List<Uri>) -> List<String>,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    val scope = rememberCoroutineScope()
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var price by remember { mutableStateOf(existing?.price?.toString() ?: "") }
    var originalPrice by remember { mutableStateOf(existing?.originalPrice?.toString() ?: "") }
    var wholesalePrice by remember { mutableStateOf(existing?.wholesalePrice?.toString() ?: "") }
    var brand by remember { mutableStateOf(existing?.brand ?: "Shasvat Textiles") }
    var selectedCategory by remember {
        mutableStateOf(
            categories.firstOrNull { it.id == existing?.categoryId } ?: categories.firstOrNull()
        )
    }
    var colorsCsv by remember { mutableStateOf(existing?.colors?.joinToString(", ") ?: "") }
    var sizesCsv by remember { mutableStateOf(existing?.sizes?.joinToString(", ") ?: "") }
    var fabricDetails by remember { mutableStateOf(existing?.fabricDetails ?: "") }
    var specifications by remember { mutableStateOf(existing?.specifications ?: "") }
    var stockQty by remember { mutableStateOf(existing?.stockQuantity?.toString() ?: "") }
    var isAvailable by remember { mutableStateOf(existing?.isAvailable ?: true) }
    var deliveryEstimate by remember { mutableStateOf(existing?.deliveryEstimate ?: "Within 24 hours to 2 days") }
    var isRushEligible by remember { mutableStateOf(existing?.isRushEligible ?: true) }
    var isTrending by remember { mutableStateOf(existing?.isTrending ?: false) }
    var isBestSeller by remember { mutableStateOf(existing?.isBestSeller ?: false) }
    var isNewArrival by remember { mutableStateOf(existing?.isNewArrival ?: true) }
    var uploadedUris by remember { mutableStateOf(existing?.uploadedImageUris ?: emptyList()) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 6)
    ) { uris ->
        if (uris.isNotEmpty()) {
            scope.launch {
                val imported = onImportImages(uris)
                uploadedUris = uploadedUris + imported
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add New Product" else "Edit ${existing.name}") },
        text = {
            LazyColumn(
                modifier = Modifier.height(420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_upload_images_button")
                    ) {
                        Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Product Photos (${uploadedUris.size})")
                    }
                    if (uploadedUris.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(uploadedUris) { uriStr ->
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    ProductMediaItem(
                                        imageSource = uriStr,
                                        contentDescription = "Uploaded product photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    IconButton(
                                        onClick = { uploadedUris = uploadedUris - uriStr },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = "Remove photo", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Product Name *") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_product_name_input")
                    )
                }
                item {
                    Text("Category", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory?.id == cat.id,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.name) }
                            )
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it.filter { c -> c.isDigit() } },
                            label = { Text("Retail Price ₹ *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_product_price_input")
                        )
                        OutlinedTextField(
                            value = originalPrice,
                            onValueChange = { originalPrice = it.filter { c -> c.isDigit() } },
                            label = { Text("MRP ₹ (Optional)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = wholesalePrice,
                            onValueChange = { wholesalePrice = it.filter { c -> c.isDigit() } },
                            label = { Text("Wholesale ₹") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = stockQty,
                            onValueChange = { stockQty = it.filter { c -> c.isDigit() } },
                            label = { Text("Stock Qty") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = colorsCsv,
                        onValueChange = { colorsCsv = it },
                        label = { Text("Colours (comma-separated)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = sizesCsv,
                        onValueChange = { sizesCsv = it },
                        label = { Text("Sizes (comma-separated)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = fabricDetails,
                        onValueChange = { fabricDetails = it },
                        label = { Text("Fabric Details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = specifications,
                        onValueChange = { specifications = it },
                        label = { Text("Product Specifications") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Product Description") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = deliveryEstimate,
                        onValueChange = { deliveryEstimate = it },
                        label = { Text("Delivery Estimate Text") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Available for Sale")
                        Switch(checked = isAvailable, onCheckedChange = { isAvailable = it })
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Rush Delivery Eligible")
                        Switch(checked = isRushEligible, onCheckedChange = { isRushEligible = it })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isTrending, onCheckedChange = { isTrending = it })
                        Text("Trending")
                        Checkbox(checked = isBestSeller, onCheckedChange = { isBestSeller = it })
                        Text("Best Seller")
                        Checkbox(checked = isNewArrival, onCheckedChange = { isNewArrival = it })
                        Text("New")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && (price.toIntOrNull() ?: 0) > 0,
                onClick = {
                    val cat = selectedCategory
                    val drawables = existing?.imageDrawables?.ifEmpty {
                        listOf(cat?.drawableName ?: "img_cat_chadar")
                    } ?: listOf(cat?.drawableName ?: "img_cat_chadar")

                    val colors = colorsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    val sizes = sizesCsv.split(",").map { it.trim() }.filter { it.isNotBlank() }

                    onSave(
                        ProductEntity(
                            id = existing?.id ?: "",
                            name = name.trim(),
                            description = description.trim(),
                            price = price.toInt(),
                            originalPrice = originalPrice.toIntOrNull(),
                            wholesalePrice = wholesalePrice.toIntOrNull(),
                            brand = brand.trim().ifBlank { "Shasvat Textiles" },
                            categoryId = cat?.id ?: existing?.categoryId ?: "cat_asli_chadar",
                            categoryName = cat?.name ?: existing?.categoryName ?: "Asli Solapuri Chadar",
                            imageDrawablesJson = JsonCodec.stringListToJson(drawables),
                            uploadedImageUrisJson = JsonCodec.stringListToJson(uploadedUris),
                            colorsJson = JsonCodec.stringListToJson(colors),
                            sizesJson = JsonCodec.stringListToJson(sizes),
                            fabricDetails = fabricDetails.trim(),
                            specifications = specifications.trim(),
                            stockQuantity = stockQty.toIntOrNull(),
                            isAvailable = isAvailable,
                            deliveryEstimate = deliveryEstimate.trim().ifBlank { "Within 24 hours to 2 days" },
                            isRushEligible = isRushEligible,
                            isTrending = isTrending,
                            isBestSeller = isBestSeller,
                            isNewArrival = isNewArrival,
                            createdAt = existing?.createdAt ?: System.currentTimeMillis()
                        )
                    )
                },
                modifier = Modifier.testTag("admin_save_product_button")
            ) {
                Text("Save Product")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminOrderUpdateDialog(
    order: OrderEntity,
    onDismiss: () -> Unit,
    onSave: (OrderStatus, String, PaymentStatus?) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(OrderStatus.fromKey(order.orderStatus)) }
    var selectedPayStatus by remember { mutableStateOf(PaymentStatus.fromKey(order.paymentStatus)) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage ${order.orderNumber}") },
        text = {
            LazyColumn(
                modifier = Modifier.height(360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("Order Status", style = MaterialTheme.typography.titleSmall)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OrderStatus.entries.forEach { st ->
                            FilterChip(
                                selected = selectedStatus == st,
                                onClick = { selectedStatus = st },
                                label = { Text(st.label) }
                            )
                        }
                    }
                }
                item {
                    Text("Payment Status", style = MaterialTheme.typography.titleSmall)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        PaymentStatus.entries.forEach { pst ->
                            FilterChip(
                                selected = selectedPayStatus == pst,
                                onClick = { selectedPayStatus = pst },
                                label = { Text(pst.label) }
                            )
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Status Update Note") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedStatus, note, selectedPayStatus) },
                modifier = Modifier.testTag("admin_confirm_order_update_button")
            ) {
                Text("Update Order")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun OfferEditorDialog(
    existing: OfferEntity?,
    onDismiss: () -> Unit,
    onSave: (OfferEntity) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var minAmount by remember { mutableStateOf(existing?.minOrderAmount?.toString() ?: "500") }
    var discount by remember { mutableStateOf(existing?.discountAmount?.toString() ?: "50") }
    var freeDelivery by remember { mutableStateOf(existing?.includesFreeStandardDelivery ?: false) }
    var freeGiftMax by remember { mutableStateOf(existing?.freeGiftMaxPrice?.toString() ?: "") }
    var expiryDays by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(existing?.isActive ?: true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Create Offer" else "Edit Offer") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Offer Title *") }, singleLine = true)
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                OutlinedTextField(
                    value = minAmount,
                    onValueChange = { minAmount = it.filter { c -> c.isDigit() } },
                    label = { Text("Minimum Order Amount ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = discount,
                    onValueChange = { discount = it.filter { c -> c.isDigit() } },
                    label = { Text("Discount Amount ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = freeGiftMax,
                    onValueChange = { freeGiftMax = it.filter { c -> c.isDigit() } },
                    label = { Text("Free Gift Max Value ₹ (Optional)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = expiryDays,
                    onValueChange = { expiryDays = it.filter { c -> c.isDigit() } },
                    label = { Text("Expires in Days (blank = no expiry)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Includes Free Standard Delivery")
                    Switch(checked = freeDelivery, onCheckedChange = { freeDelivery = it })
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Offer Active")
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank(),
                onClick = {
                    val days = expiryDays.toLongOrNull()
                    val expiryMillis = days?.let { System.currentTimeMillis() + it * 86_400_000L }
                        ?: existing?.expiryDateMillis
                    onSave(
                        OfferEntity(
                            id = existing?.id ?: "",
                            title = title.trim(),
                            description = description.trim(),
                            minOrderAmount = minAmount.toIntOrNull() ?: 0,
                            discountAmount = discount.toIntOrNull() ?: 0,
                            includesFreeStandardDelivery = freeDelivery,
                            freeGiftMaxPrice = freeGiftMax.toIntOrNull(),
                            expiryDateMillis = expiryMillis,
                            isActive = isActive,
                            isSystemDefault = existing?.isSystemDefault ?: false
                        )
                    )
                }
            ) { Text("Save Offer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminWholesaleQuoteDialog(
    enquiry: WholesaleEnquiryEntity,
    onDismiss: () -> Unit,
    onSave: (quotedUnitPrice: Int?, status: WholesaleStatus, remarks: String) -> Unit
) {
    var unitPrice by remember { mutableStateOf(enquiry.quotedUnitPrice?.toString() ?: "") }
    var status by remember { mutableStateOf(WholesaleStatus.fromKey(enquiry.status)) }
    var remarks by remember { mutableStateOf(enquiry.adminRemarks) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quote: ${enquiry.businessName}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("${enquiry.productName} • Qty: ${enquiry.requestedQuantity}", style = MaterialTheme.typography.titleSmall)
                OutlinedTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it.filter { c -> c.isDigit() } },
                    label = { Text("Quoted Unit Price ₹") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    WholesaleStatus.entries.forEach { st ->
                        FilterChip(
                            selected = status == st,
                            onClick = { status = st },
                            label = { Text(st.label) }
                        )
                    }
                }
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Admin Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onSave(unitPrice.toIntOrNull(), status, remarks) }) {
                Text("Save Quotation")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
