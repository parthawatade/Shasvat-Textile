package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.SeedCatalogData
import com.example.data.model.AddressEntity
import com.example.data.model.BusinessSettingsEntity
import com.example.data.model.DeliveryMethod
import com.example.data.model.NotificationEntity
import com.example.data.model.OrderEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.ShippingAddress
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.WholesaleEnquiryEntity
import com.example.data.network.GatewayConfigStatus
import com.example.data.repository.ShasvatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class CheckoutUiState(
    val currentStep: Int = 0, // 0..4 (Step 1..5), Step 6 = Confirmation
    val customerName: String = "",
    val customerMobile: String = "",
    val customerEmail: String = "",
    val alternateMobile: String = "",
    val houseNumber: String = "",
    val building: String = "",
    val street: String = "",
    val area: String = "",
    val city: String = "",
    val state: String = "Maharashtra",
    val pinCode: String = "",
    val deliveryInstructions: String = "",
    val saveAddressForFuture: Boolean = true,
    val deliveryMethod: DeliveryMethod = DeliveryMethod.STANDARD,
    val paymentMethod: PaymentMethod = PaymentMethod.CASH_ON_DELIVERY,
    val useGatewayTestMode: Boolean = true,
    val gatewayTestCredential: String = "success@razorpay",
    val isSubmitting: Boolean = false,
    val idempotencyKey: String = UUID.randomUUID().toString(),
    val errorMessage: String? = null,
    val placedOrder: OrderEntity? = null
)

class CheckoutAndAccountViewModel(
    private val repository: ShasvatRepository
) : ViewModel() {

    private val _checkoutState = MutableStateFlow(CheckoutUiState())
    val checkoutState: StateFlow<CheckoutUiState> = _checkoutState.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _messages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages = _messages.asSharedFlow()

    val activeUser: StateFlow<UserEntity?> = repository.activeUserFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val adminCount: StateFlow<Int> = repository.adminCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val savedAddresses: StateFlow<List<AddressEntity>> = repository.userAddressesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userOrders: StateFlow<List<OrderEntity>> = repository.userOrdersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.userNotificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wholesaleEnquiries: StateFlow<List<WholesaleEnquiryEntity>> = repository.userWholesaleEnquiriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val businessSettings: StateFlow<BusinessSettingsEntity> = repository.businessSettingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SeedCatalogData.initialBusinessSettings)

    fun getGatewayConfigStatus(): GatewayConfigStatus = repository.getGatewayConfigStatus()

    fun observeOrderById(orderId: String): Flow<OrderEntity?> = repository.observeOrderById(orderId)

    // Auth actions
    fun clearAuthError() {
        _authError.value = null
    }

    fun signUp(
        fullName: String,
        email: String,
        mobile: String,
        password: String,
        asInitialAdmin: Boolean = false,
        onSuccess: (UserEntity) -> Unit = {}
    ) {
        viewModelScope.launch {
            _authError.value = null
            repository.signUp(fullName, email, mobile, password, asInitialAdmin)
                .onSuccess { user ->
                    _messages.tryEmit(
                        if (user.role == UserRole.ADMIN.key) "Store Admin account created and signed in"
                        else "Welcome to Shasvat Textiles, ${user.fullName}!"
                    )
                    onSuccess(user)
                }
                .onFailure { err ->
                    _authError.value = err.message ?: "Registration failed"
                }
        }
    }

    fun login(
        emailOrMobile: String,
        password: String,
        onSuccess: (UserEntity) -> Unit = {}
    ) {
        viewModelScope.launch {
            _authError.value = null
            repository.login(emailOrMobile, password)
                .onSuccess { user ->
                    _messages.tryEmit("Signed in as ${user.fullName}")
                    onSuccess(user)
                }
                .onFailure { err ->
                    _authError.value = err.message ?: "Login failed"
                }
        }
    }

    fun resetPassword(email: String, mobile: String, newPassword: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _authError.value = null
            repository.resetPassword(email, mobile, newPassword)
                .onSuccess {
                    _messages.tryEmit("Password updated successfully. Please sign in.")
                    onSuccess()
                }
                .onFailure { err ->
                    _authError.value = err.message ?: "Password reset failed"
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _messages.tryEmit("Signed out")
        }
    }

    fun updateProfile(fullName: String, mobile: String) {
        viewModelScope.launch {
            repository.updateCustomerProfile(fullName, mobile)
                .onSuccess { _messages.tryEmit("Profile updated") }
                .onFailure { _messages.tryEmit(it.message ?: "Failed to update profile") }
        }
    }

    fun saveAddress(address: AddressEntity) {
        viewModelScope.launch {
            repository.saveAddress(address)
                .onSuccess { _messages.tryEmit("Delivery address saved") }
                .onFailure { _messages.tryEmit(it.message ?: "Unable to save address") }
        }
    }

    fun deleteAddress(addressId: String) {
        viewModelScope.launch {
            repository.deleteAddress(addressId)
            _messages.tryEmit("Address removed")
        }
    }

    // Checkout actions
    fun prepareCheckout(prefillPinCode: String, prefillDeliveryMethod: DeliveryMethod) {
        val user = activeUser.value
        val defaultAddr = savedAddresses.value.firstOrNull { it.isDefault } ?: savedAddresses.value.firstOrNull()
        _checkoutState.value = CheckoutUiState(
            currentStep = 0,
            customerName = defaultAddr?.fullName ?: user?.fullName ?: "",
            customerMobile = defaultAddr?.mobile ?: user?.mobile ?: "",
            customerEmail = user?.email ?: "",
            alternateMobile = defaultAddr?.alternateMobile ?: "",
            houseNumber = defaultAddr?.houseNumber ?: "",
            building = defaultAddr?.building ?: "",
            street = defaultAddr?.street ?: "",
            area = defaultAddr?.area ?: "",
            city = defaultAddr?.city ?: "",
            state = defaultAddr?.state ?: "Maharashtra",
            pinCode = prefillPinCode.ifBlank { defaultAddr?.pinCode ?: "" },
            deliveryInstructions = defaultAddr?.deliveryInstructions ?: "",
            deliveryMethod = prefillDeliveryMethod,
            idempotencyKey = UUID.randomUUID().toString()
        )
    }

    fun updateCheckoutFields(transform: (CheckoutUiState) -> CheckoutUiState) {
        _checkoutState.update { transform(it).copy(errorMessage = null) }
    }

    fun applySavedAddressToCheckout(address: AddressEntity) {
        _checkoutState.update {
            it.copy(
                customerName = address.fullName,
                customerMobile = address.mobile,
                alternateMobile = address.alternateMobile,
                houseNumber = address.houseNumber,
                building = address.building,
                street = address.street,
                area = address.area,
                city = address.city,
                state = address.state,
                pinCode = address.pinCode,
                deliveryInstructions = address.deliveryInstructions,
                errorMessage = null
            )
        }
    }

    fun goToCheckoutStep(step: Int) {
        _checkoutState.update { it.copy(currentStep = step.coerceIn(0, 5), errorMessage = null) }
    }

    fun validateAndAdvanceStep(isPinRushEligible: Boolean): Boolean {
        val s = _checkoutState.value
        val err = when (s.currentStep) {
            0 -> {
                when {
                    s.customerName.trim().length < 2 -> "Enter your full name."
                    s.customerMobile.trim().length != 10 || !s.customerMobile.all { it.isDigit() } ->
                        "Enter a valid 10-digit mobile number."
                    !s.customerEmail.contains("@") || !s.customerEmail.contains(".") ->
                        "Enter a valid email address."
                    else -> null
                }
            }
            1 -> {
                when {
                    s.houseNumber.isBlank() -> "Enter house / flat number."
                    s.street.isBlank() -> "Enter street / road name."
                    s.area.isBlank() -> "Enter area / locality."
                    s.city.isBlank() -> "Enter city."
                    s.state.isBlank() -> "Enter state."
                    s.pinCode.trim().length != 6 || !s.pinCode.all { it.isDigit() } ->
                        "Enter a valid 6-digit PIN code."
                    else -> null
                }
            }
            2 -> {
                if (s.deliveryMethod == DeliveryMethod.RUSH && !isPinRushEligible) {
                    "Rush delivery is not available for PIN code ${s.pinCode}. Please select Standard Delivery."
                } else null
            }
            else -> null
        }

        if (err != null) {
            _checkoutState.update { it.copy(errorMessage = err) }
            return false
        }

        if (s.currentStep == 1) {
            viewModelScope.launch { repository.setSelectedPinCode(s.pinCode.trim()) }
        }
        _checkoutState.update { it.copy(currentStep = (it.currentStep + 1).coerceAtMost(4), errorMessage = null) }
        return true
    }

    fun placeOrder(
        preferredOfferId: String?,
        selectedFreeGiftId: String?,
        onOrderPlaced: (OrderEntity) -> Unit
    ) {
        val s = _checkoutState.value
        if (s.isSubmitting) return
        viewModelScope.launch {
            _checkoutState.update { it.copy(isSubmitting = true, errorMessage = null) }
            val addr = ShippingAddress(
                houseNumber = s.houseNumber.trim(),
                building = s.building.trim(),
                street = s.street.trim(),
                area = s.area.trim(),
                city = s.city.trim(),
                state = s.state.trim(),
                pinCode = s.pinCode.trim(),
                deliveryInstructions = s.deliveryInstructions.trim()
            )
            if (s.saveAddressForFuture && activeUser.value != null) {
                repository.saveAddress(
                    AddressEntity(
                        id = "",
                        userId = activeUser.value!!.id,
                        label = "Saved Address",
                        fullName = s.customerName.trim(),
                        mobile = s.customerMobile.trim(),
                        alternateMobile = s.alternateMobile.trim(),
                        houseNumber = addr.houseNumber,
                        building = addr.building,
                        street = addr.street,
                        area = addr.area,
                        city = addr.city,
                        state = addr.state,
                        pinCode = addr.pinCode,
                        deliveryInstructions = addr.deliveryInstructions,
                        isDefault = savedAddresses.value.isEmpty()
                    )
                )
            }

            val result = repository.placeOrder(
                customerName = s.customerName,
                customerMobile = s.customerMobile,
                customerEmail = s.customerEmail,
                alternateMobile = s.alternateMobile,
                shippingAddress = addr,
                deliveryMethod = s.deliveryMethod,
                paymentMethod = s.paymentMethod,
                preferredOfferId = preferredOfferId,
                selectedFreeGiftId = selectedFreeGiftId,
                idempotencyKey = s.idempotencyKey,
                useGatewayTestMode = s.useGatewayTestMode,
                gatewayTestCredential = s.gatewayTestCredential
            )

            result.onSuccess { order ->
                _checkoutState.update {
                    it.copy(
                        isSubmitting = false,
                        currentStep = 5,
                        placedOrder = order,
                        errorMessage = null
                    )
                }
                onOrderPlaced(order)
            }.onFailure { err ->
                _checkoutState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = err.message ?: "Order placement failed"
                    )
                }
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            repository.cancelOrderByCustomer(orderId)
                .onSuccess { _messages.tryEmit("Order cancelled") }
                .onFailure { _messages.tryEmit(it.message ?: "Unable to cancel order") }
        }
    }

    fun markNotificationsRead() {
        viewModelScope.launch { repository.markNotificationsRead() }
    }

    fun submitWholesaleEnquiry(
        businessName: String,
        contactName: String,
        mobile: String,
        email: String,
        gstNumber: String,
        productId: String,
        quantity: Int,
        notes: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.submitWholesaleEnquiry(
                businessName = businessName,
                contactName = contactName,
                mobile = mobile,
                email = email,
                gstNumber = gstNumber,
                productId = productId,
                requestedQuantity = quantity,
                notes = notes
            ).onSuccess {
                _messages.tryEmit("Wholesale enquiry submitted (${it.id})")
                onSuccess()
            }.onFailure {
                _messages.tryEmit(it.message ?: "Unable to submit enquiry")
            }
        }
    }

    fun respondToWholesaleQuote(enquiryId: String, accept: Boolean) {
        viewModelScope.launch {
            repository.respondToWholesaleQuote(enquiryId, accept)
                .onSuccess {
                    _messages.tryEmit(if (accept) "Quotation accepted" else "Quotation declined")
                }
                .onFailure {
                    _messages.tryEmit(it.message ?: "Unable to update quotation")
                }
        }
    }

    companion object {
        fun factory(repository: ShasvatRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CheckoutAndAccountViewModel(repository) as T
                }
            }
    }
}
