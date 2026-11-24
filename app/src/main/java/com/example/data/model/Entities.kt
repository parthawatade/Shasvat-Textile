package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

enum class UserRole(val key: String) {
    CUSTOMER("CUSTOMER"),
    ADMIN("ADMIN");

    companion object {
        fun fromKey(raw: String): UserRole = entries.firstOrNull { it.key == raw } ?: CUSTOMER
    }
}

enum class DeliveryMethod(val key: String, val label: String) {
    STANDARD("STANDARD", "Standard Delivery"),
    RUSH("RUSH", "Rush Delivery (Within 2 Hours)");

    companion object {
        fun fromKey(raw: String): DeliveryMethod = entries.firstOrNull { it.key == raw } ?: STANDARD
    }
}

enum class PaymentMethod(val key: String, val label: String) {
    UPI("UPI", "UPI (GPay, PhonePe, Paytm, BHIM)"),
    DEBIT_CARD("DEBIT_CARD", "Debit Card (via Payment Gateway)"),
    CREDIT_CARD("CREDIT_CARD", "Credit Card (via Payment Gateway)"),
    CASH_ON_DELIVERY("CASH_ON_DELIVERY", "Cash on Delivery (COD)");

    companion object {
        fun fromKey(raw: String): PaymentMethod = entries.firstOrNull { it.key == raw } ?: CASH_ON_DELIVERY
    }
}

enum class PaymentStatus(val key: String, val label: String) {
    COD_PENDING("COD_PENDING", "Pay on Delivery (COD)"),
    PAID_COD("PAID_COD", "Paid (Cash on Delivery)"),
    PENDING_GATEWAY_VERIFICATION("PENDING_GATEWAY_VERIFICATION", "Pending Gateway Verification"),
    PAID_TEST_MODE("PAID_TEST_MODE", "Paid (Gateway Test Mode)"),
    PAID("PAID", "Paid (Verified)"),
    FAILED("FAILED", "Payment Failed"),
    REFUNDED("REFUNDED", "Refunded");

    companion object {
        fun fromKey(raw: String): PaymentStatus = entries.firstOrNull { it.key == raw } ?: COD_PENDING
    }
}

enum class OrderStatus(val key: String, val label: String, val stepIndex: Int) {
    ORDER_PLACED("ORDER_PLACED", "Order Placed", 0),
    ORDER_CONFIRMED("ORDER_CONFIRMED", "Order Confirmed", 1),
    PROCESSING("PROCESSING", "Processing", 2),
    PACKED("PACKED", "Packed", 3),
    SHIPPED("SHIPPED", "Shipped", 4),
    OUT_FOR_DELIVERY("OUT_FOR_DELIVERY", "Out for Delivery", 5),
    DELIVERED("DELIVERED", "Delivered", 6),
    CANCELLED("CANCELLED", "Cancelled", -1);

    companion object {
        fun fromKey(raw: String): OrderStatus = entries.firstOrNull { it.key == raw } ?: ORDER_PLACED
        val progressionOrder: List<OrderStatus> = listOf(
            ORDER_PLACED,
            ORDER_CONFIRMED,
            PROCESSING,
            PACKED,
            SHIPPED,
            OUT_FOR_DELIVERY,
            DELIVERED
        )
    }
}

enum class WholesaleStatus(val key: String, val label: String) {
    PENDING("PENDING", "Pending Review"),
    QUOTED("QUOTED", "Quotation Sent"),
    APPROVED("APPROVED", "Approved & Confirmed"),
    DISPATCHED("DISPATCHED", "Dispatched"),
    REJECTED("REJECTED", "Declined");

    companion object {
        fun fromKey(raw: String): WholesaleStatus = entries.firstOrNull { it.key == raw } ?: PENDING
    }
}

enum class SortOption(val label: String) {
    POPULARITY("Popularity"),
    PRICE_LOW_TO_HIGH("Price: Low to High"),
    PRICE_HIGH_TO_LOW("Price: High to Low"),
    CUSTOMER_RATING("Customer Rating"),
    NEW_ARRIVALS("New Arrivals")
}

enum class PriceBracket(val label: String, val min: Int?, val max: Int?) {
    UNDER_500("Under ₹500", 0, 499),
    RANGE_500_1000("₹500 – ₹1,000", 500, 1000),
    RANGE_1000_2500("₹1,000 – ₹2,500", 1000, 2500),
    RANGE_2500_5000("₹2,500 – ₹5,000", 2500, 5000),
    RANGE_5000_10000("₹5,000 – ₹10,000", 5000, 10000),
    ABOVE_10000("Above ₹10,000", 10001, null)
}

enum class DeliverySpeedFilter(val label: String) {
    WITHIN_2_HOURS("Within 2 Hours (Rush)"),
    WITHIN_24_HOURS("Within 24 Hours"),
    WITHIN_2_DAYS("Within 2 Days")
}

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val drawableName: String,
    val sortOrder: Int
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val originalPrice: Int? = null,
    val wholesalePrice: Int? = null,
    val brand: String,
    val categoryId: String,
    val categoryName: String,
    val imageDrawablesJson: String,
    val uploadedImageUrisJson: String = "[]",
    val colorsJson: String = "[]",
    val sizesJson: String = "[]",
    val fabricDetails: String = "",
    val specifications: String = "",
    val stockQuantity: Int? = null,
    val isAvailable: Boolean = true,
    val deliveryEstimate: String = "24 hours to 2 days",
    val isRushEligible: Boolean = true,
    val isTrending: Boolean = false,
    val isBestSeller: Boolean = false,
    val isNewArrival: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val productId: String,
    val quantity: Int,
    val selectedColor: String = "",
    val selectedSize: String = "",
    val isSavedForLater: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wishlist_items")
data class WishlistItemEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val productId: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val mobile: String,
    val passwordHash: String,
    val passwordSalt: String,
    val role: String = UserRole.CUSTOMER.key,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "addresses")
data class AddressEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val label: String,
    val fullName: String,
    val mobile: String,
    val alternateMobile: String = "",
    val houseNumber: String,
    val building: String = "",
    val street: String,
    val area: String,
    val city: String,
    val state: String,
    val pinCode: String,
    val deliveryInstructions: String = "",
    val isDefault: Boolean = false
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val orderNumber: String,
    val userId: String,
    val customerName: String,
    val customerMobile: String,
    val customerEmail: String,
    val alternateMobile: String = "",
    val shippingAddressJson: String,
    val pinCode: String,
    val deliveryMethod: String,
    val estimatedDelivery: String,
    val paymentMethod: String,
    val paymentStatus: String,
    val paymentReference: String = "",
    val orderStatus: String,
    val statusHistoryJson: String,
    val itemsJson: String,
    val freeGiftProductId: String? = null,
    val freeGiftProductName: String? = null,
    val appliedOfferId: String? = null,
    val appliedOfferTitle: String? = null,
    val subtotal: Int,
    val discountAmount: Int,
    val deliveryCharge: Int,
    val shoppingBagCharge: Int,
    val finalPayableAmount: Int,
    val invoiceNumber: String,
    val gstRatePercent: Double,
    val taxableAmount: Double,
    val cgstAmount: Double,
    val sgstAmount: Double,
    val igstAmount: Double,
    val idempotencyKey: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val productId: String,
    val productName: String,
    val userId: String,
    val reviewerName: String,
    val rating: Int,
    val title: String,
    val comment: String,
    val isVerifiedBuyer: Boolean,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "offers")
data class OfferEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val minOrderAmount: Int,
    val discountAmount: Int,
    val includesFreeStandardDelivery: Boolean,
    val freeGiftMaxPrice: Int? = null,
    val expiryDateMillis: Long? = null,
    val isActive: Boolean = true,
    val isSystemDefault: Boolean = false
)

@Entity(tableName = "wholesale_enquiries")
data class WholesaleEnquiryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val businessName: String,
    val contactName: String,
    val mobile: String,
    val email: String,
    val gstNumber: String = "",
    val productId: String,
    val productName: String,
    val requestedQuantity: Int,
    val notes: String = "",
    val quotedUnitPrice: Int? = null,
    val quotedTotalAmount: Int? = null,
    val status: String = WholesaleStatus.PENDING.key,
    val adminRemarks: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "delivery_settings")
data class DeliverySettingsEntity(
    @PrimaryKey val id: Int = 1,
    val standardCharge: Int = 50,
    val freeDeliveryMinSubtotal: Int = 2000,
    val rushExtraCharge: Int = 250,
    val standardEstimateText: String = "Within 24 hours to 2 days",
    val rushEstimateText: String = "Within 2 hours"
)

@Entity(tableName = "eligible_pincodes")
data class EligiblePinCodeEntity(
    @PrimaryKey val pinCode: String,
    val areaLabel: String,
    val isRushAvailable: Boolean = true
)

@Entity(tableName = "business_settings")
data class BusinessSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val businessName: String = "Shasvat Textiles",
    val gstin: String = "",
    val stateName: String = "Maharashtra",
    val defaultGstRatePercent: Double = 5.0,
    val invoicePrefix: String = "ST-INV",
    val upiMerchantId: String = "",
    val upiMerchantName: String = "Shasvat Textiles"
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val orderId: String = "",
    val title: String,
    val message: String,
    val channelStatus: String = "In-App Delivered | Email/SMS/WhatsApp webhook not configured",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "newsletter_subscribers")
data class NewsletterEntity(
    @PrimaryKey val email: String,
    val subscribedAt: Long = System.currentTimeMillis()
)

// Domain / Presentation Models

data class Product(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val originalPrice: Int?,
    val wholesalePrice: Int?,
    val brand: String,
    val categoryId: String,
    val categoryName: String,
    val imageDrawables: List<String>,
    val uploadedImageUris: List<String>,
    val colors: List<String>,
    val sizes: List<String>,
    val fabricDetails: String,
    val specifications: String,
    val stockQuantity: Int?,
    val isAvailable: Boolean,
    val deliveryEstimate: String,
    val isRushEligible: Boolean,
    val isTrending: Boolean,
    val isBestSeller: Boolean,
    val isNewArrival: Boolean,
    val averageRating: Double,
    val reviewCount: Int,
    val createdAt: Long,
    val updatedAt: Long
) {
    val isInStock: Boolean
        get() = isAvailable && (stockQuantity == null || stockQuantity > 0)

    val discountPercent: Int?
        get() = if (originalPrice != null && originalPrice > price) {
            (((originalPrice - price) * 100.0) / originalPrice).toInt()
        } else null
}

data class CartItemDetail(
    val cartItem: CartItemEntity,
    val product: Product
) {
    val lineTotal: Int get() = product.price * cartItem.quantity
}

data class OrderItemSnapshot(
    val productId: String,
    val productName: String,
    val categoryName: String,
    val unitPrice: Int,
    val quantity: Int,
    val selectedColor: String,
    val selectedSize: String,
    val lineTotal: Int,
    val imageDrawable: String,
    val uploadedImageUri: String?
)

data class OrderStatusStep(
    val statusKey: String,
    val statusLabel: String,
    val timestamp: Long,
    val note: String = ""
)

data class ShippingAddress(
    val houseNumber: String,
    val building: String,
    val street: String,
    val area: String,
    val city: String,
    val state: String,
    val pinCode: String,
    val deliveryInstructions: String
) {
    fun formattedSingleLine(): String = listOfNotNull(
        houseNumber.takeIf { it.isNotBlank() },
        building.takeIf { it.isNotBlank() },
        street.takeIf { it.isNotBlank() },
        area.takeIf { it.isNotBlank() },
        city.takeIf { it.isNotBlank() },
        state.takeIf { it.isNotBlank() },
        pinCode.takeIf { it.isNotBlank() }?.let { "PIN $it" }
    ).joinToString(", ")
}

data class OfferEvaluation(
    val offer: OfferEntity,
    val isEligible: Boolean,
    val amountShort: Int,
    val estimatedSavings: Int
)

data class PriceBreakdown(
    val productSubtotal: Int,
    val offerDiscount: Int,
    val standardDeliveryCharge: Int,
    val rushDeliveryCharge: Int,
    val totalDeliveryCharge: Int,
    val shoppingBagCharge: Int,
    val finalPayableAmount: Int,
    val appliedOffer: OfferEntity?,
    val isFreeStandardDeliveryApplied: Boolean,
    val isFreeGiftEligible: Boolean,
    val freeGiftMaxPrice: Int?,
    val selectedFreeGift: Product?,
    val isRushAvailableForPin: Boolean,
    val estimatedDeliveryText: String
)

data class CatalogFilterState(
    val query: String = "",
    val selectedCategories: Set<String> = emptySet(),
    val selectedPriceBrackets: Set<PriceBracket> = emptySet(),
    val customMinPrice: Int? = null,
    val customMaxPrice: Int? = null,
    val selectedBrands: Set<String> = emptySet(),
    val minRating: Int? = null,
    val deliverySpeed: DeliverySpeedFilter? = null,
    val sortOption: SortOption = SortOption.POPULARITY
) {
    val activeFilterCount: Int
        get() = selectedCategories.size +
            selectedPriceBrackets.size +
            (if (customMinPrice != null || customMaxPrice != null) 1 else 0) +
            selectedBrands.size +
            (if (minRating != null) 1 else 0) +
            (if (deliverySpeed != null) 1 else 0)
}

object JsonCodec {
    fun stringListToJson(items: List<String>): String {
        val arr = JSONArray()
        items.forEach { arr.put(it) }
        return arr.toString()
    }

    fun jsonToStringList(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { idx ->
                arr.optString(idx).takeIf { it.isNotBlank() }
            }
        }.getOrDefault(emptyList())
    }

    fun orderItemsToJson(items: List<OrderItemSnapshot>): String {
        val arr = JSONArray()
        items.forEach { item ->
            arr.put(JSONObject().apply {
                put("productId", item.productId)
                put("productName", item.productName)
                put("categoryName", item.categoryName)
                put("unitPrice", item.unitPrice)
                put("quantity", item.quantity)
                put("selectedColor", item.selectedColor)
                put("selectedSize", item.selectedSize)
                put("lineTotal", item.lineTotal)
                put("imageDrawable", item.imageDrawable)
                put("uploadedImageUri", item.uploadedImageUri ?: "")
            })
        }
        return arr.toString()
    }

    fun jsonToOrderItems(raw: String): List<OrderItemSnapshot> = runCatching {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { idx ->
            val obj = arr.getJSONObject(idx)
            OrderItemSnapshot(
                productId = obj.optString("productId"),
                productName = obj.optString("productName"),
                categoryName = obj.optString("categoryName"),
                unitPrice = obj.optInt("unitPrice"),
                quantity = obj.optInt("quantity"),
                selectedColor = obj.optString("selectedColor"),
                selectedSize = obj.optString("selectedSize"),
                lineTotal = obj.optInt("lineTotal"),
                imageDrawable = obj.optString("imageDrawable"),
                uploadedImageUri = obj.optString("uploadedImageUri").takeIf { it.isNotBlank() }
            )
        }
    }.getOrDefault(emptyList())

    fun statusHistoryToJson(steps: List<OrderStatusStep>): String {
        val arr = JSONArray()
        steps.forEach { step ->
            arr.put(JSONObject().apply {
                put("statusKey", step.statusKey)
                put("statusLabel", step.statusLabel)
                put("timestamp", step.timestamp)
                put("note", step.note)
            })
        }
        return arr.toString()
    }

    fun jsonToStatusHistory(raw: String): List<OrderStatusStep> = runCatching {
        val arr = JSONArray(raw)
        (0 until arr.length()).map { idx ->
            val obj = arr.getJSONObject(idx)
            OrderStatusStep(
                statusKey = obj.optString("statusKey"),
                statusLabel = obj.optString("statusLabel"),
                timestamp = obj.optLong("timestamp"),
                note = obj.optString("note")
            )
        }
    }.getOrDefault(emptyList())

    fun addressToJson(addr: ShippingAddress): String = JSONObject().apply {
        put("houseNumber", addr.houseNumber)
        put("building", addr.building)
        put("street", addr.street)
        put("area", addr.area)
        put("city", addr.city)
        put("state", addr.state)
        put("pinCode", addr.pinCode)
        put("deliveryInstructions", addr.deliveryInstructions)
    }.toString()

    fun jsonToAddress(raw: String): ShippingAddress = runCatching {
        val obj = JSONObject(raw)
        ShippingAddress(
            houseNumber = obj.optString("houseNumber"),
            building = obj.optString("building"),
            street = obj.optString("street"),
            area = obj.optString("area"),
            city = obj.optString("city"),
            state = obj.optString("state"),
            pinCode = obj.optString("pinCode"),
            deliveryInstructions = obj.optString("deliveryInstructions")
        )
    }.getOrElse {
        ShippingAddress("", "", "", "", "", "", "", "")
    }
}

fun ProductEntity.toDomain(averageRating: Double = 0.0, reviewCount: Int = 0): Product {
    return Product(
        id = id,
        name = name,
        description = description,
        price = price,
        originalPrice = originalPrice,
        wholesalePrice = wholesalePrice,
        brand = brand,
        categoryId = categoryId,
        categoryName = categoryName,
        imageDrawables = JsonCodec.jsonToStringList(imageDrawablesJson),
        uploadedImageUris = JsonCodec.jsonToStringList(uploadedImageUrisJson),
        colors = JsonCodec.jsonToStringList(colorsJson),
        sizes = JsonCodec.jsonToStringList(sizesJson),
        fabricDetails = fabricDetails,
        specifications = specifications,
        stockQuantity = stockQuantity,
        isAvailable = isAvailable,
        deliveryEstimate = deliveryEstimate,
        isRushEligible = isRushEligible,
        isTrending = isTrending,
        isBestSeller = isBestSeller,
        isNewArrival = isNewArrival,
        averageRating = averageRating,
        reviewCount = reviewCount,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
