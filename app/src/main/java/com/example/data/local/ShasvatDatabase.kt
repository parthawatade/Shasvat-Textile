package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AddressEntity
import com.example.data.model.BusinessSettingsEntity
import com.example.data.model.CartItemEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.DeliverySettingsEntity
import com.example.data.model.EligiblePinCodeEntity
import com.example.data.model.NewsletterEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.OfferEntity
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.model.ReviewEntity
import com.example.data.model.UserEntity
import com.example.data.model.WholesaleEnquiryEntity
import com.example.data.model.WishlistItemEntity

@Database(
    entities = [
        CategoryEntity::class,
        ProductEntity::class,
        CartItemEntity::class,
        WishlistItemEntity::class,
        UserEntity::class,
        AddressEntity::class,
        OrderEntity::class,
        ReviewEntity::class,
        OfferEntity::class,
        WholesaleEnquiryEntity::class,
        DeliverySettingsEntity::class,
        EligiblePinCodeEntity::class,
        BusinessSettingsEntity::class,
        NotificationEntity::class,
        NewsletterEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class ShasvatDatabase : RoomDatabase() {
    abstract fun shasvatDao(): ShasvatDao

    companion object {
        @Volatile
        private var INSTANCE: ShasvatDatabase? = null

        fun getInstance(context: Context): ShasvatDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShasvatDatabase::class.java,
                    "shasvat_textiles.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

object SeedCatalogData {

    val initialCategories = listOf(
        CategoryEntity(
            id = "cat_asli_chadar",
            name = "Asli Solapuri Chadar",
            description = "Signature authentic Solapuri chadar collection",
            drawableName = "img_cat_chadar",
            sortOrder = 1
        ),
        CategoryEntity(
            id = "cat_premium_chadar",
            name = "Premium Solapuri Chadar",
            description = "Everyday comfort Solapuri chadar collection",
            drawableName = "img_cat_chadar",
            sortOrder = 2
        ),
        CategoryEntity(
            id = "cat_satranji",
            name = "Satranji",
            description = "Traditional woven Solapuri cotton floor rugs",
            drawableName = "img_cat_satranji",
            sortOrder = 3
        ),
        CategoryEntity(
            id = "cat_apron",
            name = "Apron",
            description = "Durable woven textile aprons",
            drawableName = "img_cat_apron_napkin",
            sortOrder = 4
        ),
        CategoryEntity(
            id = "cat_towels",
            name = "Solapuri Towels",
            description = "Absorbent Solapuri cotton bath & hand towels",
            drawableName = "img_cat_towels",
            sortOrder = 5
        ),
        CategoryEntity(
            id = "cat_napkins",
            name = "Napkins",
            description = "Everyday woven Solapuri dining & utility napkins",
            drawableName = "img_cat_apron_napkin",
            sortOrder = 6
        )
    )

    // Exact 6 initial products and exact prices. No invented specs, ratings, discounts, or stock quantities.
    val initialProducts = listOf(
        ProductEntity(
            id = "prod_asli_solapuri_chadar",
            name = "Asli Solapuri Chadar",
            description = "Authentic Solapuri Chadar woven in the tradition of Solapur. Detailed fabric specifications, colour variants, and sizes can be configured in the Admin Dashboard.",
            price = 899,
            originalPrice = null,
            wholesalePrice = null,
            brand = "Shasvat Textiles",
            categoryId = "cat_asli_chadar",
            categoryName = "Asli Solapuri Chadar",
            imageDrawablesJson = "[\"img_cat_chadar\"]",
            uploadedImageUrisJson = "[]",
            colorsJson = "[]",
            sizesJson = "[]",
            fabricDetails = "",
            specifications = "",
            stockQuantity = null,
            isAvailable = true,
            deliveryEstimate = "Within 24 hours to 2 days",
            isRushEligible = true,
            isTrending = true,
            isBestSeller = true,
            isNewArrival = false,
            createdAt = 1_700_000_001_000L,
            updatedAt = 1_700_000_001_000L
        ),
        ProductEntity(
            id = "prod_premium_solapuri_chadar",
            name = "Premium Solapuri Chadar",
            description = "Premium Solapuri Chadar crafted for everyday comfort. Detailed fabric specifications, colour variants, and sizes can be configured in the Admin Dashboard.",
            price = 550,
            originalPrice = null,
            wholesalePrice = null,
            brand = "Shasvat Textiles",
            categoryId = "cat_premium_chadar",
            categoryName = "Premium Solapuri Chadar",
            imageDrawablesJson = "[\"img_cat_chadar\"]",
            uploadedImageUrisJson = "[]",
            colorsJson = "[]",
            sizesJson = "[]",
            fabricDetails = "",
            specifications = "",
            stockQuantity = null,
            isAvailable = true,
            deliveryEstimate = "Within 24 hours to 2 days",
            isRushEligible = true,
            isTrending = true,
            isBestSeller = true,
            isNewArrival = true,
            createdAt = 1_700_000_002_000L,
            updatedAt = 1_700_000_002_000L
        ),
        ProductEntity(
            id = "prod_satranji_rug",
            name = "Satranji (Rug)",
            description = "Classic Solapuri Satranji floor rug. Detailed dimensions, fabric specifications, and colour options can be configured in the Admin Dashboard.",
            price = 599,
            originalPrice = null,
            wholesalePrice = null,
            brand = "Shasvat Textiles",
            categoryId = "cat_satranji",
            categoryName = "Satranji",
            imageDrawablesJson = "[\"img_cat_satranji\"]",
            uploadedImageUrisJson = "[]",
            colorsJson = "[]",
            sizesJson = "[]",
            fabricDetails = "",
            specifications = "",
            stockQuantity = null,
            isAvailable = true,
            deliveryEstimate = "Within 24 hours to 2 days",
            isRushEligible = true,
            isTrending = true,
            isBestSeller = false,
            isNewArrival = true,
            createdAt = 1_700_000_003_000L,
            updatedAt = 1_700_000_003_000L
        ),
        ProductEntity(
            id = "prod_apron",
            name = "Apron",
            description = "Durable woven textile Apron from Shasvat Textiles. Detailed fabric specifications and size variations can be configured in the Admin Dashboard.",
            price = 629,
            originalPrice = null,
            wholesalePrice = null,
            brand = "Shasvat Textiles",
            categoryId = "cat_apron",
            categoryName = "Apron",
            imageDrawablesJson = "[\"img_cat_apron_napkin\"]",
            uploadedImageUrisJson = "[]",
            colorsJson = "[]",
            sizesJson = "[]",
            fabricDetails = "",
            specifications = "",
            stockQuantity = null,
            isAvailable = true,
            deliveryEstimate = "Within 24 hours to 2 days",
            isRushEligible = true,
            isTrending = false,
            isBestSeller = false,
            isNewArrival = true,
            createdAt = 1_700_000_004_000L,
            updatedAt = 1_700_000_004_000L
        ),
        ProductEntity(
            id = "prod_solapuri_towels",
            name = "Solapuri Towels",
            description = "Authentic Solapuri Towels designed for reliable everyday absorbency. Detailed specifications, colours, and sizes can be configured in the Admin Dashboard.",
            price = 399,
            originalPrice = null,
            wholesalePrice = null,
            brand = "Shasvat Textiles",
            categoryId = "cat_towels",
            categoryName = "Solapuri Towels",
            imageDrawablesJson = "[\"img_cat_towels\"]",
            uploadedImageUrisJson = "[]",
            colorsJson = "[]",
            sizesJson = "[]",
            fabricDetails = "",
            specifications = "",
            stockQuantity = null,
            isAvailable = true,
            deliveryEstimate = "Within 24 hours to 2 days",
            isRushEligible = true,
            isTrending = true,
            isBestSeller = true,
            isNewArrival = false,
            createdAt = 1_700_000_005_000L,
            updatedAt = 1_700_000_005_000L
        ),
        ProductEntity(
            id = "prod_napkins",
            name = "Napkins",
            description = "Woven Solapuri Napkins for kitchen and dining use. Detailed specifications, colours, and pack sizes can be configured in the Admin Dashboard.",
            price = 250,
            originalPrice = null,
            wholesalePrice = null,
            brand = "Shasvat Textiles",
            categoryId = "cat_napkins",
            categoryName = "Napkins",
            imageDrawablesJson = "[\"img_cat_apron_napkin\"]",
            uploadedImageUrisJson = "[]",
            colorsJson = "[]",
            sizesJson = "[]",
            fabricDetails = "",
            specifications = "",
            stockQuantity = null,
            isAvailable = true,
            deliveryEstimate = "Within 24 hours to 2 days",
            isRushEligible = true,
            isTrending = false,
            isBestSeller = true,
            isNewArrival = true,
            createdAt = 1_700_000_006_000L,
            updatedAt = 1_700_000_006_000L
        )
    )

    val initialOffers = listOf(
        OfferEntity(
            id = "offer_1_above_500",
            title = "Offer 1: ₹50 Instant Discount",
            description = "Save ₹50 on retail orders above ₹500. Cannot be combined with other offers.",
            minOrderAmount = 500,
            discountAmount = 50,
            includesFreeStandardDelivery = false,
            freeGiftMaxPrice = null,
            expiryDateMillis = null,
            isActive = true,
            isSystemDefault = true
        ),
        OfferEntity(
            id = "offer_2_above_1000",
            title = "Offer 2: ₹60 Instant Discount",
            description = "Save ₹60 on retail orders above ₹1,000. Cannot be combined with other offers.",
            minOrderAmount = 1000,
            discountAmount = 60,
            includesFreeStandardDelivery = false,
            freeGiftMaxPrice = null,
            expiryDateMillis = null,
            isActive = true,
            isSystemDefault = true
        ),
        OfferEntity(
            id = "offer_3_above_2500",
            title = "Offer 3: Free Standard Delivery",
            description = "Enjoy free standard delivery on orders above ₹2,500.",
            minOrderAmount = 2500,
            discountAmount = 0,
            includesFreeStandardDelivery = true,
            freeGiftMaxPrice = null,
            expiryDateMillis = null,
            isActive = true,
            isSystemDefault = true
        ),
        OfferEntity(
            id = "offer_4_above_5000",
            title = "Offer 4: Free Standard Delivery + ₹100 Off",
            description = "Get free standard delivery plus ₹100 discount on orders above ₹5,000.",
            minOrderAmount = 5000,
            discountAmount = 100,
            includesFreeStandardDelivery = true,
            freeGiftMaxPrice = null,
            expiryDateMillis = null,
            isActive = true,
            isSystemDefault = true
        ),
        OfferEntity(
            id = "offer_5_above_9000",
            title = "Offer 5: Free Standard Delivery + Free Gift (Under ₹800)",
            description = "Get free standard delivery plus one eligible free gift item priced under ₹800 on orders above ₹9,000.",
            minOrderAmount = 9000,
            discountAmount = 0,
            includesFreeStandardDelivery = true,
            freeGiftMaxPrice = 800,
            expiryDateMillis = null,
            isActive = true,
            isSystemDefault = true
        )
    )

    val initialDeliverySettings = DeliverySettingsEntity(
        id = 1,
        standardCharge = 50,
        freeDeliveryMinSubtotal = 2000,
        rushExtraCharge = 250,
        standardEstimateText = "Within 24 hours to 2 days",
        rushEstimateText = "Within 2 hours"
    )

    val initialRushPinCodes = listOf(
        EligiblePinCodeEntity(pinCode = "413001", areaLabel = "Solapur Central Zone 1", isRushAvailable = true),
        EligiblePinCodeEntity(pinCode = "413002", areaLabel = "Solapur Central Zone 2", isRushAvailable = true),
        EligiblePinCodeEntity(pinCode = "413003", areaLabel = "Solapur North Zone", isRushAvailable = true),
        EligiblePinCodeEntity(pinCode = "413004", areaLabel = "Solapur East Zone", isRushAvailable = true),
        EligiblePinCodeEntity(pinCode = "411001", areaLabel = "Pune City Express Zone", isRushAvailable = true),
        EligiblePinCodeEntity(pinCode = "400001", areaLabel = "Mumbai South Express Zone", isRushAvailable = true)
    )

    val initialBusinessSettings = BusinessSettingsEntity(
        id = 1,
        businessName = "Shasvat Textiles",
        gstin = "",
        stateName = "Maharashtra",
        defaultGstRatePercent = 5.0,
        invoicePrefix = "ST-INV",
        upiMerchantId = "",
        upiMerchantName = "Shasvat Textiles"
    )
}
