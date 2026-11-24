package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.domain.PricingEngine
import com.example.data.local.SeedCatalogData
import com.example.data.local.ShasvatDatabase
import com.example.data.model.CartItemDetail
import com.example.data.model.CartItemEntity
import com.example.data.model.DeliveryMethod
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.PriceBracket
import com.example.data.model.ProductEntity
import com.example.data.model.ShippingAddress
import com.example.data.model.SortOption
import com.example.data.model.UserRole
import com.example.data.model.WholesaleStatus
import com.example.data.model.toDomain
import com.example.data.preferences.SessionPreferences
import com.example.data.repository.ShasvatRepository
import com.example.viewmodel.StorefrontViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ShasvatPricingAndRepositoryTest {

    private lateinit var context: Context
    private lateinit var db: ShasvatDatabase
    private lateinit var prefs: SessionPreferences
    private lateinit var repository: ShasvatRepository

    private val deliverySettings = SeedCatalogData.initialDeliverySettings
    private val offers = SeedCatalogData.initialOffers

    @Before
    fun setUp() = runTest {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, ShasvatDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        prefs = SessionPreferences(context)
        prefs.setActiveUserId(SessionPreferences.GUEST_USER_ID)
        prefs.setSelectedPinCode("")
        prefs.setSelectedOfferId(null)
        repository = ShasvatRepository(context, db.shasvatDao(), prefs)
        repository.ensureSeeded()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun sampleShippingAddress(pinCode: String = "413001", state: String = "Maharashtra") =
        ShippingAddress(
            houseNumber = "12B",
            building = "Textile Residency",
            street = "Station Road",
            area = "Civil Lines",
            city = "Solapur",
            state = state,
            pinCode = pinCode,
            deliveryInstructions = "Call on arrival"
        )

    private fun makeCartDetail(price: Int, quantity: Int, id: String = "p_$price"): CartItemDetail {
        val product = ProductEntity(
            id = id,
            name = "Test Product $price",
            description = "Test",
            price = price,
            brand = "Shasvat Textiles",
            categoryId = "cat_asli_chadar",
            categoryName = "Asli Solapuri Chadar",
            imageDrawablesJson = "[\"img_cat_chadar\"]"
        ).toDomain()
        val item = CartItemEntity(
            id = "c_$id",
            userId = "guest",
            productId = id,
            quantity = quantity
        )
        return CartItemDetail(item, product)
    }

    // TEST 1: Exact initial 6 products & prices, zero invented specs/ratings/discounts/stock
    @Test
    fun `seed catalog preserves exact six products and prices without invented metadata`() = runTest {
        val products = repository.productsFlow.first()
        val categories = repository.categoriesFlow.first()

        assertEquals(6, categories.size)
        assertEquals(6, products.size)

        val priceMap = products.associate { it.name to it.price }
        assertEquals(899, priceMap["Asli Solapuri Chadar"])
        assertEquals(550, priceMap["Premium Solapuri Chadar"])
        assertEquals(599, priceMap["Satranji (Rug)"])
        assertEquals(629, priceMap["Apron"])
        assertEquals(399, priceMap["Solapuri Towels"])
        assertEquals(250, priceMap["Napkins"])

        products.forEach { product ->
            assertNull("No invented originalPrice discount for ${product.name}", product.originalPrice)
            assertNull("No invented stockQuantity for ${product.name}", product.stockQuantity)
            assertEquals("No invented rating for ${product.name}", 0.0, product.averageRating, 0.001)
            assertEquals("No invented reviews for ${product.name}", 0, product.reviewCount)
            assertEquals("No invented fabricDetails for ${product.name}", "", product.fabricDetails)
            assertEquals("No invented specifications for ${product.name}", "", product.specifications)
        }
    }

    // TEST 7, 8, 9, 10: Pricing engine offers 1-5, non-stackable, bag charge ₹20, delivery & rush rules
    @Test
    fun `pricing engine enforces shopping bag charge, delivery thresholds, rush rules, and non-stackable offers`() {
        // Subtotal ₹400 (<=500): no offer, ₹50 standard delivery, ₹20 bag => ₹470
        val under500 = PricingEngine.calculatePriceBreakdown(
            cartItems = listOf(makeCartDetail(200, 2)),
            offers = offers,
            deliverySettings = deliverySettings,
            preferredOfferId = null,
            deliveryMethod = DeliveryMethod.STANDARD,
            isPinRushEligible = false,
            selectedFreeGift = null
        )
        assertNull(under500.appliedOffer)
        assertEquals(0, under500.offerDiscount)
        assertEquals(50, under500.totalDeliveryCharge)
        assertEquals(20, under500.shoppingBagCharge)
        assertEquals(470, under500.finalPayableAmount)

        // Subtotal ₹629 (>500): Offer 1 (-₹50), ₹50 standard, ₹20 bag => 629 - 50 + 50 + 20 = ₹649
        val offer1 = PricingEngine.calculatePriceBreakdown(
            cartItems = listOf(makeCartDetail(629, 1)),
            offers = offers,
            deliverySettings = deliverySettings,
            preferredOfferId = null,
            deliveryMethod = DeliveryMethod.STANDARD,
            isPinRushEligible = false,
            selectedFreeGift = null
        )
        assertEquals("offer_1_above_500", offer1.appliedOffer?.id)
        assertEquals(50, offer1.offerDiscount)
        assertEquals(50, offer1.standardDeliveryCharge)
        assertEquals(20, offer1.shoppingBagCharge)
        assertEquals(649, offer1.finalPayableAmount)

        // Subtotal ₹1200 (>1000): Offer 2 (-₹60), ₹50 standard, ₹20 bag => 1200 - 60 + 50 + 20 = ₹1210
        val offer2 = PricingEngine.calculatePriceBreakdown(
            cartItems = listOf(makeCartDetail(600, 2)),
            offers = offers,
            deliverySettings = deliverySettings,
            preferredOfferId = null,
            deliveryMethod = DeliveryMethod.STANDARD,
            isPinRushEligible = false,
            selectedFreeGift = null
        )
        assertEquals("offer_2_above_1000", offer2.appliedOffer?.id)
        assertEquals(60, offer2.offerDiscount)
        assertEquals(50, offer2.standardDeliveryCharge)
        assertEquals(1210, offer2.finalPayableAmount)

        // Subtotal ₹2550 (>2000 free std delivery): Offer 2 (-₹60) beats Offer 3 (delivery already free), free delivery never double-counted
        val offer3 = PricingEngine.calculatePriceBreakdown(
            cartItems = listOf(makeCartDetail(850, 3)),
            offers = offers,
            deliverySettings = deliverySettings,
            preferredOfferId = null,
            deliveryMethod = DeliveryMethod.STANDARD,
            isPinRushEligible = true,
            selectedFreeGift = null
        )
        assertEquals("offer_2_above_1000", offer3.appliedOffer?.id)
        assertEquals(60, offer3.offerDiscount)
        assertEquals(0, offer3.standardDeliveryCharge)
        assertEquals(0, offer3.totalDeliveryCharge)
        assertEquals(2510, offer3.finalPayableAmount)

        // Subtotal ₹5500 (>5000): Offer 4 (-₹100 + free standard) + Rush on eligible PIN (+₹250) + ₹20 bag = 5670
        val offer4Rush = PricingEngine.calculatePriceBreakdown(
            cartItems = listOf(makeCartDetail(1100, 5)),
            offers = offers,
            deliverySettings = deliverySettings,
            preferredOfferId = null,
            deliveryMethod = DeliveryMethod.RUSH,
            isPinRushEligible = true,
            selectedFreeGift = null
        )
        assertEquals("offer_4_above_5000", offer4Rush.appliedOffer?.id)
        assertEquals(100, offer4Rush.offerDiscount)
        assertEquals(0, offer4Rush.standardDeliveryCharge)
        assertEquals(250, offer4Rush.rushDeliveryCharge)
        assertEquals(250, offer4Rush.totalDeliveryCharge)
        assertEquals(5670, offer4Rush.finalPayableAmount)

        // Subtotal ₹9200 (>9000): Offer 5 (free gift under ₹800 + free standard delivery)
        val eligibleGift = makeCartDetail(599, 1, "gift_satranji").product
        val offer5 = PricingEngine.calculatePriceBreakdown(
            cartItems = listOf(makeCartDetail(920, 10)),
            offers = offers,
            deliverySettings = deliverySettings,
            preferredOfferId = null,
            deliveryMethod = DeliveryMethod.STANDARD,
            isPinRushEligible = false,
            selectedFreeGift = eligibleGift
        )
        assertEquals("offer_5_above_9000", offer5.appliedOffer?.id)
        assertTrue(offer5.isFreeGiftEligible)
        assertEquals(800, offer5.freeGiftMaxPrice)
        assertEquals("gift_satranji", offer5.selectedFreeGift?.id)
        assertEquals(9220, offer5.finalPayableAmount)

        // Ineligible gift >= ₹800 must be rejected
        val expensiveGift = makeCartDetail(899, 1, "gift_asli").product
        val offer5InvalidGift = PricingEngine.calculatePriceBreakdown(
            cartItems = listOf(makeCartDetail(920, 10)),
            offers = offers,
            deliverySettings = deliverySettings,
            preferredOfferId = null,
            deliveryMethod = DeliveryMethod.STANDARD,
            isPinRushEligible = false,
            selectedFreeGift = expensiveGift
        )
        assertNull(offer5InvalidGift.selectedFreeGift)
    }

    // GST tax math: Intra-state CGST+SGST vs Inter-state IGST
    @Test
    fun `gst calculation splits CGST SGST intra-state and IGST inter-state`() {
        val intra = PricingEngine.calculateGstTax(
            finalPayableAmount = 1050,
            gstRatePercent = 5.0,
            sellerState = "Maharashtra",
            customerState = "Maharashtra"
        )
        assertEquals(1000.0, intra.taxableAmount, 0.01)
        assertEquals(25.0, intra.cgstAmount, 0.01)
        assertEquals(25.0, intra.sgstAmount, 0.01)
        assertEquals(0.0, intra.igstAmount, 0.01)

        val inter = PricingEngine.calculateGstTax(
            finalPayableAmount = 1050,
            gstRatePercent = 5.0,
            sellerState = "Maharashtra",
            customerState = "Karnataka"
        )
        assertEquals(1000.0, inter.taxableAmount, 0.01)
        assertEquals(0.0, inter.cgstAmount, 0.01)
        assertEquals(0.0, inter.sgstAmount, 0.01)
        assertEquals(50.0, inter.igstAmount, 0.01)
    }

    // TEST 2, 3: Search, filters, sorting & Clear All
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `storefront search and multi-filter pipeline works and clears cleanly`() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val store = androidx.lifecycle.ViewModelStore()
        val vm = androidx.lifecycle.ViewModelProvider(store, StorefrontViewModel.factory(repository))[StorefrontViewModel::class.java]
        try {
            val all = repository.productsFlow.first()
            assertEquals(6, all.size)

            vm.setSearchQuery("Chadar")
            val chadarResults = vm.filteredProducts.first { it.size == 2 }
            assertEquals(2, chadarResults.size)

            vm.togglePriceBracket(PriceBracket.RANGE_500_1000)
            vm.setSortOption(SortOption.PRICE_LOW_TO_HIGH)
            val sortedChadars = vm.filteredProducts.first { it.size == 2 && it.first().price == 550 }
            assertEquals(550, sortedChadars[0].price)
            assertEquals(899, sortedChadars[1].price)

            vm.clearAllFilters()
            val resetAll = vm.filteredProducts.first { it.size == 6 }
            assertEquals(6, resetAll.size)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    // TEST 5, 6, 9, 10, 11, 13: Wishlist, Cart, PIN check, Rush rejection, COD Checkout & Idempotency
    @Test
    fun `customer wishlist, cart persistence, PIN rush check, and COD checkout with idempotency`() = runTest {
        val signupRes = repository.signUp(
            fullName = "Aarav Deshmukh",
            email = "aarav@example.com",
            mobile = "9876543210",
            password = "password123",
            asInitialAdmin = false
        )
        assertTrue(signupRes.isSuccess)
        assertEquals(UserRole.CUSTOMER.key, signupRes.getOrNull()?.role)

        // Wishlist toggle
        assertTrue(repository.toggleWishlist("prod_napkins"))
        assertEquals(setOf("prod_napkins"), repository.wishlistIdsFlow.first())

        // Add Asli Solapuri Chadar (₹899) x 1
        assertTrue(repository.addToCart("prod_asli_solapuri_chadar", 1, "", "").isSuccess)
        var cart = repository.cartDetailsFlow.first()
        assertEquals(1, cart.size)

        // Increase quantity to 2 (₹1798)
        assertTrue(repository.updateCartQuantity(cart.first().cartItem.id, 2).isSuccess)
        cart = repository.cartDetailsFlow.first()
        assertEquals(2, cart.first().cartItem.quantity)

        // Save for later and move back
        repository.toggleSaveForLater(cart.first().cartItem.id, true)
        assertTrue(repository.cartDetailsFlow.first().first().cartItem.isSavedForLater)
        repository.toggleSaveForLater(cart.first().cartItem.id, false)
        assertFalse(repository.cartDetailsFlow.first().first().cartItem.isSavedForLater)

        // PIN eligibility check
        assertTrue(repository.isPinRushEligible("413001"))
        assertFalse(repository.isPinRushEligible("999999"))

        // Rush delivery rejected for ineligible PIN
        val badRush = repository.placeOrder(
            customerName = "Aarav Deshmukh",
            customerMobile = "9876543210",
            customerEmail = "aarav@example.com",
            alternateMobile = "",
            shippingAddress = sampleShippingAddress(pinCode = "999999"),
            deliveryMethod = DeliveryMethod.RUSH,
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
            preferredOfferId = null,
            selectedFreeGiftId = null,
            idempotencyKey = "idem_bad_rush",
            useGatewayTestMode = false,
            gatewayTestCredential = ""
        )
        assertTrue(badRush.isFailure)

        // Place valid COD order on Rush-eligible PIN 413001 with Rush delivery
        // ₹1798 subtotal -> Offer 2 (-₹60) -> Standard ₹50 + Rush ₹250 = ₹300 -> Bag ₹20 -> ₹2058
        val codOrderRes = repository.placeOrder(
            customerName = "Aarav Deshmukh",
            customerMobile = "9876543210",
            customerEmail = "aarav@example.com",
            alternateMobile = "",
            shippingAddress = sampleShippingAddress(pinCode = "413001"),
            deliveryMethod = DeliveryMethod.RUSH,
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
            preferredOfferId = null,
            selectedFreeGiftId = null,
            idempotencyKey = "idem_cod_1",
            useGatewayTestMode = false,
            gatewayTestCredential = ""
        )
        assertTrue(codOrderRes.isSuccess)
        val order = codOrderRes.getOrThrow()
        assertEquals(1798, order.subtotal)
        assertEquals(60, order.discountAmount)
        assertEquals(300, order.deliveryCharge)
        assertEquals(20, order.shoppingBagCharge)
        assertEquals(2058, order.finalPayableAmount)
        assertEquals(PaymentStatus.COD_PENDING.key, order.paymentStatus)
        assertEquals(OrderStatus.ORDER_PLACED.key, order.orderStatus)
        assertTrue(order.invoiceNumber.startsWith("ST-INV-"))

        // Cart cleared after successful order
        assertTrue(repository.cartDetailsFlow.first().isEmpty())

        // Duplicate idempotency key returns same order without creating a second one
        val duplicateRes = repository.placeOrder(
            customerName = "Aarav Deshmukh",
            customerMobile = "9876543210",
            customerEmail = "aarav@example.com",
            alternateMobile = "",
            shippingAddress = sampleShippingAddress(pinCode = "413001"),
            deliveryMethod = DeliveryMethod.RUSH,
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
            preferredOfferId = null,
            selectedFreeGiftId = null,
            idempotencyKey = "idem_cod_1",
            useGatewayTestMode = false,
            gatewayTestCredential = ""
        )
        assertEquals(order.id, duplicateRes.getOrThrow().id)
        assertEquals(1, repository.userOrdersFlow.first().size)

        // Verified buyer review check
        assertTrue(repository.submitReview("prod_asli_solapuri_chadar", 5, "Genuine weave", "Excellent Solapuri chadar quality.").isSuccess)
        assertTrue(repository.submitReview("prod_napkins", 4, "Soft weave", "Good utility napkins for kitchen.").isSuccess)
        val chadarReviews = repository.observeReviewsForProduct("prod_asli_solapuri_chadar").first()
        val napkinReviews = repository.observeReviewsForProduct("prod_napkins").first()
        assertTrue(chadarReviews.first().isVerifiedBuyer)
        assertFalse(napkinReviews.first().isVerifiedBuyer)
    }

    // TEST 12: Online Payment Gateway Sandbox Test Mode success & failure
    @Test
    fun `online payment gateway test mode handles failure and success cleanly`() = runTest {
        repository.signUp("Meera Kulkarni", "meera@example.com", "9822001122", "pass1234", false)
        repository.addToCart("prod_premium_solapuri_chadar", 1, "", "")

        val failedGw = repository.placeOrder(
            customerName = "Meera Kulkarni",
            customerMobile = "9822001122",
            customerEmail = "meera@example.com",
            alternateMobile = "",
            shippingAddress = sampleShippingAddress(),
            deliveryMethod = DeliveryMethod.STANDARD,
            paymentMethod = PaymentMethod.UPI,
            preferredOfferId = null,
            selectedFreeGiftId = null,
            idempotencyKey = "idem_gw_fail",
            useGatewayTestMode = true,
            gatewayTestCredential = "failure@razorpay"
        )
        assertTrue(failedGw.isFailure)
        assertEquals(1, repository.cartDetailsFlow.first().size)
        assertTrue(repository.userOrdersFlow.first().isEmpty())

        val okGw = repository.placeOrder(
            customerName = "Meera Kulkarni",
            customerMobile = "9822001122",
            customerEmail = "meera@example.com",
            alternateMobile = "",
            shippingAddress = sampleShippingAddress(),
            deliveryMethod = DeliveryMethod.STANDARD,
            paymentMethod = PaymentMethod.UPI,
            preferredOfferId = null,
            selectedFreeGiftId = null,
            idempotencyKey = "idem_gw_ok",
            useGatewayTestMode = true,
            gatewayTestCredential = "success@razorpay"
        )
        assertTrue(okGw.isSuccess)
        val order = okGw.getOrThrow()
        assertEquals(PaymentStatus.PAID_TEST_MODE.key, order.paymentStatus)
        assertTrue(order.paymentReference.contains("success@razorpay"))
    }

    // TEST 14, 15, 16, 17, 18: Role-based Admin security, status updates, product creation, wholesale & GST invoice
    @Test
    fun `role guard blocks customers from admin ops and admin lifecycle updates customer orders and wholesale`() = runTest {
        // 1. Customer signs up and places order
        repository.signUp("Rohan Joshi", "rohan@example.com", "9811112222", "secret12", false)
        repository.addToCart("prod_solapuri_towels", 2, "", "")
        val customerOrder = repository.placeOrder(
            customerName = "Rohan Joshi",
            customerMobile = "9811112222",
            customerEmail = "rohan@example.com",
            alternateMobile = "",
            shippingAddress = sampleShippingAddress(state = "Karnataka"),
            deliveryMethod = DeliveryMethod.STANDARD,
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
            preferredOfferId = null,
            selectedFreeGiftId = null,
            idempotencyKey = "idem_rohan_1",
            useGatewayTestMode = false,
            gatewayTestCredential = ""
        ).getOrThrow()
        // Inter-state Karnataka vs seller Maharashtra -> IGST > 0, CGST/SGST == 0
        assertEquals(0.0, customerOrder.cgstAmount, 0.001)
        assertEquals(0.0, customerOrder.sgstAmount, 0.001)
        assertTrue(customerOrder.igstAmount > 0.0)

        // Customer submits wholesale enquiry
        val enquiry = repository.submitWholesaleEnquiry(
            businessName = "Joshi Traders",
            contactName = "Rohan Joshi",
            mobile = "9811112222",
            email = "rohan@example.com",
            gstNumber = "29ABCDE1234F1Z5",
            productId = "prod_asli_solapuri_chadar",
            requestedQuantity = 50,
            notes = "Monthly wholesale supply"
        ).getOrThrow()
        assertEquals(WholesaleStatus.PENDING.key, enquiry.status)

        // Customer MUST be blocked from Admin actions
        val unauthorizedStatus = repository.adminUpdateOrderStatus(customerOrder.id, OrderStatus.SHIPPED, "Unauthorized")
        assertTrue(unauthorizedStatus.isFailure)
        assertTrue(unauthorizedStatus.exceptionOrNull() is SecurityException)

        val unauthorizedProduct = repository.adminSaveProduct(
            ProductEntity(
                id = "",
                name = "Unauthorized Product",
                description = "Blocked",
                price = 999,
                brand = "Test",
                categoryId = "cat_asli_chadar",
                categoryName = "Asli Solapuri Chadar",
                imageDrawablesJson = "[\"img_cat_chadar\"]"
            )
        )
        assertTrue(unauthorizedProduct.isFailure)
        assertTrue(unauthorizedProduct.exceptionOrNull() is SecurityException)

        // 2. Claim First-Run Admin account
        repository.logout()
        val adminSignup = repository.signUp("Shasvat Store Admin", "admin@shasvat.com", "9900000001", "adminPass99", asInitialAdmin = true)
        assertTrue(adminSignup.isSuccess)
        assertEquals(UserRole.ADMIN.key, adminSignup.getOrThrow().role)

        // Second admin claim must be rejected
        val secondAdminAttempt = repository.signUp("Fake Admin", "fake@shasvat.com", "9900000002", "adminPass99", asInitialAdmin = true)
        assertTrue(secondAdminAttempt.isFailure)
        assertTrue(secondAdminAttempt.exceptionOrNull() is SecurityException)

        // Admin updates order status through Out for Delivery -> Delivered
        assertTrue(repository.adminUpdateOrderStatus(customerOrder.id, OrderStatus.ORDER_CONFIRMED, "Confirmed at dispatch").isSuccess)
        assertTrue(repository.adminUpdateOrderStatus(customerOrder.id, OrderStatus.SHIPPED, "Dispatched via courier").isSuccess)
        assertTrue(repository.adminUpdateOrderStatus(customerOrder.id, OrderStatus.DELIVERED, "Delivered to customer", PaymentStatus.PAID_COD).isSuccess)

        // Admin creates new product
        assertTrue(
            repository.adminSaveProduct(
                ProductEntity(
                    id = "prod_jacquard_chadar",
                    name = "Jacquard Heritage Chadar",
                    description = "Admin added textile",
                    price = 1199,
                    brand = "Shasvat Textiles",
                    categoryId = "cat_asli_chadar",
                    categoryName = "Asli Solapuri Chadar",
                    imageDrawablesJson = "[\"img_cat_chadar\"]",
                    uploadedImageUrisJson = "[\"file:///tmp/sample_upload.jpg\"]",
                    stockQuantity = 25
                )
            ).isSuccess
        )
        assertEquals(7, repository.productsFlow.first().size)

        // Admin quotes wholesale enquiry
        assertTrue(repository.adminUpdateWholesaleEnquiry(enquiry.id, 690, WholesaleStatus.QUOTED, "Bulk quote valid 7 days").isSuccess)

        // 3. Customer logs back in and verifies live status + accepts wholesale quote
        repository.logout()
        assertTrue(repository.login("rohan@example.com", "secret12").isSuccess)
        val liveOrder = repository.observeOrderById(customerOrder.id).first()!!
        assertEquals(OrderStatus.DELIVERED.key, liveOrder.orderStatus)
        assertEquals(PaymentStatus.PAID_COD.key, liveOrder.paymentStatus)

        val liveNotifs = repository.userNotificationsFlow.first()
        assertTrue(liveNotifs.any { it.title.contains("Delivered") })

        assertTrue(repository.respondToWholesaleQuote(enquiry.id, accept = true).isSuccess)
        val updatedEnquiry = repository.userWholesaleEnquiriesFlow.first().first { it.id == enquiry.id }
        assertEquals(WholesaleStatus.APPROVED.key, updatedEnquiry.status)
        assertEquals(690 * 50, updatedEnquiry.quotedTotalAmount)
    }
}
