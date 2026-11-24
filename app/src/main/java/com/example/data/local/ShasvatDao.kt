package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface ShasvatDao {

    // Categories
    @Query("SELECT * FROM categories ORDER BY sortOrder ASC")
    fun observeCategories(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategory(id: String)

    // Products
    @Query("SELECT * FROM products ORDER BY createdAt ASC")
    fun observeProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    fun observeProductById(id: String): Flow<ProductEntity?>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: String): ProductEntity?

    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProducts(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: String)

    @Query("UPDATE products SET stockQuantity = :newStock, isAvailable = :isAvailable, updatedAt = :updatedAt WHERE id = :productId")
    suspend fun updateProductStock(productId: String, newStock: Int?, isAvailable: Boolean, updatedAt: Long)

    // Reviews
    @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
    fun observeAllReviews(): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE productId = :productId ORDER BY createdAt DESC")
    fun observeReviewsForProduct(productId: String): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity)

    // Cart
    @Query("SELECT * FROM cart_items WHERE userId = :userId ORDER BY addedAt DESC")
    fun observeCartItems(userId: String): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    suspend fun getCartItems(userId: String): List<CartItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCartItem(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: String)

    @Query("DELETE FROM cart_items WHERE userId = :userId AND isSavedForLater = 0")
    suspend fun clearActiveCart(userId: String)

    @Query("UPDATE cart_items SET userId = :newUserId WHERE userId = :oldUserId")
    suspend fun migrateCartUser(oldUserId: String, newUserId: String)

    // Wishlist
    @Query("SELECT * FROM wishlist_items WHERE userId = :userId ORDER BY addedAt DESC")
    fun observeWishlistItems(userId: String): Flow<List<WishlistItemEntity>>

    @Query("SELECT * FROM wishlist_items WHERE userId = :userId")
    suspend fun getWishlistItems(userId: String): List<WishlistItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWishlistItem(item: WishlistItemEntity)

    @Query("DELETE FROM wishlist_items WHERE userId = :userId AND productId = :productId")
    suspend fun removeWishlistItem(userId: String, productId: String)

    @Query("UPDATE wishlist_items SET userId = :newUserId WHERE userId = :oldUserId")
    suspend fun migrateWishlistUser(oldUserId: String, newUserId: String)

    // Users & Auth
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE LOWER(email) = LOWER(:email) LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE mobile = :mobile LIMIT 1")
    suspend fun getUserByMobile(mobile: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'")
    fun observeAdminCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'")
    suspend fun getAdminCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    // Addresses
    @Query("SELECT * FROM addresses WHERE userId = :userId ORDER BY isDefault DESC")
    fun observeAddressesForUser(userId: String): Flow<List<AddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAddress(address: AddressEntity)

    @Query("UPDATE addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultAddress(userId: String)

    @Query("DELETE FROM addresses WHERE id = :id")
    suspend fun deleteAddress(id: String)

    // Orders
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeOrdersForUser(userId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    fun observeOrderById(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE idempotencyKey = :key LIMIT 1")
    suspend fun getOrderByIdempotencyKey(key: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE userId = :userId")
    suspend fun getOrdersForUser(userId: String): List<OrderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOrder(order: OrderEntity)

    // Offers
    @Query("SELECT * FROM offers ORDER BY minOrderAmount ASC")
    fun observeOffers(): Flow<List<OfferEntity>>

    @Query("SELECT * FROM offers")
    suspend fun getAllOffers(): List<OfferEntity>

    @Query("SELECT COUNT(*) FROM offers")
    suspend fun getOfferCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOffers(offers: List<OfferEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOffer(offer: OfferEntity)

    @Query("DELETE FROM offers WHERE id = :id")
    suspend fun deleteOffer(id: String)

    // Wholesale Enquiries
    @Query("SELECT * FROM wholesale_enquiries ORDER BY createdAt DESC")
    fun observeAllWholesaleEnquiries(): Flow<List<WholesaleEnquiryEntity>>

    @Query("SELECT * FROM wholesale_enquiries WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeWholesaleEnquiriesForUser(userId: String): Flow<List<WholesaleEnquiryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWholesaleEnquiry(enquiry: WholesaleEnquiryEntity)

    // Delivery & PIN Codes
    @Query("SELECT * FROM delivery_settings WHERE id = 1 LIMIT 1")
    fun observeDeliverySettings(): Flow<DeliverySettingsEntity?>

    @Query("SELECT * FROM delivery_settings WHERE id = 1 LIMIT 1")
    suspend fun getDeliverySettings(): DeliverySettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDeliverySettings(settings: DeliverySettingsEntity)

    @Query("SELECT * FROM eligible_pincodes ORDER BY pinCode ASC")
    fun observeEligiblePinCodes(): Flow<List<EligiblePinCodeEntity>>

    @Query("SELECT * FROM eligible_pincodes WHERE pinCode = :pinCode LIMIT 1")
    suspend fun getEligiblePinCode(pinCode: String): EligiblePinCodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEligiblePinCode(pinCode: EligiblePinCodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEligiblePinCodes(pinCodes: List<EligiblePinCodeEntity>)

    @Query("DELETE FROM eligible_pincodes WHERE pinCode = :pinCode")
    suspend fun deleteEligiblePinCode(pinCode: String)

    // Business / GST Settings
    @Query("SELECT * FROM business_settings WHERE id = 1 LIMIT 1")
    fun observeBusinessSettings(): Flow<BusinessSettingsEntity?>

    @Query("SELECT * FROM business_settings WHERE id = 1 LIMIT 1")
    suspend fun getBusinessSettings(): BusinessSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBusinessSettings(settings: BusinessSettingsEntity)

    // Notifications
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY createdAt DESC")
    fun observeAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsRead(userId: String)

    // Newsletter
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNewsletter(entry: NewsletterEntity)

    @Query("SELECT * FROM newsletter_subscribers ORDER BY subscribedAt DESC")
    fun observeNewsletterSubscribers(): Flow<List<NewsletterEntity>>
}
