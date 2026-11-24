package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.domain.PricingEngine
import com.example.data.local.SeedCatalogData
import com.example.data.local.ShasvatDao
import com.example.data.local.ShasvatDatabase
import com.example.data.model.AddressEntity
import com.example.data.model.BusinessSettingsEntity
import com.example.data.model.CartItemDetail
import com.example.data.model.CartItemEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.DeliveryMethod
import com.example.data.model.DeliverySettingsEntity
import com.example.data.model.EligiblePinCodeEntity
import com.example.data.model.JsonCodec
import com.example.data.model.NewsletterEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.OfferEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderItemSnapshot
import com.example.data.model.OrderStatus
import com.example.data.model.OrderStatusStep
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.Product
import com.example.data.model.ProductEntity
import com.example.data.model.ReviewEntity
import com.example.data.model.ShippingAddress
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.WholesaleEnquiryEntity
import com.example.data.model.WholesaleStatus
import com.example.data.model.WishlistItemEntity
import com.example.data.model.toDomain
import com.example.data.network.GatewayConfigStatus
import com.example.data.network.GatewayExecutionResult
import com.example.data.network.NotificationWebhookPayload
import com.example.data.network.PaymentGatewayManager
import com.example.data.preferences.SessionPreferences
import com.example.data.security.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class ShasvatRepository(
    private val appContext: Context,
    private val dao: ShasvatDao,
    private val sessionPrefs: SessionPreferences,
    private val paymentGateway: PaymentGatewayManager = PaymentGatewayManager()
) {
    private val seedMutex = Mutex()
    private val orderMutex = Mutex()
    private var seeded = false

    companion object {
        @Volatile
        private var INSTANCE: ShasvatRepository? = null

        fun getInstance(context: Context): ShasvatRepository {
            return INSTANCE ?: synchronized(this) {
                val appCtx = context.applicationContext
                val db = ShasvatDatabase.getInstance(appCtx)
                val prefs = SessionPreferences(appCtx)
                val repo = ShasvatRepository(appCtx, db.shasvatDao(), prefs)
                INSTANCE = repo
                repo
            }
        }
    }

    suspend fun ensureSeeded() {
        if (seeded) return
        seedMutex.withLock {
            if (seeded) return
            if (dao.getProductCount() == 0) {
                dao.upsertCategories(SeedCatalogData.initialCategories)
                dao.upsertProducts(SeedCatalogData.initialProducts)
            }
            if (dao.getOfferCount() == 0) {
                dao.upsertOffers(SeedCatalogData.initialOffers)
            }
            if (dao.getDeliverySettings() == null) {
                dao.upsertDeliverySettings(SeedCatalogData.initialDeliverySettings)
                dao.upsertEligiblePinCodes(SeedCatalogData.initialRushPinCodes)
            }
            if (dao.getBusinessSettings() == null) {
                dao.upsertBusinessSettings(SeedCatalogData.initialBusinessSettings)
            }
            seeded = true
        }
    }

    // Reactive Streams

    val activeUserIdFlow: Flow<String> = sessionPrefs.activeUserId
    val selectedPinCodeFlow: Flow<String> = sessionPrefs.selectedPinCode
    val selectedOfferIdFlow: Flow<String?> = sessionPrefs.selectedOfferId

    val activeUserFlow: Flow<UserEntity?> = activeUserIdFlow.flatMapLatest { uid ->
        if (uid == SessionPreferences.GUEST_USER_ID) flowOf(null)
        else dao.observeUserById(uid)
    }

    val adminCountFlow: Flow<Int> = dao.observeAdminCount()

    val categoriesFlow: Flow<List<CategoryEntity>> = dao.observeCategories()

    val allReviewsFlow: Flow<List<ReviewEntity>> = dao.observeAllReviews()

    val productsFlow: Flow<List<Product>> = combine(
        dao.observeProducts(),
        dao.observeAllReviews()
    ) { entities, reviews ->
        val reviewsByProduct = reviews.groupBy { it.productId }
        entities.map { entity ->
            val prodReviews = reviewsByProduct[entity.id].orEmpty()
            val avg = if (prodReviews.isEmpty()) 0.0 else prodReviews.map { it.rating }.average()
            entity.toDomain(averageRating = avg, reviewCount = prodReviews.size)
        }
    }

    fun observeProduct(productId: String): Flow<Product?> = combine(
        dao.observeProductById(productId),
        dao.observeReviewsForProduct(productId)
    ) { entity, reviews ->
        entity?.let {
            val avg = if (reviews.isEmpty()) 0.0 else reviews.map { r -> r.rating }.average()
            it.toDomain(averageRating = avg, reviewCount = reviews.size)
        }
    }

    fun observeReviewsForProduct(productId: String): Flow<List<ReviewEntity>> =
        dao.observeReviewsForProduct(productId)

    val cartDetailsFlow: Flow<List<CartItemDetail>> = activeUserIdFlow.flatMapLatest { uid ->
        combine(
            dao.observeCartItems(uid),
            productsFlow
        ) { cartItems, products ->
            val productMap = products.associateBy { it.id }
            cartItems.mapNotNull { item ->
                productMap[item.productId]?.let { prod -> CartItemDetail(item, prod) }
            }
        }
    }

    val wishlistProductsFlow: Flow<List<Product>> = activeUserIdFlow.flatMapLatest { uid ->
        combine(
            dao.observeWishlistItems(uid),
            productsFlow
        ) { wishlist, products ->
            val productMap = products.associateBy { it.id }
            wishlist.mapNotNull { w -> productMap[w.productId] }
        }
    }

    val wishlistIdsFlow: Flow<Set<String>> = activeUserIdFlow.flatMapLatest { uid ->
        dao.observeWishlistItems(uid).map { items -> items.map { it.productId }.toSet() }
    }

    val offersFlow: Flow<List<OfferEntity>> = dao.observeOffers()

    val deliverySettingsFlow: Flow<DeliverySettingsEntity> = dao.observeDeliverySettings()
        .map { it ?: SeedCatalogData.initialDeliverySettings }

    val eligiblePinCodesFlow: Flow<List<EligiblePinCodeEntity>> = dao.observeEligiblePinCodes()

    val businessSettingsFlow: Flow<BusinessSettingsEntity> = dao.observeBusinessSettings()
        .map { it ?: SeedCatalogData.initialBusinessSettings }

    val userAddressesFlow: Flow<List<AddressEntity>> = activeUserIdFlow.flatMapLatest { uid ->
        if (uid == SessionPreferences.GUEST_USER_ID) flowOf(emptyList())
        else dao.observeAddressesForUser(uid)
    }

    val userOrdersFlow: Flow<List<OrderEntity>> = activeUserIdFlow.flatMapLatest { uid ->
        if (uid == SessionPreferences.GUEST_USER_ID) flowOf(emptyList())
        else dao.observeOrdersForUser(uid)
    }

    fun observeOrderById(orderId: String): Flow<OrderEntity?> = dao.observeOrderById(orderId)

    val userNotificationsFlow: Flow<List<NotificationEntity>> = activeUserIdFlow.flatMapLatest { uid ->
        dao.observeNotificationsForUser(uid)
    }

    val userWholesaleEnquiriesFlow: Flow<List<WholesaleEnquiryEntity>> = activeUserIdFlow.flatMapLatest { uid ->
        if (uid == SessionPreferences.GUEST_USER_ID) flowOf(emptyList())
        else dao.observeWholesaleEnquiriesForUser(uid)
    }

    // Admin Flows (guarded at UI/VM + repo mutation checks)
    val adminAllOrdersFlow: Flow<List<OrderEntity>> = dao.observeAllOrders()
    val adminAllUsersFlow: Flow<List<UserEntity>> = dao.observeAllUsers()
    val adminAllWholesaleFlow: Flow<List<WholesaleEnquiryEntity>> = dao.observeAllWholesaleEnquiries()
    val adminAllNotificationsFlow: Flow<List<NotificationEntity>> = dao.observeAllNotifications()
    val adminNewsletterFlow: Flow<List<NewsletterEntity>> = dao.observeNewsletterSubscribers()

    // Session & PIN helpers

    suspend fun setSelectedPinCode(pinCode: String) = sessionPrefs.setSelectedPinCode(pinCode)

    suspend fun setSelectedOfferId(offerId: String?) = sessionPrefs.setSelectedOfferId(offerId)

    suspend fun isPinRushEligible(pinCode: String): Boolean {
        val cleaned = pinCode.trim()
        if (cleaned.length != 6 || cleaned.any { !it.isDigit() }) return false
        val record = dao.getEligiblePinCode(cleaned)
        return record?.isRushAvailable == true
    }

    fun getGatewayConfigStatus(): GatewayConfigStatus = paymentGateway.getConfigStatus()

    // Authentication

    suspend fun signUp(
        fullName: String,
        email: String,
        mobile: String,
        password: String,
        asInitialAdmin: Boolean = false
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val cleanName = fullName.trim()
            val cleanEmail = email.trim().lowercase()
            val cleanMobile = mobile.trim()
            require(cleanName.length >= 2) { "Please enter your full name." }
            require(cleanEmail.contains("@") && cleanEmail.contains(".")) { "Please enter a valid email address." }
            require(cleanMobile.length == 10 && cleanMobile.all { it.isDigit() }) { "Enter a valid 10-digit mobile number." }
            require(password.length >= 6) { "Password must be at least 6 characters." }

            if (dao.getUserByEmail(cleanEmail) != null) {
                return@withContext Result.failure(IllegalStateException("An account with this email already exists."))
            }
            if (dao.getUserByMobile(cleanMobile) != null) {
                return@withContext Result.failure(IllegalStateException("An account with this mobile number already exists."))
            }

            val role = if (asInitialAdmin) {
                if (dao.getAdminCount() > 0) {
                    return@withContext Result.failure(SecurityException("Store Admin account has already been claimed."))
                }
                UserRole.ADMIN
            } else {
                UserRole.CUSTOMER
            }

            val salt = PasswordHasher.generateSalt()
            val hash = PasswordHasher.hashPassword(password, salt)
            val newUser = UserEntity(
                id = "usr_${UUID.randomUUID().toString().take(12)}",
                fullName = cleanName,
                email = cleanEmail,
                mobile = cleanMobile,
                passwordHash = hash,
                passwordSalt = salt,
                role = role.key,
                isActive = true
            )
            dao.upsertUser(newUser)
            migrateGuestStateToUser(newUser.id)
            sessionPrefs.setActiveUserId(newUser.id)
            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(emailOrMobile: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        try {
            val identifier = emailOrMobile.trim()
            require(identifier.isNotBlank() && password.isNotBlank()) { "Enter email/mobile and password." }
            val user = dao.getUserByEmail(identifier.lowercase()) ?: dao.getUserByMobile(identifier)
                ?: return@withContext Result.failure(IllegalArgumentException("No account found with those credentials."))

            if (!user.isActive) {
                return@withContext Result.failure(IllegalStateException("This account has been deactivated by the store administrator."))
            }
            if (!PasswordHasher.verifyPassword(password, user.passwordSalt, user.passwordHash)) {
                return@withContext Result.failure(IllegalArgumentException("Incorrect password."))
            }

            migrateGuestStateToUser(user.id)
            sessionPrefs.setActiveUserId(user.id)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resetPassword(
        email: String,
        mobile: String,
        newPassword: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val user = dao.getUserByEmail(email.trim().lowercase())
                ?: return@withContext Result.failure(IllegalArgumentException("Email address not found."))
            if (user.mobile != mobile.trim()) {
                return@withContext Result.failure(IllegalArgumentException("Mobile number does not match our records for this email."))
            }
            require(newPassword.length >= 6) { "New password must be at least 6 characters." }
            val salt = PasswordHasher.generateSalt()
            val hash = PasswordHasher.hashPassword(newPassword, salt)
            dao.updateUser(user.copy(passwordHash = hash, passwordSalt = salt))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        sessionPrefs.setActiveUserId(SessionPreferences.GUEST_USER_ID)
    }

    suspend fun updateCustomerProfile(fullName: String, mobile: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = currentAuthenticatedUserId() ?: error("Please sign in first.")
            val user = dao.getUserById(uid) ?: error("User profile not found.")
            require(fullName.trim().length >= 2) { "Enter a valid full name." }
            require(mobile.trim().length == 10 && mobile.trim().all { it.isDigit() }) { "Enter a valid 10-digit mobile number." }
            dao.updateUser(user.copy(fullName = fullName.trim(), mobile = mobile.trim()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun migrateGuestStateToUser(newUserId: String) {
        val guestCart = dao.getCartItems(SessionPreferences.GUEST_USER_ID)
        val userCart = dao.getCartItems(newUserId)
        for (guestItem in guestCart) {
            val match = userCart.firstOrNull {
                it.productId == guestItem.productId &&
                    it.selectedColor == guestItem.selectedColor &&
                    it.selectedSize == guestItem.selectedSize &&
                    it.isSavedForLater == guestItem.isSavedForLater
            }
            if (match != null) {
                dao.upsertCartItem(match.copy(quantity = match.quantity + guestItem.quantity))
                dao.deleteCartItem(guestItem.id)
            } else {
                dao.upsertCartItem(guestItem.copy(userId = newUserId))
            }
        }
        val guestWishlist = dao.getWishlistItems(SessionPreferences.GUEST_USER_ID)
        val userWishlistIds = dao.getWishlistItems(newUserId).map { it.productId }.toSet()
        for (w in guestWishlist) {
            if (w.productId in userWishlistIds) {
                dao.removeWishlistItem(SessionPreferences.GUEST_USER_ID, w.productId)
            } else {
                dao.insertWishlistItem(w.copy(userId = newUserId))
                dao.removeWishlistItem(SessionPreferences.GUEST_USER_ID, w.productId)
            }
        }
    }

    private suspend fun currentEffectiveUserId(): String {
        var latest = SessionPreferences.GUEST_USER_ID
        sessionPrefs.activeUserId.collectOnce { latest = it }
        return latest
    }

    private suspend fun currentAuthenticatedUserId(): String? {
        val uid = currentEffectiveUserId()
        return if (uid == SessionPreferences.GUEST_USER_ID) null else uid
    }

    private suspend fun requireAdminUser(): UserEntity {
        val uid = currentAuthenticatedUserId() ?: throw SecurityException("Admin sign-in required.")
        val user = dao.getUserById(uid) ?: throw SecurityException("Admin account not found.")
        if (user.role != UserRole.ADMIN.key || !user.isActive) {
            throw SecurityException("Unauthorized: Customer accounts cannot access Admin functions.")
        }
        return user
    }

    // Addresses

    suspend fun saveAddress(address: AddressEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = currentAuthenticatedUserId() ?: error("Sign in to save addresses.")
            if (address.isDefault) {
                dao.clearDefaultAddress(uid)
            }
            val finalId = address.id.ifBlank { "addr_${UUID.randomUUID().toString().take(10)}" }
            dao.upsertAddress(address.copy(id = finalId, userId = uid))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAddress(addressId: String) = withContext(Dispatchers.IO) {
        dao.deleteAddress(addressId)
    }

    // Cart & Wishlist

    suspend fun addToCart(
        productId: String,
        quantity: Int = 1,
        selectedColor: String = "",
        selectedSize: String = ""
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val product = dao.getProductById(productId) ?: error("Product not found.")
            if (!product.isAvailable || (product.stockQuantity != null && product.stockQuantity <= 0)) {
                return@withContext Result.failure(IllegalStateException("This product is currently out of stock."))
            }
            val uid = currentEffectiveUserId()
            val existing = dao.getCartItems(uid).firstOrNull {
                !it.isSavedForLater &&
                    it.productId == productId &&
                    it.selectedColor == selectedColor &&
                    it.selectedSize == selectedSize
            }
            val newQty = (existing?.quantity ?: 0) + quantity.coerceAtLeast(1)
            if (product.stockQuantity != null && newQty > product.stockQuantity) {
                return@withContext Result.failure(
                    IllegalStateException("Only ${product.stockQuantity} units available in stock.")
                )
            }
            val item = existing?.copy(quantity = newQty) ?: CartItemEntity(
                id = "cart_${UUID.randomUUID().toString().take(12)}",
                userId = uid,
                productId = productId,
                quantity = newQty,
                selectedColor = selectedColor,
                selectedSize = selectedSize,
                isSavedForLater = false
            )
            dao.upsertCartItem(item)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCartQuantity(cartItemId: String, newQuantity: Int): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = currentEffectiveUserId()
            val existing = dao.getCartItems(uid).firstOrNull { it.id == cartItemId }
                ?: return@withContext Result.success(Unit)
            if (newQuantity <= 0) {
                dao.deleteCartItem(cartItemId)
                return@withContext Result.success(Unit)
            }
            val product = dao.getProductById(existing.productId)
            if (product?.stockQuantity != null && newQuantity > product.stockQuantity) {
                return@withContext Result.failure(
                    IllegalStateException("Only ${product.stockQuantity} units available in stock.")
                )
            }
            dao.upsertCartItem(existing.copy(quantity = newQuantity))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleSaveForLater(cartItemId: String, saveForLater: Boolean) = withContext(Dispatchers.IO) {
        val uid = currentEffectiveUserId()
        val existing = dao.getCartItems(uid).firstOrNull { it.id == cartItemId } ?: return@withContext
        dao.upsertCartItem(existing.copy(isSavedForLater = saveForLater))
    }

    suspend fun removeFromCart(cartItemId: String) = withContext(Dispatchers.IO) {
        dao.deleteCartItem(cartItemId)
    }

    suspend fun toggleWishlist(productId: String): Boolean = withContext(Dispatchers.IO) {
        val uid = currentEffectiveUserId()
        val current = dao.getWishlistItems(uid)
        val exists = current.any { it.productId == productId }
        if (exists) {
            dao.removeWishlistItem(uid, productId)
            false
        } else {
            dao.insertWishlistItem(
                WishlistItemEntity(
                    id = "wish_${UUID.randomUUID().toString().take(12)}",
                    userId = uid,
                    productId = productId
                )
            )
            true
        }
    }

    // Checkout & Orders (Authoritative server-side/repository price recalculation + duplicate prevention)

    suspend fun placeOrder(
        customerName: String,
        customerMobile: String,
        customerEmail: String,
        alternateMobile: String,
        shippingAddress: ShippingAddress,
        deliveryMethod: DeliveryMethod,
        paymentMethod: PaymentMethod,
        preferredOfferId: String?,
        selectedFreeGiftId: String?,
        idempotencyKey: String,
        useGatewayTestMode: Boolean,
        gatewayTestCredential: String
    ): Result<OrderEntity> = orderMutex.withLock {
        withContext(Dispatchers.IO) {
            try {
                // Prevent duplicate order submission
                dao.getOrderByIdempotencyKey(idempotencyKey)?.let { existing ->
                    return@withContext Result.success(existing)
                }

                val uid = currentEffectiveUserId()
                val cartItems = dao.getCartItems(uid).filter { !it.isSavedForLater }
                if (cartItems.isEmpty()) {
                    return@withContext Result.failure(IllegalStateException("Your shopping cart is empty."))
                }

                val activeProducts = mutableListOf<CartItemDetail>()
                for (c in cartItems) {
                    val entity = dao.getProductById(c.productId)
                        ?: return@withContext Result.failure(IllegalStateException("Product no longer exists."))
                    if (!entity.isAvailable) {
                        return@withContext Result.failure(IllegalStateException("${entity.name} is currently unavailable."))
                    }
                    if (entity.stockQuantity != null && entity.stockQuantity < c.quantity) {
                        return@withContext Result.failure(
                            IllegalStateException("Insufficient stock for ${entity.name}. Available: ${entity.stockQuantity}")
                        )
                    }
                    activeProducts.add(CartItemDetail(c, entity.toDomain()))
                }

                val deliverySettings = dao.getDeliverySettings() ?: SeedCatalogData.initialDeliverySettings
                val pinRushEligible = isPinRushEligible(shippingAddress.pinCode)
                if (deliveryMethod == DeliveryMethod.RUSH && !pinRushEligible) {
                    return@withContext Result.failure(
                        IllegalStateException("Rush delivery is not available for PIN code ${shippingAddress.pinCode}.")
                    )
                }

                val offers = dao.getAllOffers()
                val giftProduct = selectedFreeGiftId?.let { dao.getProductById(it)?.toDomain() }

                val breakdown = PricingEngine.calculatePriceBreakdown(
                    cartItems = activeProducts,
                    offers = offers,
                    deliverySettings = deliverySettings,
                    preferredOfferId = preferredOfferId,
                    deliveryMethod = deliveryMethod,
                    isPinRushEligible = pinRushEligible,
                    selectedFreeGift = giftProduct
                )

                val orderNumber = "ST-${(100000..999999).random()}"

                // Execute payment verification before persisting order
                val paymentStatus: PaymentStatus
                val paymentReference: String
                if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY) {
                    paymentStatus = PaymentStatus.COD_PENDING
                    paymentReference = "COD-$orderNumber"
                } else {
                    when (val gwResult = paymentGateway.processOnlinePayment(
                        orderNumber = orderNumber,
                        amountInr = breakdown.finalPayableAmount,
                        paymentMethod = paymentMethod,
                        customerEmail = customerEmail,
                        customerMobile = customerMobile,
                        useTestMode = useGatewayTestMode,
                        testCredentialInput = gatewayTestCredential
                    )) {
                        is GatewayExecutionResult.Verified -> {
                            paymentStatus = gwResult.paymentStatus
                            paymentReference = gwResult.paymentReference
                        }
                        is GatewayExecutionResult.PendingVerification -> {
                            paymentStatus = gwResult.paymentStatus
                            paymentReference = gwResult.paymentReference
                        }
                        is GatewayExecutionResult.Failed -> {
                            return@withContext Result.failure(IllegalStateException(gwResult.reason))
                        }
                    }
                }

                val business = dao.getBusinessSettings() ?: SeedCatalogData.initialBusinessSettings
                val gst = PricingEngine.calculateGstTax(
                    finalPayableAmount = breakdown.finalPayableAmount,
                    gstRatePercent = business.defaultGstRatePercent,
                    sellerState = business.stateName,
                    customerState = shippingAddress.state
                )

                val now = System.currentTimeMillis()
                val initialHistory = listOf(
                    OrderStatusStep(
                        statusKey = OrderStatus.ORDER_PLACED.key,
                        statusLabel = OrderStatus.ORDER_PLACED.label,
                        timestamp = now,
                        note = "Order received by Shasvat Textiles"
                    )
                )

                val itemSnapshots = activeProducts.map { detail ->
                    OrderItemSnapshot(
                        productId = detail.product.id,
                        productName = detail.product.name,
                        categoryName = detail.product.categoryName,
                        unitPrice = detail.product.price,
                        quantity = detail.cartItem.quantity,
                        selectedColor = detail.cartItem.selectedColor,
                        selectedSize = detail.cartItem.selectedSize,
                        lineTotal = detail.lineTotal,
                        imageDrawable = detail.product.imageDrawables.firstOrNull() ?: "img_cat_chadar",
                        uploadedImageUri = detail.product.uploadedImageUris.firstOrNull()
                    )
                }

                val order = OrderEntity(
                    id = "ord_${UUID.randomUUID().toString().take(12)}",
                    orderNumber = orderNumber,
                    userId = uid,
                    customerName = customerName.trim(),
                    customerMobile = customerMobile.trim(),
                    customerEmail = customerEmail.trim(),
                    alternateMobile = alternateMobile.trim(),
                    shippingAddressJson = JsonCodec.addressToJson(shippingAddress),
                    pinCode = shippingAddress.pinCode.trim(),
                    deliveryMethod = deliveryMethod.key,
                    estimatedDelivery = breakdown.estimatedDeliveryText,
                    paymentMethod = paymentMethod.key,
                    paymentStatus = paymentStatus.key,
                    paymentReference = paymentReference,
                    orderStatus = OrderStatus.ORDER_PLACED.key,
                    statusHistoryJson = JsonCodec.statusHistoryToJson(initialHistory),
                    itemsJson = JsonCodec.orderItemsToJson(itemSnapshots),
                    freeGiftProductId = breakdown.selectedFreeGift?.id,
                    freeGiftProductName = breakdown.selectedFreeGift?.name,
                    appliedOfferId = breakdown.appliedOffer?.id,
                    appliedOfferTitle = breakdown.appliedOffer?.title,
                    subtotal = breakdown.productSubtotal,
                    discountAmount = breakdown.offerDiscount,
                    deliveryCharge = breakdown.totalDeliveryCharge,
                    shoppingBagCharge = breakdown.shoppingBagCharge,
                    finalPayableAmount = breakdown.finalPayableAmount,
                    invoiceNumber = "${business.invoicePrefix}-${orderNumber.removePrefix("ST-")}",
                    gstRatePercent = business.defaultGstRatePercent,
                    taxableAmount = gst.taxableAmount,
                    cgstAmount = gst.cgstAmount,
                    sgstAmount = gst.sgstAmount,
                    igstAmount = gst.igstAmount,
                    idempotencyKey = idempotencyKey,
                    createdAt = now,
                    updatedAt = now
                )

                // Decrement configured stock quantities
                for (detail in activeProducts) {
                    val currentStock = detail.product.stockQuantity
                    if (currentStock != null) {
                        val remaining = (currentStock - detail.cartItem.quantity).coerceAtLeast(0)
                        dao.updateProductStock(
                            productId = detail.product.id,
                            newStock = remaining,
                            isAvailable = remaining > 0,
                            updatedAt = now
                        )
                    }
                }

                dao.upsertOrder(order)
                dao.clearActiveCart(uid)

                // Record Order Confirmation + Payment Notification
                dispatchOrderNotification(
                    userId = uid,
                    order = order,
                    title = "Order Placed: ${order.orderNumber}",
                    message = "Your order of ₹${order.finalPayableAmount} has been placed (${paymentStatus.label}). Estimated delivery: ${order.estimatedDelivery}."
                )
                if (paymentStatus == PaymentStatus.PAID || paymentStatus == PaymentStatus.PAID_TEST_MODE) {
                    dispatchOrderNotification(
                        userId = uid,
                        order = order,
                        title = "Payment Confirmed: ${order.orderNumber}",
                        message = "Payment of ₹${order.finalPayableAmount} recorded (${paymentStatus.label}). Ref: $paymentReference"
                    )
                }

                Result.success(order)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun cancelOrderByCustomer(orderId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = currentEffectiveUserId()
            val order = dao.getOrderById(orderId) ?: error("Order not found.")
            if (order.userId != uid) throw SecurityException("Cannot cancel another customer's order.")
            val currentStatus = OrderStatus.fromKey(order.orderStatus)
            if (currentStatus.stepIndex >= OrderStatus.SHIPPED.stepIndex || currentStatus == OrderStatus.CANCELLED) {
                return@withContext Result.failure(IllegalStateException("Orders already shipped or cancelled cannot be cancelled here."))
            }
            val now = System.currentTimeMillis()
            val history = JsonCodec.jsonToStatusHistory(order.statusHistoryJson) + OrderStatusStep(
                statusKey = OrderStatus.CANCELLED.key,
                statusLabel = OrderStatus.CANCELLED.label,
                timestamp = now,
                note = "Cancelled by customer"
            )
            dao.upsertOrder(
                order.copy(
                    orderStatus = OrderStatus.CANCELLED.key,
                    statusHistoryJson = JsonCodec.statusHistoryToJson(history),
                    updatedAt = now
                )
            )
            dispatchOrderNotification(
                userId = order.userId,
                order = order,
                title = "Order Cancelled: ${order.orderNumber}",
                message = "Your order ${order.orderNumber} has been cancelled as requested."
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Reviews (Verified Buyer Check)

    suspend fun submitReview(
        productId: String,
        rating: Int,
        title: String,
        comment: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = currentAuthenticatedUserId()
                ?: return@withContext Result.failure(IllegalStateException("Please sign in to submit a review."))
            val user = dao.getUserById(uid)
                ?: return@withContext Result.failure(IllegalStateException("Customer profile not found."))
            val product = dao.getProductById(productId)
                ?: return@withContext Result.failure(IllegalStateException("Product not found."))
            require(rating in 1..5) { "Select a star rating between 1 and 5." }
            require(comment.trim().length >= 5) { "Please write a brief review (at least 5 characters)." }

            // Verify whether customer actually purchased this product (non-cancelled order)
            val customerOrders = dao.getOrdersForUser(uid)
            val isVerified = customerOrders.any { order ->
                order.orderStatus != OrderStatus.CANCELLED.key &&
                    JsonCodec.jsonToOrderItems(order.itemsJson).any { it.productId == productId }
            }

            val review = ReviewEntity(
                id = "rev_${UUID.randomUUID().toString().take(12)}",
                productId = productId,
                productName = product.name,
                userId = uid,
                reviewerName = user.fullName,
                rating = rating,
                title = title.trim().ifBlank { "${rating}-Star Review" },
                comment = comment.trim(),
                isVerifiedBuyer = isVerified
            )
            dao.insertReview(review)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Wholesale Enquiries

    suspend fun submitWholesaleEnquiry(
        businessName: String,
        contactName: String,
        mobile: String,
        email: String,
        gstNumber: String,
        productId: String,
        requestedQuantity: Int,
        notes: String
    ): Result<WholesaleEnquiryEntity> = withContext(Dispatchers.IO) {
        try {
            val product = dao.getProductById(productId) ?: error("Selected product not found.")
            require(businessName.trim().length >= 2) { "Enter your business or shop name." }
            require(contactName.trim().length >= 2) { "Enter contact person name." }
            require(mobile.trim().length == 10 && mobile.trim().all { it.isDigit() }) { "Enter a valid 10-digit mobile number." }
            require(requestedQuantity >= 5) { "Minimum wholesale enquiry quantity is 5 units." }

            val uid = currentEffectiveUserId()
            val enquiry = WholesaleEnquiryEntity(
                id = "wh_${UUID.randomUUID().toString().take(10)}",
                userId = uid,
                businessName = businessName.trim(),
                contactName = contactName.trim(),
                mobile = mobile.trim(),
                email = email.trim(),
                gstNumber = gstNumber.trim().uppercase(),
                productId = product.id,
                productName = product.name,
                requestedQuantity = requestedQuantity,
                notes = notes.trim(),
                quotedUnitPrice = product.wholesalePrice,
                quotedTotalAmount = product.wholesalePrice?.let { it * requestedQuantity },
                status = WholesaleStatus.PENDING.key
            )
            dao.upsertWholesaleEnquiry(enquiry)
            Result.success(enquiry)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun respondToWholesaleQuote(enquiryId: String, accept: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val uid = currentEffectiveUserId()
            val all = mutableListOf<WholesaleEnquiryEntity>()
            dao.observeAllWholesaleEnquiries().collectOnce { all.addAll(it) }
            val existing = all.firstOrNull { it.id == enquiryId } ?: error("Enquiry not found.")
            if (existing.userId != uid) throw SecurityException("Unauthorized.")
            val nextStatus = if (accept) WholesaleStatus.APPROVED.key else WholesaleStatus.REJECTED.key
            dao.upsertWholesaleEnquiry(existing.copy(status = nextStatus, updatedAt = System.currentTimeMillis()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Newsletter & Notifications

    suspend fun subscribeNewsletter(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val clean = email.trim().lowercase()
            require(clean.contains("@") && clean.contains(".")) { "Enter a valid email address." }
            dao.insertNewsletter(NewsletterEntity(email = clean))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markNotificationsRead() = withContext(Dispatchers.IO) {
        dao.markAllNotificationsRead(currentEffectiveUserId())
    }

    private suspend fun dispatchOrderNotification(
        userId: String,
        order: OrderEntity,
        title: String,
        message: String
    ) {
        val channelStatus = paymentGateway.dispatchWebhookNotificationIfConfigured(
            NotificationWebhookPayload(
                eventType = order.orderStatus,
                orderNumber = order.orderNumber,
                customerEmail = order.customerEmail,
                customerMobile = order.customerMobile,
                title = title,
                message = message
            )
        )
        dao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(12)}",
                userId = userId,
                orderId = order.id,
                title = title,
                message = message,
                channelStatus = channelStatus
            )
        )
    }

    // Product Image Copy (Zero-Permission Photo Picker -> App Private Storage)

    suspend fun importPickedImages(uris: List<Uri>): List<String> = withContext(Dispatchers.IO) {
        val dir = File(appContext.filesDir, "product_images").apply { mkdirs() }
        uris.mapNotNull { uri ->
            runCatching {
                val outFile = File(dir, "prod_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
                appContext.contentResolver.openInputStream(uri)?.use { input ->
                    outFile.outputStream().use { output -> input.copyTo(output) }
                }
                Uri.fromFile(outFile).toString()
            }.getOrNull()
        }
    }

    // ---------------- ADMIN-ONLY OPERATIONS ----------------

    suspend fun adminSaveProduct(product: ProductEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            requireAdminUser()
            require(product.name.isNotBlank()) { "Product name is required." }
            require(product.price > 0) { "Product price must be greater than ₹0." }
            val finalId = product.id.ifBlank { "prod_${UUID.randomUUID().toString().take(10)}" }
            dao.upsertProduct(product.copy(id = finalId, updatedAt = System.currentTimeMillis()))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminDeleteProduct(productId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            requireAdminUser()
            dao.deleteProduct(productId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminSaveCategory(category: CategoryEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            requireAdminUser()
            require(category.name.isNotBlank()) { "Category name is required." }
            val finalId = category.id.ifBlank { "cat_${UUID.randomUUID().toString().take(10)}" }
            dao.upsertCategory(category.copy(id = finalId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminUpdateOrderStatus(
        orderId: String,
        newStatus: OrderStatus,
        note: String,
        newPaymentStatus: PaymentStatus? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            requireAdminUser()
            val order = dao.getOrderById(orderId) ?: error("Order not found.")
            val now = System.currentTimeMillis()
            val history = JsonCodec.jsonToStatusHistory(order.statusHistoryJson) + OrderStatusStep(
                statusKey = newStatus.key,
                statusLabel = newStatus.label,
                timestamp = now,
                note = note.trim().ifBlank { "Updated by Shasvat Textiles Admin" }
            )
            val updatedOrder = order.copy(
                orderStatus = newStatus.key,
                paymentStatus = newPaymentStatus?.key ?: order.paymentStatus,
                statusHistoryJson = JsonCodec.statusHistoryToJson(history),
                updatedAt = now
            )
            dao.upsertOrder(updatedOrder)
            dispatchOrderNotification(
                userId = updatedOrder.userId,
                order = updatedOrder,
                title = "Order ${updatedOrder.orderNumber}: ${newStatus.label}",
                message = note.trim().ifBlank { "Your order status is now ${newStatus.label}." }
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminToggleUserActive(userId: String, isActive: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val admin = requireAdminUser()
            if (admin.id == userId && !isActive) {
                return@withContext Result.failure(IllegalStateException("You cannot deactivate your own Admin account."))
            }
            val target = dao.getUserById(userId) ?: error("User not found.")
            dao.updateUser(target.copy(isActive = isActive))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminSaveDeliverySettings(settings: DeliverySettingsEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            requireAdminUser()
            dao.upsertDeliverySettings(settings.copy(id = 1))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminSaveEligiblePinCode(pinCode: EligiblePinCodeEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            requireAdminUser()
            require(pinCode.pinCode.length == 6 && pinCode.pinCode.all { it.isDigit() }) { "PIN code must be 6 digits." }
            dao.upsertEligiblePinCode(pinCode)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminDeleteEligiblePinCode(pinCode: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            requireAdminUser()
            dao.deleteEligiblePinCode(pinCode)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminSaveOffer(offer: OfferEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            requireAdminUser()
            require(offer.title.isNotBlank()) { "Offer title is required." }
            val finalId = offer.id.ifBlank { "offer_${UUID.randomUUID().toString().take(8)}" }
            dao.upsertOffer(offer.copy(id = finalId))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminDeleteOffer(offerId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            requireAdminUser()
            dao.deleteOffer(offerId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminUpdateWholesaleEnquiry(
        enquiryId: String,
        quotedUnitPrice: Int?,
        status: WholesaleStatus,
        adminRemarks: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            requireAdminUser()
            val all = mutableListOf<WholesaleEnquiryEntity>()
            dao.observeAllWholesaleEnquiries().collectOnce { all.addAll(it) }
            val existing = all.firstOrNull { it.id == enquiryId } ?: error("Enquiry not found.")
            val total = quotedUnitPrice?.let { it * existing.requestedQuantity }
            dao.upsertWholesaleEnquiry(
                existing.copy(
                    quotedUnitPrice = quotedUnitPrice,
                    quotedTotalAmount = total,
                    status = status.key,
                    adminRemarks = adminRemarks.trim(),
                    updatedAt = System.currentTimeMillis()
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun adminSaveBusinessSettings(settings: BusinessSettingsEntity): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            requireAdminUser()
            require(settings.businessName.isNotBlank()) { "Business name is required." }
            dao.upsertBusinessSettings(settings.copy(id = 1))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun <T> Flow<T>.collectOnce(block: (T) -> Unit) {
        var collected = false
        try {
            collect {
                if (!collected) {
                    collected = true
                    block(it)
                    throw AbortFlowException()
                }
            }
        } catch (_: AbortFlowException) {
        }
    }

    private class AbortFlowException : RuntimeException()
}
