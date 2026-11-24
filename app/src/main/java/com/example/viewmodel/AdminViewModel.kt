package com.example.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.SeedCatalogData
import com.example.data.model.BusinessSettingsEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.DeliverySettingsEntity
import com.example.data.model.EligiblePinCodeEntity
import com.example.data.model.JsonCodec
import com.example.data.model.NewsletterEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.OfferEntity
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentStatus
import com.example.data.model.Product
import com.example.data.model.ProductEntity
import com.example.data.model.UserEntity
import com.example.data.model.WholesaleEnquiryEntity
import com.example.data.model.WholesaleStatus
import com.example.data.repository.ShasvatRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdminReportsSummary(
    val totalOrders: Int = 0,
    val totalSalesAmount: Int = 0,
    val pendingOrders: Int = 0,
    val completedOrders: Int = 0,
    val cancelledOrders: Int = 0,
    val codOrders: Int = 0,
    val onlineOrders: Int = 0,
    val bestSellingProducts: List<Pair<String, Int>> = emptyList(),
    val lowStockProducts: List<Product> = emptyList(),
    val totalWholesaleEnquiries: Int = 0,
    val newsletterCount: Int = 0
)

class AdminViewModel(
    private val repository: ShasvatRepository
) : ViewModel() {

    private val _orderSearchQuery = MutableStateFlow("")
    val orderSearchQuery: StateFlow<String> = _orderSearchQuery.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages = _messages.asSharedFlow()

    val categories: StateFlow<List<CategoryEntity>> = repository.categoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = repository.productsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.adminAllOrdersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredOrders: StateFlow<List<OrderEntity>> = combine(
        repository.adminAllOrdersFlow,
        _orderSearchQuery
    ) { orders, query ->
        val q = query.trim().lowercase()
        if (q.isBlank()) orders else {
            orders.filter {
                it.orderNumber.lowercase().contains(q) ||
                    it.customerName.lowercase().contains(q) ||
                    it.customerMobile.contains(q) ||
                    it.orderStatus.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.adminAllUsersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offers: StateFlow<List<OfferEntity>> = repository.offersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deliverySettings: StateFlow<DeliverySettingsEntity> = repository.deliverySettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SeedCatalogData.initialDeliverySettings)

    val eligiblePinCodes: StateFlow<List<EligiblePinCodeEntity>> = repository.eligiblePinCodesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wholesaleEnquiries: StateFlow<List<WholesaleEnquiryEntity>> = repository.adminAllWholesaleFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessSettings: StateFlow<BusinessSettingsEntity> = repository.businessSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SeedCatalogData.initialBusinessSettings)

    val allNotifications: StateFlow<List<NotificationEntity>> = repository.adminAllNotificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val newsletterSubscribers: StateFlow<List<NewsletterEntity>> = repository.adminNewsletterFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reportsSummary: StateFlow<AdminReportsSummary> = combine(
        repository.adminAllOrdersFlow,
        repository.productsFlow,
        repository.adminAllWholesaleFlow,
        repository.adminNewsletterFlow
    ) { orders, productList, enquiries, subscribers ->
        val validOrders = orders.filter { it.orderStatus != OrderStatus.CANCELLED.key }
        val totalSales = validOrders.sumOf { it.finalPayableAmount }
        val completed = orders.count { it.orderStatus == OrderStatus.DELIVERED.key }
        val cancelled = orders.count { it.orderStatus == OrderStatus.CANCELLED.key }
        val pending = orders.count {
            it.orderStatus != OrderStatus.DELIVERED.key && it.orderStatus != OrderStatus.CANCELLED.key
        }
        val cod = orders.count { it.paymentMethod == PaymentMethod.CASH_ON_DELIVERY.key }
        val online = orders.size - cod

        val productUnitCounts = mutableMapOf<String, Int>()
        validOrders.forEach { order ->
            JsonCodec.jsonToOrderItems(order.itemsJson).forEach { item ->
                productUnitCounts[item.productName] = (productUnitCounts[item.productName] ?: 0) + item.quantity
            }
        }
        val bestSellers = productUnitCounts.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key to it.value }

        val lowStock = productList.filter { it.stockQuantity != null && it.stockQuantity <= 5 }

        AdminReportsSummary(
            totalOrders = orders.size,
            totalSalesAmount = totalSales,
            pendingOrders = pending,
            completedOrders = completed,
            cancelledOrders = cancelled,
            codOrders = cod,
            onlineOrders = online,
            bestSellingProducts = bestSellers,
            lowStockProducts = lowStock,
            totalWholesaleEnquiries = enquiries.size,
            newsletterCount = subscribers.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AdminReportsSummary())

    fun setOrderSearchQuery(q: String) {
        _orderSearchQuery.value = q
    }

    suspend fun importPickedImages(uris: List<Uri>): List<String> = repository.importPickedImages(uris)

    fun saveProduct(entity: ProductEntity, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.adminSaveProduct(entity)
                .onSuccess {
                    _messages.tryEmit("Product '${entity.name}' saved")
                    onDone()
                }
                .onFailure { _messages.tryEmit(it.message ?: "Failed to save product") }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.adminDeleteProduct(productId)
                .onSuccess { _messages.tryEmit("Product deleted") }
                .onFailure { _messages.tryEmit(it.message ?: "Failed to delete product") }
        }
    }

    fun saveCategory(category: CategoryEntity) {
        viewModelScope.launch {
            repository.adminSaveCategory(category)
                .onSuccess { _messages.tryEmit("Category '${category.name}' saved") }
                .onFailure { _messages.tryEmit(it.message ?: "Failed to save category") }
        }
    }

    fun updateOrderStatus(
        orderId: String,
        newStatus: OrderStatus,
        note: String,
        newPaymentStatus: PaymentStatus? = null
    ) {
        viewModelScope.launch {
            repository.adminUpdateOrderStatus(orderId, newStatus, note, newPaymentStatus)
                .onSuccess { _messages.tryEmit("Order status updated to ${newStatus.label}") }
                .onFailure { _messages.tryEmit(it.message ?: "Failed to update order") }
        }
    }

    fun toggleUserActive(userId: String, isActive: Boolean) {
        viewModelScope.launch {
            repository.adminToggleUserActive(userId, isActive)
                .onSuccess { _messages.tryEmit(if (isActive) "Customer enabled" else "Customer disabled") }
                .onFailure { _messages.tryEmit(it.message ?: "Failed to update user") }
        }
    }

    fun saveDeliverySettings(settings: DeliverySettingsEntity) {
        viewModelScope.launch {
            repository.adminSaveDeliverySettings(settings)
                .onSuccess { _messages.tryEmit("Delivery settings updated") }
                .onFailure { _messages.tryEmit(it.message ?: "Failed to update delivery settings") }
        }
    }

    fun savePinCode(pinCode: String, areaLabel: String, rushAvailable: Boolean) {
        viewModelScope.launch {
            repository.adminSaveEligiblePinCode(
                EligiblePinCodeEntity(pinCode.trim(), areaLabel.trim(), rushAvailable)
            ).onSuccess {
                _messages.tryEmit("PIN code $pinCode saved")
            }.onFailure {
                _messages.tryEmit(it.message ?: "Invalid PIN code")
            }
        }
    }

    fun deletePinCode(pinCode: String) {
        viewModelScope.launch {
            repository.adminDeleteEligiblePinCode(pinCode)
                .onSuccess { _messages.tryEmit("PIN code $pinCode removed") }
                .onFailure { _messages.tryEmit(it.message ?: "Failed to remove PIN code") }
        }
    }

    fun saveOffer(offer: OfferEntity) {
        viewModelScope.launch {
            repository.adminSaveOffer(offer)
                .onSuccess { _messages.tryEmit("Offer saved") }
                .onFailure { _messages.tryEmit(it.message ?: "Failed to save offer") }
        }
    }

    fun deleteOffer(offerId: String) {
        viewModelScope.launch {
            repository.adminDeleteOffer(offerId)
                .onSuccess { _messages.tryEmit("Offer deleted") }
                .onFailure { _messages.tryEmit(it.message ?: "Failed to delete offer") }
        }
    }

    fun updateWholesaleEnquiry(
        enquiryId: String,
        quotedUnitPrice: Int?,
        status: WholesaleStatus,
        remarks: String
    ) {
        viewModelScope.launch {
            repository.adminUpdateWholesaleEnquiry(enquiryId, quotedUnitPrice, status, remarks)
                .onSuccess { _messages.tryEmit("Wholesale enquiry updated") }
                .onFailure { _messages.tryEmit(it.message ?: "Failed to update enquiry") }
        }
    }

    fun saveBusinessSettings(settings: BusinessSettingsEntity) {
        viewModelScope.launch {
            repository.adminSaveBusinessSettings(settings)
                .onSuccess { _messages.tryEmit("GST & Business settings saved") }
                .onFailure { _messages.tryEmit(it.message ?: "Failed to save business settings") }
        }
    }

    companion object {
        fun factory(repository: ShasvatRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AdminViewModel(repository) as T
                }
            }
    }
}
